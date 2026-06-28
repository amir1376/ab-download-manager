// xeton_core::part — Async part runner for downloading byte ranges.
//
// Replaces `com.xeton.downloader.part.PartDownloader` which used an OS thread
// per chunk (via `kotlin.concurrent.thread`). The Rust version is a pure Tokio
// task — no OS thread needed. This is the critical performance win: 256 part
// runners use 256 Tokio tasks on a small thread pool (default: num_cpus).
//
// Key design:
//   - Each PartRunner is a self-contained state machine.
//   - Retry logic with exponential back-off (1s × 2^tries, capped at 30s).
//   - Status is broadcast via `tokio::sync::watch`.
//   - Stop signal via `Arc<AtomicBool>`.
//   - Dynamic part splitting via `SplitGuard` (shared mutex).

use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::sync::Arc;
use std::time::Instant;

use thiserror::Error;
use tokio::sync::{watch, Mutex};
use tokio::time::{sleep, Duration};
use tokio_stream::StreamExt;
use tracing::{debug, warn};

use crate::connection::{ConnectionError, HttpClient};
use crate::destination::PartWriter;
use crate::models::{PartStatus, RangedPart};
use crate::throttle::ThrottleChain;

// ─── SpeedTracker ───────────────────────────────────────────────────────────

/// Sliding-window speed tracker for a single part runner.
///
/// Records cumulative bytes downloaded and computes bytes/sec over the
/// most recent measurement window (default 2 seconds). This is used by
/// the dynamic re-segmenting monitor to identify the slowest active segment.
pub struct SpeedTracker {
    /// Cumulative bytes downloaded by this part since the current attempt started.
    bytes_total: AtomicU64,
    /// Snapshot of `bytes_total` at the last measurement tick.
    bytes_at_last_tick: AtomicU64,
    /// Wall-clock instant of the last measurement tick.
    last_tick: Mutex<Instant>,
    /// Computed speed in bytes per second (smoothed).
    speed_bps: AtomicU64,
}

impl SpeedTracker {
    pub fn new() -> Self {
        Self {
            bytes_total: AtomicU64::new(0),
            bytes_at_last_tick: AtomicU64::new(0),
            last_tick: Mutex::new(Instant::now()),
            speed_bps: AtomicU64::new(0),
        }
    }

    /// Record that `n` bytes were just received.
    #[inline]
    pub fn record(&self, n: u64) {
        self.bytes_total.fetch_add(n, Ordering::Relaxed);
    }

    /// Recompute the speed measurement. Called periodically by the monitor task.
    pub async fn tick(&self) {
        let now = Instant::now();
        let mut last = self.last_tick.lock().await;
        let elapsed = now.duration_since(*last);
        if elapsed.is_zero() {
            return;
        }

        let current = self.bytes_total.load(Ordering::Relaxed);
        let prev = self.bytes_at_last_tick.swap(current, Ordering::Relaxed);
        let delta = current.saturating_sub(prev);

        let bps = (delta as f64 / elapsed.as_secs_f64()) as u64;
        self.speed_bps.store(bps, Ordering::Relaxed);
        *last = now;
    }

    /// Current speed in bytes per second.
    #[inline]
    pub fn speed_bps(&self) -> u64 {
        self.speed_bps.load(Ordering::Relaxed)
    }

    /// Reset all counters (on retry).
    pub async fn reset(&self) {
        self.bytes_total.store(0, Ordering::Relaxed);
        self.bytes_at_last_tick.store(0, Ordering::Relaxed);
        self.speed_bps.store(0, Ordering::Relaxed);
        *self.last_tick.lock().await = Instant::now();
    }
}

impl Default for SpeedTracker {
    fn default() -> Self {
        Self::new()
    }
}

/// Maximum retries per part before declaring failure.
pub const PART_MAX_TRIES: u32 = 10;

/// Base retry delay in milliseconds.
const RETRY_DELAY_BASE_MS: u64 = 1_000;

/// Maximum retry delay cap.
const RETRY_DELAY_CAP_MS: u64 = 30_000;

/// Default buffer read size.
const BUFFER_SIZE: usize = 65536; // 64 KB

// ─── Errors ─────────────────────────────────────────────────────────────────

