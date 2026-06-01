// xeton_core::models — Core data types crossing the FFI boundary.
//
// These structs mirror the Kotlin `IDownloadItem`, `RangedPart`, `QueueModel`,
// and `DownloadSettings` with value semantics (Clone + Send + Sync).

use serde::{Deserialize, Serialize};
use surrealdb::sql::Thing;

// ─── Download Status ────────────────────────────────────────────────────────

/// Mirrors `ir.amirab.downloader.downloaditem.DownloadStatus`.
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, uniffi::Enum, surrealdb::SurrealValue)]
pub enum DownloadStatus {
    Added,
    Downloading,
    Paused,
    Completed,
    Error,
}

impl Default for DownloadStatus {
    fn default() -> Self {
        Self::Added
    }
}

// ─── Download Job Status ────────────────────────────────────────────────────

/// Runtime status of a download job, not persisted.
/// Mirrors `ir.amirab.downloader.downloaditem.DownloadJobStatus`.
#[derive(Clone, Debug, PartialEq, uniffi::Enum)]
pub enum JobStatus {
    Idle,
    Resuming,
    Downloading,
    PreparingFile { progress: Option<i32> },
    Retrying { delay_ms: i64 },
    Canceled { reason: String },
    Finished,
}

impl Default for JobStatus {
    fn default() -> Self {
        Self::Idle
    }
}

// ─── Download Protocol ──────────────────────────────────────────────────────

/// Protocol discriminator for multi-protocol dispatch.
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, uniffi::Enum, surrealdb::SurrealValue)]
pub enum DownloadProtocol {
    Http,
    Hls,
    Ftp,
    Torrent,
}

impl Default for DownloadProtocol {
    fn default() -> Self {
        Self::Http
    }
}

// ─── Download Item ──────────────────────────────────────────────────────────

/// Persistent download record.
/// Mirrors `ir.amirab.downloader.downloaditem.http.HttpDownloadItem` (and HLS variant).
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record, surrealdb::SurrealValue)]
pub struct DownloadItem {
    /// SurrealDB record id (table:id). The `id` field used by the rest of the
    /// code is the numeric suffix extracted from this record id.
    #[serde(skip)]
    pub numeric_id: i64,

    pub name: String,
    pub folder: String,
    pub link: String,

    /// Total content length in bytes. -1 = unknown.
    pub content_length: i64,

    pub status: DownloadStatus,
    pub protocol: DownloadProtocol,

    pub server_etag: Option<String>,

    /// Epoch milliseconds when the download was added.
    pub date_added: i64,
    /// Epoch milliseconds when the download was first started.
    pub start_time: Option<i64>,
    /// Epoch milliseconds when the download completed.
    pub complete_time: Option<i64>,

    /// User-requested connection count override. `None` uses global default.
    pub preferred_connections: Option<u32>,

    /// Per-download speed limit in bytes/s. 0 = unlimited.
    pub speed_limit: i64,
}

impl DownloadItem {
    pub const LENGTH_UNKNOWN: i64 = -1;

    pub fn record_id(&self) -> Thing {
        Thing::from(("downloads", self.numeric_id.to_string().as_str()))
    }
}

impl Default for DownloadItem {
    fn default() -> Self {
        Self {
            numeric_id: 0,
            name: String::new(),
            folder: String::new(),
            link: String::new(),
            content_length: Self::LENGTH_UNKNOWN,
            status: DownloadStatus::default(),
            protocol: DownloadProtocol::default(),
            server_etag: None,
            date_added: 0,
            start_time: None,
            complete_time: None,
            preferred_connections: None,
            speed_limit: 0,
        }
    }
}

// ─── Ranged Part ────────────────────────────────────────────────────────────

/// A byte-range segment of a download.
/// Mirrors `ir.amirab.downloader.part.RangedPart`.
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, surrealdb::SurrealValue)]
pub struct RangedPart {
    /// Absolute start byte offset within the file.
    pub from: i64,
    /// Absolute end byte (inclusive). `None` = "blind" part (unknown length).
    pub to: Option<i64>,
    /// How many bytes have been written starting from `from`.
    /// `current` is the next byte offset to write: `from + downloaded`.
    pub current: i64,
}

impl RangedPart {
    pub fn new(from: i64, to: Option<i64>, current: i64) -> Self {
        Self { from, to, current }
    }

    /// Total bytes downloaded for this part.
    #[inline]
    pub fn downloaded(&self) -> i64 {
        self.current - self.from
    }

    /// Is this part fully downloaded?
    #[inline]
    pub fn is_completed(&self) -> bool {
        match self.to {
            Some(end) => self.current > end,
            None => false, // blind parts never self-complete
        }
    }

    /// Remaining bytes. `None` for blind parts.
    #[inline]
    pub fn remaining(&self) -> Option<i64> {
        self.to.map(|end| (end - self.current + 1).max(0))
    }

    /// Is this a blind part (unknown end)?
    #[inline]
    pub fn is_blind(&self) -> bool {
        self.to.is_none()
    }

    /// Mark a blind part as completed by setting `to = current - 1`.
    pub fn set_blind_as_completed(&mut self) {
        if self.is_blind() && self.current > self.from {
            self.to = Some(self.current - 1);
        }
    }

