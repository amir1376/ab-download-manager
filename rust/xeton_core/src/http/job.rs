// xeton_core::http::job — HTTP download job state machine.
//
// Direct port of `ir.amirab.downloader.downloaditem.http.HttpDownloadJob`.
// State machine:
//   Idle → Booting → FetchingInfo → PreparingFile → Downloading → Completed
//                                                         ↕ Retrying
//                                                         → Paused / Error

use std::path::PathBuf;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};

use reqwest::header::HeaderMap;
use tokio::sync::{watch, Mutex, RwLock};
use tokio::task::JoinHandle;
use tokio::time::{interval, Duration};
use tokio_util::sync::CancellationToken;
use tracing::{debug, info};

use crate::connection::{HttpClient, DEFAULT_USER_AGENT};
use crate::connection::proxy::ProxyConfig;
use crate::db::{DownloadDb, PartDb, ProgressBatcher};
use crate::destination::{atomic_rename, DiskActor, IncompleteFileUtil};
use crate::models::*;
use crate::part::{split_to_ranges, PartRunner, SplitGuard};
use crate::throttle::{ThrottleChain, Throttler};

/// Delay between retry attempts in milliseconds.
const RETRY_DELAY_MS: u64 = 3_000;

/// HTTP download job managing multiple part runners.
pub struct HttpJob {
    /// The download item (protected for concurrent access by parts and auto-saver).
    pub item: Arc<RwLock<DownloadItem>>,
    /// Byte-range parts.
    parts: Arc<Mutex<Vec<RangedPart>>>,
    /// HTTP client (connection pool shared across parts).
    client: Arc<HttpClient>,
    /// Disk writer actor.
    disk: Mutex<Option<DiskActor>>,
    /// Part runners keyed by `part.from`.
    runners: Arc<Mutex<Vec<Arc<PartRunner>>>>,
    /// Dynamic part split guard.
    split_guard: Arc<SplitGuard>,
    /// Download settings.
    settings: Arc<RwLock<DownloadSettings>>,
    /// Database handles.
    dl_db: Arc<dyn DownloadDb>,
    part_db: Arc<dyn PartDb>,
    /// Job-level throttler (stacked on top of the global throttler).
    job_throttler: Arc<Throttler>,
    /// Global throttler.
    global_throttler: Arc<Throttler>,
    /// Progress batcher.
    batcher: ProgressBatcher,

    // ── Runtime state ───────────────────────────────────────────────────
    /// Job status broadcast.
    status_tx: watch::Sender<JobStatus>,
    pub status_rx: watch::Receiver<JobStatus>,
    /// Whether the server supports resuming (Range requests).
    supports_concurrent: Mutex<Option<bool>>,
    /// Server's last-modified timestamp (epoch ms).
    server_last_modified: Mutex<Option<i64>>,
    /// Whether to enforce strict content-length validation.
    strict_mode: Mutex<bool>,
    /// Cancellation token for the active download scope.
    cancel: Mutex<Option<CancellationToken>>,
    /// Auto-saver task handle.
    auto_saver: Mutex<Option<JoinHandle<()>>>,
    /// Dynamic re-segmenting monitor task handle.
    split_monitor: Mutex<Option<JoinHandle<()>>>,
    /// Retry state.
    failed_tries: Mutex<u32>,
    downloaded_before_retry: Mutex<i64>,
    /// Data directory for the download.
    data_dir: PathBuf,
}

