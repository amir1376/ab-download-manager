// xeton_core::db::store — SurrealDB v3 embedded implementation.
//
// Uses the `kv-rocksdb` feature for durable on-disk storage.
// Schema:
//   - `task` table:           DownloadTask records (schemafull)
//   - `part` table:           PartSegment records (schemafull)
//   - `block_checksum` table: BlockChecksum records (schemafull)
//   - `queues` table:         QueueModel records keyed by queue id
//   - `counters` table:       Auto-increment counter for download IDs

use std::path::{Path, PathBuf};
use std::sync::Arc;

use async_trait::async_trait;
use serde::{Deserialize, Serialize};
use surrealdb::engine::local::SurrealKv;
use surrealdb::types::RecordId;
use surrealdb::types::SurrealValue;
use surrealdb::Surreal;
use tracing::{debug, info, warn};

use crate::db::{DownloadDb, PartDb, QueueDb, BlockDb};
use crate::models::{DownloadItem, QueueModel, RangedPart, Block, DownloadTask, PartSegment, BlockChecksum};

/// Discover default data directory based on OS.
pub fn discover_data_dir() -> PathBuf {
    #[cfg(target_os = "windows")]
    {
        if let Ok(app_data) = std::env::var("APPDATA") {
            PathBuf::from(app_data).join("Xeton").join("data")
        } else {
            PathBuf::from("C:\\Xeton\\data")
        }
    }
    #[cfg(target_os = "macos")]
    {
        if let Ok(home) = std::env::var("HOME") {
            PathBuf::from(home).join("Library").join("Application Support").join("Xeton").join("data")
        } else {
            PathBuf::from("/Library/Application Support/Xeton/data")
        }
    }
    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    {
        // Linux/Android/Fallback
        if let Ok(home) = std::env::var("HOME") {
            PathBuf::from(home).join(".config").join("xeton").join("data")
        } else {
            PathBuf::from("/data/local/tmp/xeton/data")
        }
    }
}

// ─── Internal record wrappers ───────────────────────────────────────────────

#[derive(Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
struct DownloadRecord {
    id: Option<RecordId>,
    #[serde(flatten)]
    inner: DownloadTask,
}

/// The actual persisted fields (no `numeric_id` — that's the SurrealDB key).
#[derive(Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
struct DownloadItemData {
    numeric_id: i64,
    name: String,
    folder: String,
    link: String,
    content_length: i64,
    status: crate::models::DownloadStatus,
    protocol: crate::models::DownloadProtocol,
    server_etag: Option<String>,
    date_added: i64,
    start_time: Option<i64>,
    complete_time: Option<i64>,
    preferred_connections: Option<u32>,
    speed_limit: i64,
}

impl From<&DownloadItem> for DownloadItemData {
    fn from(item: &DownloadItem) -> Self {
        Self {
            numeric_id: item.numeric_id,
            name: item.name.clone(),
            folder: item.folder.clone(),
            link: item.link.clone(),
            content_length: item.content_length,
            status: item.status.clone(),
            protocol: item.protocol.clone(),
            server_etag: item.server_etag.clone(),
            date_added: item.date_added,
            start_time: item.start_time,
            complete_time: item.complete_time,
            preferred_connections: item.preferred_connections,
            speed_limit: item.speed_limit,
        }
    }
}