    /// Reset downloaded progress to zero.
    pub fn reset_current(&mut self) {
        self.current = self.from;
    }
}

// ─── Part Download Status ───────────────────────────────────────────────────

/// Runtime status of a single part downloader.
/// Mirrors `ir.amirab.downloader.part.PartDownloadStatus`.
#[derive(Clone, Debug, PartialEq)]
pub enum PartStatus {
    Idle,
    Connecting,
    Receiving,
    Completed,
    Canceled(String),
}

// ─── New Download Request ───────────────────────────────────────────────────

/// Props for adding a new download, mirrors `NewDownloadItemProps`.
#[derive(Clone, Debug, uniffi::Record)]
pub struct NewDownloadProps {
    pub name: String,
    pub folder: String,
    pub link: String,
    pub protocol: DownloadProtocol,
    pub preferred_connections: Option<u32>,
    pub speed_limit: i64,
    pub on_duplicate: DuplicateStrategy,
}

#[derive(Clone, Debug, PartialEq, Eq, uniffi::Enum)]
pub enum DuplicateStrategy {
    AddNumbered,
    Override,
    Abort,
}

impl Default for DuplicateStrategy {
    fn default() -> Self {
        Self::AddNumbered
    }
}

// ─── Download Settings ──────────────────────────────────────────────────────

/// Global settings for the download engine.
/// Mirrors `ir.amirab.downloader.DownloadSettings`.
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record, surrealdb::SurrealValue)]
pub struct DownloadSettings {
    /// Default number of concurrent connections per download.
    pub default_thread_count: u32,
    /// Allow dynamic splitting of parts during download.
    pub dynamic_part_creation: bool,
    /// Use the server's `Last-Modified` header for the output file timestamp.
    pub use_server_last_modified: bool,
    /// Global speed limit in bytes/s. 0 = unlimited.
    pub global_speed_limit: i64,
    /// Minimum allowed part size in bytes before splitting.
    pub min_part_size: i64,
    /// Maximum number of retry attempts per download.
    pub max_retry_count: u32,
    /// Append `.xdlpart.<id>` extension to files in progress.
    pub append_extension_to_incomplete: bool,
}

impl Default for DownloadSettings {
    fn default() -> Self {
        Self {
            default_thread_count: 8,
            dynamic_part_creation: true,
            use_server_last_modified: false,
            global_speed_limit: 0,
            min_part_size: 2048,
            max_retry_count: 3,
            append_extension_to_incomplete: false,
        }
    }
}

// ─── Queue Model ────────────────────────────────────────────────────────────

/// Persistent queue descriptor.
/// Mirrors `ir.amirab.downloader.db.QueueModel`.
#[derive(Clone, Debug, Serialize, Deserialize, surrealdb::SurrealValue)]
pub struct QueueModel {
    pub id: i64,
    pub name: String,
    pub queue_items: Vec<i64>,
    pub max_concurrent: usize,
    pub stop_queue_on_empty: bool,
    pub scheduled_times: ScheduleTimes,
}

impl Default for QueueModel {
    fn default() -> Self {
        Self {
            id: 0,
            name: String::from("Default"),
            queue_items: Vec::new(),
            max_concurrent: 3,
            stop_queue_on_empty: true,
            scheduled_times: ScheduleTimes::default(),
        }
    }
}

/// Scheduled start/stop times for a queue.
/// Mirrors `ir.amirab.downloader.queue.ScheduleTimes`.
#[derive(Clone, Debug, Default, Serialize, Deserialize, surrealdb::SurrealValue)]
pub struct ScheduleTimes {
    pub enabled_start_time: bool,
    pub start_time_ms: i64,
    pub enabled_end_time: bool,
    pub end_time_ms: i64,
}

// ─── Manager Events ─────────────────────────────────────────────────────────

/// Events emitted by the DownloadManager.
/// Mirrors `DownloadManagerEvents` from Kotlin.
#[derive(Clone, Debug, uniffi::Enum)]
pub enum ManagerEvent {
    JobAdded { id: i64 },
    JobStarting { id: i64 },
    JobStarted { id: i64 },
    JobCanceled { id: i64, reason: String },
    JobCompleted { id: i64 },
    JobChanged { id: i64 },
    JobRemoved { id: i64 },
}

// ─── Content Range ──────────────────────────────────────────────────────────

/// Parsed `Content-Range` header.
#[derive(Clone, Debug)]
pub struct ContentRange {
    pub start: i64,
    pub end: i64,
    pub total: Option<i64>,
}

// ─── HTTP Response Info ─────────────────────────────────────────────────────

/// Information extracted from an HTTP response.
/// Mirrors `ir.amirab.downloader.connection.response.HttpResponseInfo`.
#[derive(Clone, Debug)]
pub struct ResponseInfo {
    pub status: u16,
    pub resume_support: bool,
    pub total_length: Option<i64>,
    pub content_length: Option<i64>,
    pub content_range: Option<ContentRange>,
    pub etag: Option<String>,
    pub last_modified: Option<String>,
    pub is_webpage: bool,
}