impl HttpJob {
    /// Create a new HTTP download job.
    pub fn new(
        item: DownloadItem,
        settings: Arc<RwLock<DownloadSettings>>,
        dl_db: Arc<dyn DownloadDb>,
        part_db: Arc<dyn PartDb>,
        global_throttler: Arc<Throttler>,
        batcher: ProgressBatcher,
        data_dir: PathBuf,
        proxy: &ProxyConfig,
    ) -> Result<Arc<Self>, crate::connection::ConnectionError> {
        let (status_tx, status_rx) = watch::channel(JobStatus::Idle);
        let client = Arc::new(HttpClient::new(proxy, DEFAULT_USER_AGENT)?);
        let job_throttler = Arc::new(Throttler::new(item.speed_limit));

        Ok(Arc::new(Self {
            item: Arc::new(RwLock::new(item)),
            parts: Arc::new(Mutex::new(Vec::new())),
            client,
            disk: Mutex::new(None),
            runners: Arc::new(Mutex::new(Vec::new())),
            split_guard: Arc::new(SplitGuard::new()),
            settings,
            dl_db,
            part_db,
            job_throttler,
            global_throttler,
            batcher,
            status_tx,
            status_rx,
            supports_concurrent: Mutex::new(None),
            server_last_modified: Mutex::new(None),
            strict_mode: Mutex::new(true),
            cancel: Mutex::new(None),
            auto_saver: Mutex::new(None),
            split_monitor: Mutex::new(None),
            failed_tries: Mutex::new(0),
            downloaded_before_retry: Mutex::new(0),
            data_dir,
        }))
    }

    /// Get the download item ID.
    pub async fn id(&self) -> i64 {
        self.item.read().await.numeric_id
    }

    /// Boot the job: load persisted state and initialize destination.
    pub async fn boot(&self) -> anyhow::Result<()> {
        let id = self.id().await;
        debug!("Booting HTTP job #{}", id);

        // Load parts from DB
        let saved_parts = self.part_db.get_parts(id).await?;
        let mut parts = self.parts.lock().await;
        *parts = saved_parts;

        // Infer concurrent support from saved parts count
        if parts.len() >= 2 {
            let mut sc = self.supports_concurrent.lock().await;
            *sc = Some(true);
        }

        // Apply per-job speed limit
        let item = self.item.read().await;
        self.job_throttler.set_limit(item.speed_limit).await;

        // Track initial downloaded size
        let downloaded = parts.iter().map(|p| p.downloaded()).sum::<i64>();
        let mut dbr = self.downloaded_before_retry.lock().await;
        *dbr = downloaded;

        Ok(())
    }

    /// Resume (start or continue) the download.
    pub async fn resume(self: &Arc<Self>) -> anyhow::Result<()> {
        let id = self.id().await;
        info!("Resuming HTTP job #{}", id);

        // Cancel any existing download scope
        self.cancel_download_scope().await;

        let cancel = CancellationToken::new();
        {
            let mut c = self.cancel.lock().await;
            *c = Some(cancel.clone());
        }

        self.set_status(JobStatus::Resuming);

        // Check if already completed
        {
            let parts = self.parts.lock().await;
            if !parts.is_empty() && parts.iter().all(|p| p.is_completed()) {
                self.on_download_finished().await?;
                return Ok(());
            }
        }

        // Fetch server info and validate
        self.fetch_download_info_and_validate().await?;

        // Create parts if needed
        self.create_parts_if_needed().await?;

        // Prepare destination file
        self.prepare_destination().await?;

        // Create and start part runners
        self.start_part_runners().await?;

        // Start auto-saver
        self.start_auto_saver().await;

        // Start dynamic re-segmenting monitor
        self.start_split_monitor().await;

        // Update status
        {
            let mut item = self.item.write().await;
            item.status = DownloadStatus::Downloading;
            if item.start_time.is_none() {
                item.start_time = Some(now_ms());
            }
        }
        self.save_state().await?;
        self.set_status(JobStatus::Downloading);

        Ok(())
    }

    /// Pause the download.
    pub async fn pause(&self) -> anyhow::Result<()> {
        let id = self.id().await;
        info!("Pausing HTTP job #{}", id);

        *self.failed_tries.lock().await = 0;
        self.cancel_download_scope().await;
        self.stop_all_runners().await;

        let mut item = self.item.write().await;
        item.status = DownloadStatus::Paused;
        drop(item);

        self.save_state().await?;
        self.set_status(JobStatus::Canceled {
            reason: "paused".into(),
        });

        Ok(())
    }