#[derive(Debug, Error)]
pub enum PartError {
    #[error("Connection error: {0}")]
    Connection(#[from] ConnectionError),

    #[error("Server part mismatch: expected {expected_len:?} bytes, got {actual_len}")]
    PartMismatch {
        expected_len: Option<i64>,
        actual_len: i64,
    },

    #[error("Too many retries ({tries})")]
    TooManyRetries { tries: u32 },

    #[error("Part cancelled")]
    Cancelled,

    #[error("Write error: {0}")]
    Write(String),

    #[error("Stream error: {0}")]
    Stream(String),
}

// ─── PartRunner ─────────────────────────────────────────────────────────────

/// Async download worker for a single byte range.
///
/// Replaces `com.xeton.downloader.part.PartDownloader` (which used an OS
/// thread). Runs entirely as a Tokio task.
pub struct PartRunner {
    /// The byte range this runner is responsible for.
    pub part: Arc<Mutex<RangedPart>>,
    /// Status broadcast channel.
    status_tx: watch::Sender<PartStatus>,
    /// Public receiver for status updates.
    pub status_rx: watch::Receiver<PartStatus>,
    /// Cancellation flag.
    stop: Arc<AtomicBool>,
    /// Is the runner actively downloading?
    pub active: Arc<AtomicBool>,
    /// Callback invoked when the part encounters too many errors.
    on_too_many_errors: Option<Box<dyn Fn(PartError) + Send + Sync>>,
    /// Real-time throughput tracker for dynamic re-segmenting decisions.
    pub speed_tracker: Arc<SpeedTracker>,
}

impl PartRunner {
    /// Create a new PartRunner for the given range.
    pub fn new(part: RangedPart) -> Self {
        let (status_tx, status_rx) = watch::channel(PartStatus::Idle);
        Self {
            part: Arc::new(Mutex::new(part)),
            status_tx,
            status_rx,
            stop: Arc::new(AtomicBool::new(false)),
            active: Arc::new(AtomicBool::new(false)),
            on_too_many_errors: None,
            speed_tracker: Arc::new(SpeedTracker::new()),
        }
    }

    /// Current download speed in bytes per second for this part.
    pub fn speed_bps(&self) -> u64 {
        self.speed_tracker.speed_bps()
    }

    /// Estimated time remaining for this part, based on current speed.
    /// Returns `None` if speed is zero or the part has unknown remaining size.
    pub async fn estimated_eta(&self) -> Option<Duration> {
        let part = self.part.lock().await;
        let remaining = part.remaining()?;
        if remaining <= 0 {
            return Some(Duration::ZERO);
        }
        let speed = self.speed_bps();
        if speed == 0 {
            return None; // Cannot estimate — will be treated as "infinitely slow"
        }
        Some(Duration::from_secs(remaining as u64 / speed))
    }

    /// Set the callback for when too many errors occur.
    pub fn set_error_callback<F>(&mut self, cb: F)
    where
        F: Fn(PartError) + Send + Sync + 'static,
    {
        self.on_too_many_errors = Some(Box::new(cb));
    }

    /// Signal the runner to stop.
    pub fn stop(&self) {
        self.stop.store(true, Ordering::Release);
    }

    /// Check if a stop was requested.
    fn is_stopped(&self) -> bool {
        self.stop.load(Ordering::Acquire)
    }

    /// Is the runner actively downloading?
    pub fn is_active(&self) -> bool {
        self.active.load(Ordering::Acquire)
    }

    /// Start the part download loop.
    ///
    /// This spawns a Tokio task that retries on failure up to `PART_MAX_TRIES` times
    /// with exponential back-off. Returns a `JoinHandle` to await completion.
    pub fn start(
        self: Arc<Self>,
        client: Arc<HttpClient>,
        url: String,
        headers: reqwest::header::HeaderMap,
        mut writer: PartWriter,
        throttle: Arc<ThrottleChain>,
        strict_mode: bool,
    ) -> tokio::task::JoinHandle<()> {
        let runner = self.clone();
        runner.stop.store(false, Ordering::Release);
        runner.active.store(true, Ordering::Release);

        tokio::spawn(async move {
            let mut tries: u32 = 0;

            loop {
                if runner.is_stopped() {
                    runner.set_status(PartStatus::Canceled("stopped".into()));
                    break;
                }

                // Check if already completed
                {
                    let part = runner.part.lock().await;
                    if part.is_completed() {
                        runner.set_status(PartStatus::Completed);
                        break;
                    }
                }

                if tries >= PART_MAX_TRIES {
                    let err = PartError::TooManyRetries { tries };
                    runner.set_status(PartStatus::Canceled(format!("{}", err)));
                    if let Some(ref cb) = runner.on_too_many_errors {
                        cb(err);
                    }
                    break;
                }

                if tries > 0 {
                    let delay = Self::retry_delay(tries);
                    debug!("Part retry #{} after {}ms", tries, delay.as_millis());
                    runner.speed_tracker.reset().await;
                    sleep(delay).await;
                }

                // Attempt download
                match runner
                    .download_once(&client, &url, &headers, &mut writer, &throttle, strict_mode)
                    .await
                {
                    Ok(completed) => {
                        if completed {
                            break;
                        }
                        // Part was split out from under us — exit gracefully
                        break;
                    }
                    Err(e) => {
                        tries += 1;
                        warn!("Part download error (try {}): {}", tries, e);
                        runner.set_status(PartStatus::Canceled(format!("{}", e)));

                        if runner.is_stopped() {
                            break;
                        }
                        // Retryable — loop continues
                    }
                }
            }

            runner.active.store(false, Ordering::Release);
            let part = runner.part.lock().await;
            if !part.is_completed() {
                runner.set_status(PartStatus::Idle);
            }
        })
    }

