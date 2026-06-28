// xeton_core::models — Core data types crossing the FFI boundary.
//
// These structs mirror the Kotlin `IDownloadItem`, `RangedPart`, `QueueModel`,
// and `DownloadSettings` with value semantics (Clone + Send + Sync).

use serde::{Deserialize, Serialize};
use surrealdb::types::RecordId;
use surrealdb::types::SurrealValue;

// ─── Download Status ────────────────────────────────────────────────────────

/// Mirrors `com.xeton.downloader.downloaditem.DownloadStatus`.
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, uniffi::Enum, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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
/// Mirrors `com.xeton.downloader.downloaditem.DownloadJobStatus`.
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
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, uniffi::Enum, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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
/// Mirrors `com.xeton.downloader.downloaditem.http.HttpDownloadItem` (and HLS variant).
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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

    pub fn record_id(&self) -> RecordId {
        RecordId::new("task", self.numeric_id)
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
/// Mirrors `com.xeton.downloader.part.RangedPart`.
#[derive(Clone, Debug, PartialEq, Eq, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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
/// Mirrors `com.xeton.downloader.part.PartDownloadStatus`.
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
pub struct TorrentMetadata {
    pub name: String,
    pub files: Vec<String>,
    pub total_bytes: i64,
}

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
/// Mirrors `com.xeton.downloader.DownloadSettings`.
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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
/// Mirrors `com.xeton.downloader.db.QueueModel`.
#[derive(Clone, Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
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
/// Mirrors `com.xeton.downloader.queue.ScheduleTimes`.
#[derive(Clone, Debug, Default, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
pub struct ScheduleTimes {
    pub enabled_start_time: bool,
    pub start_time_ms: i64,
    pub enabled_end_time: bool,
    pub end_time_ms: i64,
}

// ─── Block Struct ───────────────────────────────────────────────────────────

/// Represents a small sub-chunk (block) of a ranged part or file, used for fine-grained progress/checksumming.
#[derive(Clone, Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
pub struct Block {
    pub task_id: i64,
    pub block_index: u32,
    pub start_offset: u64,
    pub end_offset: u64,
    pub checksum: Option<u32>,
    pub is_completed: bool,
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
/// Mirrors `com.xeton.downloader.connection.response.HttpResponseInfo`.
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

// ─── Database Native Models ──────────────────────────────────────────────────

#[derive(Clone, Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
pub struct DownloadTask {
    pub id: i64,
    pub url: String,
    pub dest_path: String,
    pub protocol: DownloadProtocol,
    pub total_size: i64,
    pub status: DownloadStatus,
}

impl From<&DownloadItem> for DownloadTask {
    fn from(item: &DownloadItem) -> Self {
        Self {
            id: item.numeric_id,
            url: item.link.clone(),
            dest_path: format!("{}/{}", item.folder, item.name),
            protocol: item.protocol.clone(),
            total_size: item.content_length,
            status: item.status.clone(),
        }
    }
}

impl From<DownloadTask> for DownloadItem {
    fn from(task: DownloadTask) -> Self {
        let path = std::path::Path::new(&task.dest_path);
        let folder = path.parent().map(|p| p.to_string_lossy().into_owned()).unwrap_or_default();
        let name = path.file_name().map(|n| n.to_string_lossy().into_owned()).unwrap_or_default();
        Self {
            numeric_id: task.id,
            name,
            folder,
            link: task.url,
            content_length: task.total_size,
            status: task.status,
            protocol: task.protocol,
            server_etag: None,
            date_added: 0,
            start_time: None,
            complete_time: None,
            preferred_connections: None,
            speed_limit: 0,
        }
    }
}

#[derive(Clone, Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
pub struct PartSegment {
    pub task_id: i64,
    pub part_index: u32,
    pub from_offset: i64,
    pub to_offset: Option<i64>,
    pub current_offset: i64,
}

impl PartSegment {
    pub fn to_ranged_part(&self) -> RangedPart {
        RangedPart {
            from: self.from_offset,
            to: self.to_offset,
            current: self.current_offset,
        }
    }

    pub fn from_ranged_part(task_id: i64, part_index: u32, part: &RangedPart) -> Self {
        Self {
            task_id,
            part_index,
            from_offset: part.from,
            to_offset: part.to,
            current_offset: part.current,
        }
    }
}

#[derive(Clone, Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
pub struct BlockChecksum {
    pub task_id: i64,
    pub block_index: u32,
    pub expected_crc32: u32,
    pub start_offset: u64,
    pub end_offset: u64,
    pub is_completed: bool,
}

impl From<&Block> for BlockChecksum {
    fn from(block: &Block) -> Self {
        Self {
            task_id: block.task_id,
            block_index: block.block_index,
            expected_crc32: block.checksum.unwrap_or(0),
            start_offset: block.start_offset,
            end_offset: block.end_offset,
            is_completed: block.is_completed,
        }
    }
}

impl From<BlockChecksum> for Block {
    fn from(bc: BlockChecksum) -> Self {
        Self {
            task_id: bc.task_id,
            block_index: bc.block_index,
            start_offset: bc.start_offset,
            end_offset: bc.end_offset,
            checksum: Some(bc.expected_crc32),
            is_completed: bc.is_completed,
        }
    }
}