    /// Reset the download (clear all progress).
    pub async fn reset(&self) -> anyhow::Result<()> {
        self.pause().await?;

        let mut parts = self.parts.lock().await;
        parts.clear();

        let mut item = self.item.write().await;
        item.content_length = DownloadItem::LENGTH_UNKNOWN;
        item.server_etag = None;
        item.status = DownloadStatus::Added;
        item.start_time = None;
        item.complete_time = None;

        *self.strict_mode.lock().await = true;
        *self.downloaded_before_retry.lock().await = 0;

        drop(parts);
        drop(item);

        self.save_state().await?;
        Ok(())
    }

    /// Get total bytes downloaded across all parts.
    pub async fn downloaded_size(&self) -> i64 {
        let parts = self.parts.lock().await;
        parts.iter().map(|p| p.downloaded()).sum()
    }

    // ── Internal methods ────────────────────────────────────────────────

    /// Fetch server info and validate against existing state.
    /// Mirrors `HttpDownloadJob.fetchDownloadInfoAndValidate()`.
    async fn fetch_download_info_and_validate(&self) -> anyhow::Result<()> {
        let item = self.item.read().await;
        let url = item.link.clone();
        drop(item);

        let headers = HeaderMap::new();
        let info = self.client.test(&url, &headers).await?;

        // Check resume support change
        {
            let mut sc = self.supports_concurrent.lock().await;
            if let Some(prev) = *sc {
                if prev && !info.resume_support {
                    anyhow::bail!("Server resume support changed: was supported, now not");
                }
            }
            *sc = Some(info.resume_support);
        }

        // Store last-modified
        if let Some(ref lm) = info.last_modified {
            if let Ok(ts) = parse_last_modified(lm) {
                *self.server_last_modified.lock().await = Some(ts);
            }
        }

        // Handle webpage responses
        let mut item = self.item.write().await;
        if info.is_webpage {
            if item.name.ends_with(".html") || item.name.ends_with(".htm") {
                *self.strict_mode.lock().await = false;
                *self.supports_concurrent.lock().await = Some(false);
                item.content_length = DownloadItem::LENGTH_UNKNOWN;
                item.server_etag = None;
            } else {
                anyhow::bail!("Expected a file but got a webpage — link may be invalid");
            }
        }

        // Validate content length
        if item.content_length == DownloadItem::LENGTH_UNKNOWN {
            // New download
            item.content_length = info.total_length.unwrap_or(-1);
            item.server_etag = info.etag.clone();
        } else {
            // Resuming — validate
            if let Some(total) = info.total_length {
                if total != item.content_length {
                    anyhow::bail!(
                        "Content length changed: expected {}, got {}",
                        item.content_length,
                        total
                    );
                }
            }
            // ETag validation
            if let (Some(ref old_etag), Some(ref new_etag)) = (&item.server_etag, &info.etag) {
                if old_etag != new_etag {
                    anyhow::bail!("ETag changed: {} → {}", old_etag, new_etag);
                }
            }
        }

        drop(item);
        self.save_state().await?;
        Ok(())
    }

    /// Create parts if none exist yet.
    async fn create_parts_if_needed(&self) -> anyhow::Result<()> {
        let mut parts = self.parts.lock().await;
        if !parts.is_empty() {
            return Ok(());
        }

        let item = self.item.read().await;
        let settings = self.settings.read().await;
        let sc = self.supports_concurrent.lock().await;

        if item.content_length == DownloadItem::LENGTH_UNKNOWN {
            // Blind download — single part
            parts.push(RangedPart::new(0, None, 0));
        } else if *sc == Some(true) {
            // Multi-part download
            let conn_count = item.preferred_connections.unwrap_or(settings.default_thread_count);
            let ranges = split_to_ranges(item.content_length, conn_count, settings.min_part_size);
            for (start, end) in ranges {
                parts.push(RangedPart::new(start, Some(end), start));
            }
        } else {
            // Single-part download
            let end = if item.content_length > 0 {
                Some(item.content_length - 1)
            } else {
                None
            };
            parts.push(RangedPart::new(0, end, 0));
        }

        drop(sc);
        drop(settings);
        drop(item);
        drop(parts);

        self.save_state().await?;
        Ok(())
    }

