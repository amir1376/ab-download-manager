// xeton_core::manager — Download manager (engine hub).
//
// Port of `ir.amirab.downloader.DownloadManager`.
// Central coordinator: job registry, event bus, download lifecycle.

use std::path::PathBuf;
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};

use dashmap::DashMap;
use tokio::sync::{broadcast, RwLock};
use tracing::{debug, info, warn};

use crate::connection::proxy::ProxyConfig;
use crate::db::{DownloadDb, PartDb, SurrealStore};
use crate::http::job::HttpJob;
use crate::models::*;
use crate::queue::manager::QueueManager;
use crate::throttle::Throttler;

/// The top-level download manager.
///
/// Mirrors `ir.amirab.downloader.DownloadManager`.
/// Uses `DashMap` for lock-free concurrent reads of the job registry.
pub struct DownloadManager {
    /// Database store (SurrealDB).
    store: Arc<SurrealStore>,
    /// Download settings.
    pub settings: Arc<RwLock<DownloadSettings>>,
    /// Active download jobs keyed by download ID.
    jobs: DashMap<i64, Arc<HttpJob>>,
    /// Event broadcast channel.
    event_tx: broadcast::Sender<ManagerEvent>,
    /// Public event receiver (clone for each subscriber).
    pub event_rx: broadcast::Receiver<ManagerEvent>,
    /// Global speed limiter.
    pub global_throttler: Arc<Throttler>,
    /// Queue manager.
    pub queue_manager: Arc<QueueManager>,
    /// Proxy configuration.
    proxy: RwLock<ProxyConfig>,
    /// Data directory.
    data_dir: PathBuf,
}

impl DownloadManager {
    /// Create and boot the download manager.
    pub async fn new(data_dir: PathBuf, settings: DownloadSettings) -> anyhow::Result<Arc<Self>> {
        // Open SurrealDB
        let store = SurrealStore::open(&data_dir).await?;

        // Attempt JSON migration from legacy Kotlin data
        store.migrate_from_json_if_needed(&data_dir).await?;

        let (event_tx, event_rx) = broadcast::channel(256);
        let global_throttler = Arc::new(Throttler::new(settings.global_speed_limit));
        let queue_manager = Arc::new(QueueManager::new(store.clone()));

        let manager = Arc::new(Self {
            store,
            settings: Arc::new(RwLock::new(settings)),
            jobs: DashMap::new(),
            event_tx,
            event_rx,
            global_throttler,
            queue_manager,
            proxy: RwLock::new(ProxyConfig::System),
            data_dir,
        });

        Ok(manager)
    }

    /// Boot the manager: load pending downloads and create jobs.
    pub async fn boot(self: &Arc<Self>) -> anyhow::Result<()> {
        info!("Booting DownloadManager");

        // Boot queue manager
        self.queue_manager.boot().await?;

        // Create jobs for non-completed downloads
        let items = self.store.get_all().await?;
        let pending: Vec<_> = items
            .into_iter()
            .filter(|item| item.status != DownloadStatus::Completed)
            .collect();

        info!("Found {} pending downloads", pending.len());
        for item in pending {
            if let Err(e) = self.create_job(item).await {
                warn!("Failed to create job: {}", e);
            }
        }

        Ok(())
    }

    /// Add a new download.
    pub async fn add_download(&self, props: NewDownloadProps) -> anyhow::Result<i64> {
        // Validate
        anyhow::ensure!(!props.folder.is_empty(), "Download folder cannot be empty");
        anyhow::ensure!(!props.name.is_empty(), "Download name cannot be empty");
        anyhow::ensure!(!props.link.is_empty(), "Download link cannot be empty");

        // Check for duplicates
        let all_downloads = self.store.get_all().await?;
        let output_path = PathBuf::from(&props.folder).join(&props.name);
        let duplicates: Vec<_> = all_downloads
            .iter()
            .filter(|d| {
                PathBuf::from(&d.folder).join(&d.name) == output_path
            })
            .collect();

        if !duplicates.is_empty() {
            match props.on_duplicate {
                DuplicateStrategy::Abort => {
                    anyhow::bail!("Download already exists at {}", output_path.display());
                }
                DuplicateStrategy::Override => {
                    for dup in &duplicates {
                        self.delete_download(dup.numeric_id, true).await?;
                    }
                }
                DuplicateStrategy::AddNumbered => {
                    // Name will be adjusted below
                }
            }
        }

        // Generate unique name if needed
        let name = if props.on_duplicate == DuplicateStrategy::AddNumbered && !duplicates.is_empty()
        {
            generate_numbered_name(&props.name, &all_downloads, &props.folder)
        } else {
            props.name.clone()
        };

        // Get next ID
        let last_id = self.store.get_last_id().await?;
        let id = last_id + 1;

        let item = DownloadItem {
            numeric_id: id,
            name,
            folder: props.folder,
            link: props.link,
            content_length: DownloadItem::LENGTH_UNKNOWN,
            status: DownloadStatus::Added,
            protocol: props.protocol,
            server_etag: None,
            date_added: now_ms(),
            start_time: None,
            complete_time: None,
            preferred_connections: props.preferred_connections,
            speed_limit: props.speed_limit,
        };

        self.store.add(&item).await?;
        self.create_job(item).await?;
        self.emit(ManagerEvent::JobAdded { id });

        info!("Added download #{}", id);
        Ok(id)
    }