impl From<DownloadItemData> for DownloadItem {
    fn from(data: DownloadItemData) -> Self {
        Self {
            numeric_id: data.numeric_id,
            name: data.name,
            folder: data.folder,
            link: data.link,
            content_length: data.content_length,
            status: data.status,
            protocol: data.protocol,
            server_etag: data.server_etag,
            date_added: data.date_added,
            start_time: data.start_time,
            complete_time: data.complete_time,
            preferred_connections: data.preferred_connections,
            speed_limit: data.speed_limit,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
struct QueueRecord {
    id: Option<RecordId>,
    #[serde(flatten)]
    inner: QueueModel,
}

#[derive(Debug, Serialize, Deserialize, SurrealValue)]
#[surreal(crate = "surrealdb::types")]
struct CounterRecord {
    id: Option<RecordId>,
    value: i64,
}

// ─── SurrealStore ───────────────────────────────────────────────────────────

pub struct SurrealStore {
    db: Surreal<surrealdb::engine::local::Db>,
}

impl SurrealStore {
    pub async fn open(data_dir: &Path) -> anyhow::Result<Arc<Self>> {
        let db_path = data_dir.join("xeton.db");
        info!("Opening SurrealDB at {}", db_path.display());

        let db = Surreal::new::<SurrealKv>(db_path.to_str().unwrap_or("xeton.db")).await?;
        db.use_ns("xeton").use_db("core").await?;

        let store = Arc::new(Self { db });
        store.ensure_schema().await?;
        Ok(store)
    }

    async fn ensure_schema(&self) -> anyhow::Result<()> {
        self.db
            .query(
                "
                DEFINE TABLE task SCHEMAFULL;
                DEFINE FIELD id ON TABLE task TYPE record<task>;
                DEFINE FIELD numeric_id ON TABLE task TYPE int;
                DEFINE FIELD url ON TABLE task TYPE string;
                DEFINE FIELD dest_path ON TABLE task TYPE string;
                DEFINE FIELD protocol ON TABLE task TYPE any;
                DEFINE FIELD total_size ON TABLE task TYPE int;
                DEFINE FIELD status ON TABLE task TYPE any;

                DEFINE TABLE part SCHEMAFULL;
                DEFINE FIELD id ON TABLE part TYPE record<part>;
                DEFINE FIELD task_id ON TABLE part TYPE int;
                DEFINE FIELD part_index ON TABLE part TYPE int;
                DEFINE FIELD from_offset ON TABLE part TYPE int;
                DEFINE FIELD to_offset ON TABLE part TYPE any;
                DEFINE FIELD current_offset ON TABLE part TYPE int;

                DEFINE TABLE block_checksum SCHEMAFULL;
                DEFINE FIELD id ON TABLE block_checksum TYPE record<block_checksum>;
                DEFINE FIELD task_id ON TABLE block_checksum TYPE int;
                DEFINE FIELD block_index ON TABLE block_checksum TYPE int;
                DEFINE FIELD expected_crc32 ON TABLE block_checksum TYPE int;
                DEFINE FIELD start_offset ON TABLE block_checksum TYPE int;
                DEFINE FIELD end_offset ON TABLE block_checksum TYPE int;
                DEFINE FIELD is_completed ON TABLE block_checksum TYPE bool;

                DEFINE TABLE queues SCHEMALESS;
                DEFINE TABLE counters SCHEMALESS;

                DEFINE INDEX IF NOT EXISTS idx_download_numeric_id ON TABLE task COLUMNS numeric_id UNIQUE;
                DEFINE INDEX IF NOT EXISTS idx_parts_download_id   ON TABLE part COLUMNS task_id, part_index UNIQUE;
                DEFINE INDEX IF NOT EXISTS idx_queue_id            ON TABLE queues COLUMNS inner.id UNIQUE;
                DEFINE INDEX IF NOT EXISTS idx_blocks_task_id      ON TABLE block_checksum COLUMNS task_id, block_index UNIQUE;
                ",
            )
            .await?;
        debug!("Schema ensured");
        Ok(())
    }

    async fn next_download_id(&self) -> anyhow::Result<i64> {
        let result: Option<CounterRecord> = self
            .db
            .upsert(("counters", "download_id"))
            .content(CounterRecord {
                id: None,
                value: 1,
            })
            .await?;

        if let Some(_counter) = result {
            let updated: Option<CounterRecord> = self
                .db
                .query("UPDATE counters:download_id SET value += 1 RETURN AFTER")
                .await?
                .take(0)?;
            Ok(updated.map(|c| c.value).unwrap_or(1))
        } else {
            Ok(1)
        }
    }
}

// ─── DownloadDb impl ────────────────────────────────────────────────────────

#[async_trait]
impl DownloadDb for SurrealStore {
    async fn get_all(&self) -> anyhow::Result<Vec<DownloadItem>> {
        let records: Vec<DownloadRecord> = self.db.select("task").await?;
        Ok(records.into_iter().map(|r| r.inner.into()).collect())
    }

    async fn get_by_id(&self, id: i64) -> anyhow::Result<Option<DownloadItem>> {
        let mut result = self
            .db
            .query("SELECT * FROM task WHERE numeric_id = $id LIMIT 1")
            .bind(("id", id))
            .await?;
        let records: Vec<DownloadRecord> = result.take(0)?;
        Ok(records.into_iter().next().map(|r| r.inner.into()))
    }

    async fn get_last_id(&self) -> anyhow::Result<i64> {
        let mut result = self
            .db
            .query("SELECT numeric_id FROM task ORDER BY numeric_id DESC LIMIT 1")
            .await?;

        #[derive(Deserialize, SurrealValue)]
        #[surreal(crate = "surrealdb::types")]
        struct IdOnly {
            numeric_id: i64,
        }

        let records: Vec<IdOnly> = result.take(0)?;
        Ok(records.into_iter().next().map(|r| r.numeric_id).unwrap_or(0))
    }

    async fn add(&self, item: &DownloadItem) -> anyhow::Result<()> {
        let data = DownloadTask::from(item);
        let _: Option<DownloadRecord> = self
            .db
            .create(("task", item.numeric_id))
            .content(data)
            .await?;
        debug!("Added download task #{}", item.numeric_id);
        Ok(())
    }

    async fn update(&self, item: &DownloadItem) -> anyhow::Result<()> {
        let data = DownloadTask::from(item);
        let _: Option<DownloadRecord> = self
            .db
            .update(("task", item.numeric_id))
            .content(data)
            .await?;
        Ok(())
    }
    async fn remove(&self, id: i64) -> anyhow::Result<()> {
        let _: Option<DownloadRecord> = self.db.delete(("task", id)).await?;
        debug!("Removed download task #{}", id);
        Ok(())
    }
}

// ─── PartDb impl ────────────────────────────────────────────────────────────

#[async_trait]
impl PartDb for SurrealStore {
    async fn get_parts(&self, download_id: i64) -> anyhow::Result<Vec<RangedPart>> {
        let mut result = self.db.query("SELECT * FROM part WHERE task_id = $download_id ORDER BY part_index ASC")
            .bind(("download_id", download_id))
            .await?;
        let segments: Vec<PartSegment> = result.take(0)?;
        Ok(segments.into_iter().map(|s| s.to_ranged_part()).collect())
    }

    async fn set_parts(&self, download_id: i64, parts: &[RangedPart]) -> anyhow::Result<()> {
        self.remove_parts(download_id).await?;
        for (idx, part) in parts.iter().enumerate() {
            let segment = PartSegment::from_ranged_part(download_id, idx as u32, part);
            let _: Option<PartSegment> = self.db
                .create(("part", format!("{}_{}", download_id, idx)))
                .content(segment)
                .await?;
        }
        Ok(())
    }

    async fn remove_parts(&self, download_id: i64) -> anyhow::Result<()> {
        let _: Vec<PartSegment> = self.db.query("DELETE FROM part WHERE task_id = $download_id")
            .bind(("download_id", download_id))
            .await?
            .take(0)?;
        Ok(())
    }
}

// ─── QueueDb impl ───────────────────────────────────────────────────────────

#[async_trait]
impl QueueDb for SurrealStore {
    async fn get_all_queues(&self) -> anyhow::Result<Vec<QueueModel>> {
        let records: Vec<QueueRecord> = self.db.select("queues").await?;
        Ok(records.into_iter().map(|r| r.inner).collect())
    }

    async fn get_queue(&self, id: i64) -> anyhow::Result<Option<QueueModel>> {
        let record: Option<QueueRecord> = self.db.select(("queues", id)).await?;
        Ok(record.map(|r| r.inner))
    }

    async fn set_queue(&self, model: &QueueModel) -> anyhow::Result<()> {
        let record = QueueRecord {
            id: None,
            inner: model.clone(),
        };
        let _: Option<QueueRecord> = self
            .db
            .upsert(("queues", model.id))
            .content(record)
            .await?;
        Ok(())
    }

    async fn remove_queue(&self, id: i64) -> anyhow::Result<()> {
        let _: Option<QueueRecord> = self.db.delete(("queues", id)).await?;
        Ok(())
    }
}

// ─── BlockDb impl ───────────────────────────────────────────────────────────

#[async_trait]
impl BlockDb for SurrealStore {
    async fn get_blocks(&self, task_id: i64) -> anyhow::Result<Vec<Block>> {
        let mut result = self.db.query("SELECT * FROM block_checksum WHERE task_id = $task_id ORDER BY block_index ASC")
            .bind(("task_id", task_id))
            .await?;
        let checksums: Vec<BlockChecksum> = result.take(0)?;
        Ok(checksums.into_iter().map(|bc| bc.into()).collect())
    }

    async fn set_blocks(&self, task_id: i64, blocks: &[Block]) -> anyhow::Result<()> {
        self.remove_blocks(task_id).await?;
        for (idx, block) in blocks.iter().enumerate() {
            let bc = BlockChecksum::from(block);
            let _: Option<BlockChecksum> = self.db
                .create(("block_checksum", format!("{}_{}", task_id, idx)))
                .content(bc)
                .await?;
        }
        Ok(())
    }

    async fn remove_blocks(&self, task_id: i64) -> anyhow::Result<()> {
        let _: Vec<BlockChecksum> = self.db.query("DELETE FROM block_checksum WHERE task_id = $task_id")
            .bind(("task_id", task_id))
            .await?
            .take(0)?;
        Ok(())
    }
}

// ─── JSON Migration ─────────────────────────────────────────────────────────

impl SurrealStore {
    pub async fn migrate_from_json_if_needed(&self, data_dir: &Path) -> anyhow::Result<()> {
        let existing: Vec<DownloadRecord> = self.db.select("task").await?;
        if !existing.is_empty() {
            debug!("SurrealDB already has {} downloads, skipping JSON migration", existing.len());
            return Ok(());
        }

        let json_dir = data_dir.join("downloadData");
        if !json_dir.exists() {
            debug!("No legacy downloadData directory found, skipping migration");
            return Ok(());
        }

        info!("Migrating legacy JSON data from {}", json_dir.display());

        let mut migrated = 0u32;
        if let Ok(entries) = std::fs::read_dir(&json_dir) {
            for entry in entries.flatten() {
                let path = entry.path();
                if path.extension().is_some_and(|ext| ext == "json") {
                    if let Ok(json_str) = std::fs::read_to_string(&path) {
                        if let Ok(data) = serde_json::from_str::<DownloadItemData>(&json_str) {
                            let item: DownloadItem = data.into();
                            if let Err(e) = self.add(&item).await {
                                warn!("Failed to migrate {}: {}", path.display(), e);
                            } else {
                                migrated += 1;
                            }
                        }
                    }
                }
            }
        }

        if migrated > 0 {
            info!("Successfully migrated {} downloads from JSON to SurrealDB", migrated);
        }

        Ok(())
    }
}