    /// Prepare the destination file (pre-allocate).
    async fn prepare_destination(&self) -> anyhow::Result<()> {
        self.set_status(JobStatus::PreparingFile { progress: None });

        let item = self.item.read().await;
        let settings = self.settings.read().await;

        let output_path = PathBuf::from(&item.folder).join(&item.name);
        let file_path = if settings.append_extension_to_incomplete {
            IncompleteFileUtil::add_indicator(&output_path, item.numeric_id)
        } else {
            output_path
        };

        let expected_size = if *self.strict_mode.lock().await
            && item.content_length != DownloadItem::LENGTH_UNKNOWN
            && *self.supports_concurrent.lock().await != Some(false)
        {
            Some(item.content_length as u64)
        } else {
            None
        };

        drop(settings);
        drop(item);

        let disk = DiskActor::spawn(file_path, expected_size).await?;
        *self.disk.lock().await = Some(disk);

        Ok(())
    }

    /// Create and start part runners for all incomplete parts.
    async fn start_part_runners(&self) -> anyhow::Result<()> {
        let parts = self.parts.lock().await;
        let item = self.item.read().await;
        let settings = self.settings.read().await;
        let disk_guard = self.disk.lock().await;
        let disk = disk_guard
            .as_ref()
            .ok_or_else(|| anyhow::anyhow!("No disk actor"))?;

        let url = item.link.clone();
        let headers = HeaderMap::new();
        let strict = *self.strict_mode.lock().await;
        let conn_count = item.preferred_connections.unwrap_or(settings.default_thread_count);

        let throttle = Arc::new(ThrottleChain::new(vec![
            self.global_throttler.clone(),
            self.job_throttler.clone(),
        ]));

        let mut runners = self.runners.lock().await;
        runners.clear();

        let incomplete_parts: Vec<_> = parts
            .iter()
            .filter(|p| !p.is_completed())
            .take(conn_count as usize)
            .cloned()
            .collect();

        for part in incomplete_parts {
            let writer = disk.writer_for(part.current as u64);
            let runner = Arc::new(PartRunner::new(part));
            let _handle = runner.clone().start(
                self.client.clone(),
                url.clone(),
                headers.clone(),
                writer,
                throttle.clone(),
                strict,
            );
            runners.push(runner);
        }

        Ok(())
    }

    /// Stop all active part runners.
    async fn stop_all_runners(&self) {
        let runners = self.runners.lock().await;
        for runner in runners.iter() {
            runner.stop();
        }
    }

    /// Cancel the active download scope.
    async fn cancel_download_scope(&self) {
        if let Some(cancel) = self.cancel.lock().await.take() {
            cancel.cancel();
        }
        self.stop_auto_saver().await;
        self.stop_split_monitor().await;
    }

    /// Start the periodic auto-saver (every 1 second).
    async fn start_auto_saver(&self) {
        let parts = self.parts.clone();
        let batcher = self.batcher.clone();
        let item = self.item.clone();

        let handle = tokio::spawn(async move {
            let mut ticker = interval(Duration::from_secs(1));
            loop {
                ticker.tick().await;
                let id = item.read().await.numeric_id;
                let item_data = item.read().await.clone();
                let parts_data: Vec<RangedPart> = parts.lock().await.clone();
                
                // Send debounced updates through the batcher instead of hitting the DB directly
                let _ = batcher.update_item(item_data).await;
                let _ = batcher.update_parts(id, parts_data).await;
            }
        });

        *self.auto_saver.lock().await = Some(handle);
    }