    /// Resume a download.
    pub async fn resume(&self, id: i64) -> anyhow::Result<()> {
        if let Some(job) = self.jobs.get(&id) {
            job.resume().await?;
            self.emit(ManagerEvent::JobStarted { id });
        } else {
            // Try to recreate from DB
            if let Some(item) = self.store.get_by_id(id).await? {
                let job = self.create_job(item).await?;
                job.resume().await?;
                self.emit(ManagerEvent::JobStarted { id });
            }
        }
        Ok(())
    }

    /// Pause a download.
    pub async fn pause(&self, id: i64) -> anyhow::Result<()> {
        if let Some(job) = self.jobs.get(&id) {
            job.pause().await?;
            self.emit(ManagerEvent::JobCanceled {
                id,
                reason: "paused".into(),
            });
        }
        Ok(())
    }

    /// Reset a download (clear all progress).
    pub async fn reset(&self, id: i64) -> anyhow::Result<()> {
        if let Some(job) = self.jobs.get(&id) {
            job.reset().await?;
            self.emit(ManagerEvent::JobChanged { id });
        }
        Ok(())
    }

    /// Delete a download.
    pub async fn delete_download(&self, id: i64, remove_file: bool) -> anyhow::Result<()> {
        // Pause first
        let _ = self.pause(id).await;

        if remove_file {
            if let Some(job) = self.jobs.get(&id) {
                job.remove_output_files().await;
            }
        }

        // Remove from DB and registry
        self.jobs.remove(&id);
        self.store.remove(id).await?;
        self.store.remove_parts(id).await?;

        // Notify queues
        self.queue_manager.on_download_removed(id).await;

        self.emit(ManagerEvent::JobRemoved { id });
        info!("Deleted download #{}", id);
        Ok(())
    }

    /// Get all downloads.
    pub async fn get_download_list(&self) -> anyhow::Result<Vec<DownloadItem>> {
        self.store.get_all().await
    }

    /// Get status of a specific download.
    pub fn get_job_status(&self, id: i64) -> Option<JobStatus> {
        self.jobs
            .get(&id)
            .map(|job| job.status_rx.borrow().clone())
    }

    /// Get the number of active downloads.
    pub fn active_count(&self) -> usize {
        self.jobs
            .iter()
            .filter(|entry| {
                matches!(
                    *entry.value().status_rx.borrow(),
                    JobStatus::Downloading | JobStatus::Resuming
                )
            })
            .count()
    }

    /// Set the global speed limit.
    pub async fn set_global_speed_limit(&self, bytes_per_second: i64) {
        self.global_throttler.set_limit(bytes_per_second).await;
        let mut settings = self.settings.write().await;
        settings.global_speed_limit = bytes_per_second;
    }

    /// Reload settings on all active jobs.
    pub async fn reload_settings(&self, new_settings: DownloadSettings) {
        self.global_throttler
            .set_limit(new_settings.global_speed_limit)
            .await;
        let mut settings = self.settings.write().await;
        *settings = new_settings;
    }

    /// Set proxy configuration.
    pub async fn set_proxy(&self, proxy: ProxyConfig) {
        *self.proxy.write().await = proxy;
    }

    /// Stop all active downloads.
    pub async fn stop_all(&self) {
        let ids: Vec<i64> = self.jobs.iter().map(|e| *e.key()).collect();
        for id in ids {
            let _ = self.pause(id).await;
        }
    }

    /// Subscribe to manager events.
    pub fn subscribe(&self) -> broadcast::Receiver<ManagerEvent> {
        self.event_tx.subscribe()
    }

    // ── Internal ────────────────────────────────────────────────────────

    /// Create a job for a download item and add it to the registry.
    async fn create_job(&self, item: DownloadItem) -> anyhow::Result<Arc<HttpJob>> {
        let id = item.numeric_id;
        let proxy = self.proxy.read().await;

        let job = HttpJob::new(
            item,
            self.settings.clone(),
            self.store.clone(),
            self.store.clone(),
            self.global_throttler.clone(),
            self.data_dir.clone(),
            &proxy,
        )?;

        job.boot().await?;
        self.jobs.insert(id, job.clone());

        debug!("Created job #{}", id);
        Ok(job)
    }

    /// Emit a manager event.
    fn emit(&self, event: ManagerEvent) {
        let _ = self.event_tx.send(event);
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

fn now_ms() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as i64
}

/// Generate a numbered filename to avoid duplicates.
/// "file.zip" → "file (1).zip", "file (2).zip", etc.
fn generate_numbered_name(name: &str, existing: &[DownloadItem], folder: &str) -> String {
    let (stem, ext) = match name.rfind('.') {
        Some(i) => (&name[..i], &name[i..]),
        None => (name, ""),
    };

    let mut counter = 1u32;
    loop {
        let candidate = format!("{} ({}){}", stem, counter, ext);
        let candidate_path = PathBuf::from(folder).join(&candidate);
        let exists = existing.iter().any(|d| {
            PathBuf::from(&d.folder).join(&d.name) == candidate_path
        });
        if !exists {
            return candidate;
        }
        counter += 1;
    }
}