    /// Execute a single download attempt for this part.
    async fn download_once(
        &self,
        client: &HttpClient,
        url: &str,
        headers: &reqwest::header::HeaderMap,
        writer: &mut PartWriter,
        throttle: &ThrottleChain,
        strict_mode: bool,
    ) -> Result<bool, PartError> {
        let (from, to) = {
            let part = self.part.lock().await;
            (part.current, part.to)
        };

        // Connect
        self.set_status(PartStatus::Connecting);
        let conn = client.connect(url, headers, from, to).await?;

        if self.is_stopped() {
            return Err(PartError::Cancelled);
        }

        // Validate content length in strict mode
        if strict_mode {
            let expected_remaining = {
                let part = self.part.lock().await;
                part.remaining()
            };
            if let Some(expected) = expected_remaining {
                if conn.content_length >= 0 && conn.content_length != expected {
                    // Check if at least the range start matches
                    let range_ok = conn
                        .info
                        .content_range
                        .as_ref()
                        .is_some_and(|cr| cr.start == from);

                    if !range_ok {
                        return Err(PartError::PartMismatch {
                            expected_len: Some(expected),
                            actual_len: conn.content_length,
                        });
                    }
                }
            }
        }

        // Stream data
        self.set_status(PartStatus::Receiving);
        let mut stream = conn.stream;
        let mut first_chunk = true;

        while let Some(chunk_result) = stream.next().await {
            if self.is_stopped() {
                return Err(PartError::Cancelled);
            }

            let chunk = chunk_result.map_err(|e| PartError::Stream(e.to_string()))?;
            if chunk.is_empty() {
                continue;
            }

            let chunk_len = chunk.len() as i64;

            // Rate limiting
            throttle.acquire(chunk.len() as u32).await;

            // Record bytes for speed tracking (before write to include I/O wait)
            self.speed_tracker.record(chunk_len as u64);

            // Write to disk
            writer
                .write(chunk)
                .await
                .map_err(|e| PartError::Write(e.to_string()))?;

            // Update part progress
            {
                let mut part = self.part.lock().await;
                part.current += chunk_len;

                if part.is_completed() {
                    // Handle blind parts
                    if part.is_blind() {
                        part.set_blind_as_completed();
                    }
                    self.set_status(PartStatus::Completed);
                    return Ok(true);
                }
            }

            if first_chunk {
                first_chunk = false;
                // Reset tries on first successful data
            }
        }

        // Stream ended — check if we're done
        let part = self.part.lock().await;
        if part.is_completed() || part.is_blind() {
            drop(part);
            let mut part = self.part.lock().await;
            if part.is_blind() {
                part.set_blind_as_completed();
            }
            self.set_status(PartStatus::Completed);
            Ok(true)
        } else {
            // Stream ended before part was complete — will retry
            Err(PartError::Stream("stream ended prematurely".into()))
        }
    }

    /// Set status on the watch channel.
    fn set_status(&self, status: PartStatus) {
        let _ = self.status_tx.send(status);
    }

    /// Exponential back-off delay: base × 2^(tries-1), capped.
    fn retry_delay(tries: u32) -> Duration {
        let delay_ms = RETRY_DELAY_BASE_MS.saturating_mul(1 << (tries - 1).min(10));
        Duration::from_millis(delay_ms.min(RETRY_DELAY_CAP_MS))
    }
}

// ─── SplitGuard ─────────────────────────────────────────────────────────────

/// Guards dynamic part splitting, shared across all part runners of a job.
/// Mirrors the Kotlin `partSplitLock: Any` used with `synchronized` blocks.
pub struct SplitGuard {
    inner: Mutex<()>,
}

impl SplitGuard {
    pub fn new() -> Self {
        Self {
            inner: Mutex::new(()),
        }
    }

    /// Split a part in half, returning the new part if successful.
    ///
    /// The split only happens if the remaining bytes are at least `2 × min_part_size`.
    pub async fn try_split(
        &self,
        part: &Arc<Mutex<RangedPart>>,
        min_part_size: i64,
    ) -> Option<RangedPart> {
        let _lock = self.inner.lock().await;

        let mut p = part.lock().await;
        let remaining = p.remaining()?;

        if remaining < min_part_size * 2 {
            return None;
        }

        let mid = p.current + remaining / 2;
        let original_end = p.to?;

        // Shrink original part
        p.to = Some(mid - 1);

        // New part covers the second half
        Some(RangedPart::new(mid, Some(original_end), mid))
    }
}

impl Default for SplitGuard {
    fn default() -> Self {
        Self::new()
    }
}

// ─── Range Splitting ────────────────────────────────────────────────────────

/// Split a total byte range into parts.
/// Mirrors `com.xeton.downloader.utils.splitToRange`.
pub fn split_to_ranges(size: i64, max_parts: u32, min_part_size: i64) -> Vec<(i64, i64)> {
    if size <= 0 {
        return vec![(0, 0)];
    }

    let part_count = (max_parts as i64).min(size / min_part_size.max(1)).max(1);
    let part_size = size / part_count;
    let mut ranges = Vec::with_capacity(part_count as usize);

    let mut start = 0i64;
    for i in 0..part_count {
        let end = if i == part_count - 1 {
            size - 1
        } else {
            start + part_size - 1
        };
        ranges.push((start, end));
        start = end + 1;
    }

    ranges
}