    /// Stop the auto-saver.
    async fn stop_auto_saver(&self) {
        if let Some(handle) = self.auto_saver.lock().await.take() {
            handle.abort();
        }
    }

    /// Stop the split monitor.
    async fn stop_split_monitor(&self) {
        if let Some(handle) = self.split_monitor.lock().await.take() {
            handle.abort();
        }
    }

    /// Start the dynamic re-segmenting monitor.
    ///
    /// Every 2 seconds, this task:
    ///   1. Ticks all speed trackers to refresh throughput measurements.
    ///   2. Checks if any runner has finished its segment.
    ///   3. Finds the slowest active segment by ETA.
    ///   4. Splits it in half if large enough, spawning a new runner.
    ///
    /// This implements the mathematical formalization from the roadmap:
    ///   s = argmax_j(R_j / v_j) ; split at offset + ⌊R_s / 2⌋
    async fn start_split_monitor(&self) {
        let parts = self.parts.clone();
        let runners = self.runners.clone();
        let split_guard = self.split_guard.clone();
        let settings = self.settings.clone();
        let client = self.client.clone();
        let item = self.item.clone();
        let global_throttler = self.global_throttler.clone();
        let job_throttler = self.job_throttler.clone();
        // Clone the DiskActor handle — this is cheap (just an mpsc::Sender clone).
        let disk = self.disk.lock().await.as_ref().cloned();
        let strict_mode = *self.strict_mode.lock().await;

        let handle = tokio::spawn(async move {
            let mut ticker = interval(Duration::from_secs(2));
            loop {
                ticker.tick().await;

                // Need a disk actor to create writers for new segments
                let disk = match disk.as_ref() {
                    Some(d) => d,
                    None => continue,
                };

                // 1. Tick all speed trackers
                let current_runners = runners.lock().await;
                for runner in current_runners.iter() {
                    runner.speed_tracker.tick().await;
                }

                // 2. Check settings
                let s = settings.read().await;
                if !s.dynamic_part_creation {
                    drop(current_runners);
                    continue;
                }
                let min_part_size = s.min_part_size;
                drop(s);

                // 3. Check if any runner has finished
                let has_finished = current_runners.iter().any(|r| !r.is_active());
                if !has_finished {
                    drop(current_runners);
                    continue;
                }

                // 4. Find the slowest active segment by ETA
                let mut slowest_idx: Option<usize> = None;
                let mut slowest_eta = Duration::ZERO;

                for (i, runner) in current_runners.iter().enumerate() {
                    if !runner.is_active() {
                        continue;
                    }
                    // None ETA = speed is 0 = infinitely slow → always the worst candidate
                    let eta = runner.estimated_eta().await.unwrap_or(Duration::from_secs(u64::MAX));
                    if eta > slowest_eta {
                        slowest_eta = eta;
                        slowest_idx = Some(i);
                    }
                }

                let target_idx = match slowest_idx {
                    Some(idx) => idx,
                    None => {
                        drop(current_runners);
                        continue;
                    }
                };

                // 5. Try to split the slowest segment
                let target_runner = &current_runners[target_idx];
                let new_part = split_guard.try_split(&target_runner.part, min_part_size).await;

                drop(current_runners);

                if let Some(new_part) = new_part {
                    debug!(
                        "Dynamic re-segment: split slowest part, new sub-segment [{}, {:?}] starts at offset {}",
                        new_part.from, new_part.to, new_part.current
                    );

                    // Add the new part to the parts list
                    {
                        let mut p = parts.lock().await;
                        p.push(new_part.clone());
                    }

                    // Create a PartWriter at the new part's starting offset
                    let writer = disk.writer_for(new_part.current as u64);

                    // Build the new runner
                    let runner = Arc::new(PartRunner::new(new_part));

                    let i = item.read().await;
                    let url = i.link.clone();
                    drop(i);

                    let headers = HeaderMap::new();
                    let throttle = Arc::new(ThrottleChain::new(vec![
                        global_throttler.clone(),
                        job_throttler.clone(),
                    ]));

                    // Spawn the runner task
                    let _handle = runner.clone().start(
                        client.clone(),
                        url,
                        headers,
                        writer,
                        throttle,
                        strict_mode,
                    );

                    // Register the runner so the auto-saver and future splits can see it
                    runners.lock().await.push(runner);
                }
            }
        });

        *self.split_monitor.lock().await = Some(handle);
    }

