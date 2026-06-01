// xeton_core::db — SurrealDB-backed persistence layer.
//
// Replaces the Kotlin `IDownloadListDb` / `IDownloadPartListDb` file-based JSON stores
// with an embedded SurrealDB v3 instance using RocksDB for durable storage.

mod store;
pub mod progress_batcher;

pub use store::SurrealStore;
pub use progress_batcher::ProgressBatcher;



use crate::models::{DownloadItem, QueueModel, RangedPart};
use async_trait::async_trait;

// ─── Download DB trait ──────────────────────────────────────────────────────

/// Trait abstracting the download items database.
/// Mirrors `ir.amirab.downloader.db.IDownloadListDb`.
#[async_trait]
pub trait DownloadDb: Send + Sync {
    async fn get_all(&self) -> anyhow::Result<Vec<DownloadItem>>;
    async fn get_by_id(&self, id: i64) -> anyhow::Result<Option<DownloadItem>>;
    async fn get_last_id(&self) -> anyhow::Result<i64>;
    async fn add(&self, item: &DownloadItem) -> anyhow::Result<()>;
    async fn update(&self, item: &DownloadItem) -> anyhow::Result<()>;
    async fn remove(&self, id: i64) -> anyhow::Result<()>;
}

// ─── Part DB trait ──────────────────────────────────────────────────────────

/// Trait abstracting the download parts database.
/// Mirrors `ir.amirab.downloader.db.IDownloadPartListDb`.
#[async_trait]
pub trait PartDb: Send + Sync {
    async fn get_parts(&self, download_id: i64) -> anyhow::Result<Vec<RangedPart>>;
    async fn set_parts(&self, download_id: i64, parts: &[RangedPart]) -> anyhow::Result<()>;
    async fn remove_parts(&self, download_id: i64) -> anyhow::Result<()>;
}

// ─── Queue DB trait ─────────────────────────────────────────────────────────

/// Trait abstracting the queue persistence.
/// Mirrors `ir.amirab.downloader.db.DownloadQueuePersistedDataAccess`.
#[async_trait]
pub trait QueueDb: Send + Sync {
    async fn get_all_queues(&self) -> anyhow::Result<Vec<QueueModel>>;
    async fn get_queue(&self, id: i64) -> anyhow::Result<Option<QueueModel>>;
    async fn set_queue(&self, model: &QueueModel) -> anyhow::Result<()>;
    async fn remove_queue(&self, id: i64) -> anyhow::Result<()>;
}

// ─── Block DB trait ─────────────────────────────────────────────────────────

/// Trait abstracting block-level persistence for downloads.
#[async_trait]
pub trait BlockDb: Send + Sync {
    async fn get_blocks(&self, task_id: i64) -> anyhow::Result<Vec<crate::models::Block>>;
    async fn set_blocks(&self, task_id: i64, blocks: &[crate::models::Block]) -> anyhow::Result<()>;
    async fn remove_blocks(&self, task_id: i64) -> anyhow::Result<()>;
}