    /// Save current state to the database.
    pub async fn save_state(&self) -> anyhow::Result<()> {
        let item = self.item.read().await;
        self.batcher.update_item(item.clone()).await?;

        let parts = self.parts.lock().await;
        self.batcher.update_parts(item.numeric_id, parts.clone()).await?;
        Ok(())
    }

    /// Handle download completion.
    async fn on_download_finished(&self) -> anyhow::Result<()> {
        // Handle incomplete file rename
        let item = self.item.read().await;
        let settings = self.settings.read().await;
        let output_path = PathBuf::from(&item.folder).join(&item.name);

        if settings.append_extension_to_incomplete {
            let incomplete_path = IncompleteFileUtil::add_indicator(&output_path, item.numeric_id);
            if incomplete_path.exists() {
                atomic_rename(&incomplete_path, &output_path).await?;
            }
        }

        // Update last modified time
        if settings.use_server_last_modified {
            if let Some(ts) = *self.server_last_modified.lock().await {
                let modified = filetime::FileTime::from_unix_time(ts / 1000, 0);
                let _ = filetime::set_file_mtime(&output_path, modified);
            }
        }

        drop(settings);

        // Finalize
        let mut item = self.item.write().await;
        item.status = DownloadStatus::Completed;
        item.complete_time = Some(now_ms());

        // Handle blind part length update
        let parts = self.parts.lock().await;
        if item.content_length == DownloadItem::LENGTH_UNKNOWN && parts.len() == 1 {
            item.content_length = parts[0].downloaded();
        }

        drop(parts);
        drop(item);

        self.save_state().await?;
        self.set_status(JobStatus::Finished);

        // Shut down disk actor
        if let Some(disk) = self.disk.lock().await.as_ref() {
            disk.flush().await.ok();
            disk.shutdown().await;
        }

        info!("HTTP job #{} completed", self.id().await);
        Ok(())
    }

    /// Set the job status.
    fn set_status(&self, status: JobStatus) {
        let _ = self.status_tx.send(status);
    }

    /// Apply speed limit from the download item.
    pub async fn apply_speed_limit(&self) {
        let item = self.item.read().await;
        self.job_throttler.set_limit(item.speed_limit).await;
    }

    /// Remove the output file and any incomplete file.
    pub async fn remove_output_files(&self) {
        let item = self.item.read().await;
        let output = PathBuf::from(&item.folder).join(&item.name);
        let incomplete = IncompleteFileUtil::add_indicator(&output, item.numeric_id);

        let _ = tokio::fs::remove_file(&output).await;
        let _ = tokio::fs::remove_file(&incomplete).await;
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

/// Current time in epoch milliseconds.
fn now_ms() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as i64
}

/// Parse an HTTP `Last-Modified` header into epoch milliseconds.
fn parse_last_modified(value: &str) -> Result<i64, ()> {
    use chrono::DateTime;
    // HTTP date format: "Thu, 01 Dec 1994 16:00:00 GMT" (RFC 2822/7231)
    if let Ok(dt) = DateTime::parse_from_rfc2822(value) {
        return Ok(dt.timestamp_millis());
    }
    // Also try RFC 3339 just in case it's poorly formatted but ISO
    if let Ok(dt) = DateTime::parse_from_rfc3339(value) {
        return Ok(dt.timestamp_millis());
    }
    // Fallback: just return an error if we can't parse
    Err(())
}
