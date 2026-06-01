// xeton_core::db::store — SurrealDB v3 embedded implementation.
//
// Uses the `kv-rocksdb` feature for durable on-disk storage.
// Schema:
//   - `downloads` table:  DownloadItem records keyed by numeric id
//   - `parts` table:      Per-download part lists keyed by download id
//   - `queues` table:     QueueModel records keyed by queue id
//   - `counters` table:   Auto-increment counter for download IDs

use std::path::Path;
use std::sync::Arc;

use async_trait::async_trait;
use serde::{Deserialize, Serialize};
use surrealdb::engine::local::SurrealKv;
use surrealdb::sql::Thing;
use surrealdb::Surreal;
use tracing::{debug, info, warn};

use crate::db::{DownloadDb, PartDb, QueueDb};
use crate::models::{DownloadItem, QueueModel, RangedPart};

// ─── Internal record wrappers ───────────────────────────────────────────────
// SurrealDB returns records with an `id` field. These wrappers let serde
// deserialize the full record including the SurrealDB id.

#[derive(Debug, Serialize, Deserialize, surrealdb::SurrealValue)]
struct DownloadRecord {
    id: Option<Thing>,
    #[serde(flatten)]
    inner: DownloadItemData,
}

/// The actual persisted fields (no `numeric_id` — that's the SurrealDB key).
#[derive(Debug, Serialize, Deserialize, surrealdb::SurrealValue)]
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

#[derive(Debug, Serialize, Deserialize, surrealdb::SurrealValue)]
struct PartsRecord {
    id: Option<Thing>,
    download_id: i64,
    parts: Vec<RangedPart>,
}

#[derive(Debug, Clone, Serialize, Deserialize, surrealdb::SurrealValue)]
struct QueueRecord {
    id: Option<Thing>,
    #[serde(flatten)]
    inner: QueueModel,
}

#[derive(Debug, Serialize, Deserialize, surrealdb::SurrealValue)]
struct CounterRecord {
    id: Option<Thing>,
    value: i64,
}

// ─── SurrealStore ───────────────────────────────────────────────────────────

/// Unified SurrealDB store implementing all three DB traits.
///
/// The database is opened once at startup with `SurrealStore::open()` and
/// shared via `Arc<SurrealStore>` across the manager, jobs, and queues.
pub struct SurrealStore {
    db: Surreal<surrealdb::engine::local::Db>,
}

impl SurrealStore {
    /// Open (or create) the embedded SurrealDB database at the given directory.
    ///
    /// The SurrealKV data files are stored inside `<data_dir>/xeton.db/`.
    pub async fn open(data_dir: &Path) -> anyhow::Result<Arc<Self>> {
        let db_path = data_dir.join("xeton.db");
        info!("Opening SurrealDB at {}", db_path.display());

        let db = Surreal::new::<Surrealkv>(db_path.to_str().unwrap_or("xeton.db")).await?;

        // Select namespace and database — SurrealDB requires these even for embedded.
        db.use_ns("xeton").use_db("core").await?;

        let store = Arc::new(Self { db });
        store.ensure_schema().await?;
        Ok(store)
    }

    /// Create tables and indexes if they don't exist.
    async fn ensure_schema(&self) -> anyhow::Result<()> {
        // SurrealDB v3 is schemaless by default — tables are created on first write.
        // We create indexes for fast lookups.
        self.db
            .query(
                "
                DEFINE INDEX IF NOT EXISTS idx_download_numeric_id ON TABLE downloads COLUMNS numeric_id UNIQUE;
                DEFINE INDEX IF NOT EXISTS idx_parts_download_id   ON TABLE parts     COLUMNS download_id UNIQUE;
                DEFINE INDEX IF NOT EXISTS idx_queue_id            ON TABLE queues    COLUMNS inner.id UNIQUE;
                ",
            )
            .await?;
        debug!("Schema ensured");
        Ok(())
    }

    /// Get the next auto-increment download ID.
    async fn next_download_id(&self) -> anyhow::Result<i64> {
        // Atomic increment using SurrealDB's UPDATE ... SET value += 1
        let result: Option<CounterRecord> = self
            .db
            .upsert(("counters", "download_id"))
            .content(CounterRecord {
                id: None,
                value: 1,
            })
            .await?;

        if let Some(counter) = result {
            // If the counter already existed, increment it
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
        let records: Vec<DownloadRecord> = self.db.select("downloads").await?;
        Ok(records.into_iter().map(|r| r.inner.into()).collect())
    }

    async fn get_by_id(&self, id: i64) -> anyhow::Result<Option<DownloadItem>> {
        let mut result = self
            .db
            .query("SELECT * FROM downloads WHERE numeric_id = $id LIMIT 1")
            .bind(("id", id))
            .await?;
        let records: Vec<DownloadRecord> = result.take(0)?;
        Ok(records.into_iter().next().map(|r| r.inner.into()))
    }

    async fn get_last_id(&self) -> anyhow::Result<i64> {
        let mut result = self
            .db
            .query("SELECT numeric_id FROM downloads ORDER BY numeric_id DESC LIMIT 1")
            .await?;

        #[derive(Deserialize)]
        struct IdOnly {
            numeric_id: i64,
        }

        let records: Vec<IdOnly> = result.take(0)?;
        Ok(records.into_iter().next().map(|r| r.numeric_id).unwrap_or(0))
    }

    async fn add(&self, item: &DownloadItem) -> anyhow::Result<()> {
        let data = DownloadItemData::from(item);
        let _: Option<DownloadRecord> = self
            .db
            .create(("downloads", item.numeric_id))
            .content(data)
            .await?;
        debug!("Added download #{}", item.numeric_id);
        Ok(())
    }

    async fn update(&self, item: &DownloadItem) -> anyhow::Result<()> {
        let data = DownloadItemData::from(item);
        let _: Option<DownloadRecord> = self
            .db
            .update(("downloads", item.numeric_id))
            .content(data)
            .await?;
        Ok(())
    }

    async fn remove(&self, id: i64) -> anyhow::Result<()> {
        let _: Option<DownloadRecord> = self.db.delete(("downloads", id)).await?;
        debug!("Removed download #{}", id);
        Ok(())
    }
}

// ─── PartDb impl ────────────────────────────────────────────────────────────

#[async_trait]
impl PartDb for SurrealStore {
    async fn get_parts(&self, download_id: i64) -> anyhow::Result<Vec<RangedPart>> {
        let record: Option<PartsRecord> = self.db.select(("parts", download_id)).await?;
        Ok(record.map(|r| r.parts).unwrap_or_default())
    }

    async fn set_parts(&self, download_id: i64, parts: &[RangedPart]) -> anyhow::Result<()> {
        let record = PartsRecord {
            id: None,
            download_id,
            parts: parts.to_vec(),
        };
        let _: Option<PartsRecord> = self
            .db
            .upsert(("parts", download_id))
            .content(record)
            .await?;
        Ok(())
    }

    async fn remove_parts(&self, download_id: i64) -> anyhow::Result<()> {
        let _: Option<PartsRecord> = self.db.delete(("parts", download_id)).await?;
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

// ─── JSON Migration ─────────────────────────────────────────────────────────

impl SurrealStore {
    /// Attempt to migrate legacy Kotlin JSON state files into SurrealDB.
    /// This is called once on first boot. If the SurrealDB already has data,
    /// migration is skipped.
    pub async fn migrate_from_json_if_needed(&self, data_dir: &Path) -> anyhow::Result<()> {
        let existing: Vec<DownloadRecord> = self.db.select("downloads").await?;
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

        // Scan for download item JSON files
        let mut migrated = 0u32;
        if let Ok(entries) = std::fs::read_dir(&json_dir) {
            for entry in entries.flatten() {
                let path = entry.path();
                if path.extension().is_some_and(|ext| ext == "json") {
                    match std::fs::read_to_string(&path) {
                        Ok(json_str) => {
                            // Attempt to parse as a DownloadItem
                            match serde_json::from_str::<DownloadItemData>(&json_str) {
                                Ok(data) => {
                                    let item: DownloadItem = data.into();
                                    if let Err(e) = self.add(&item).await {
                                        warn!("Failed to migrate {}: {}", path.display(), e);
                                    } else {
                                        migrated += 1;
                                    }
                                }
                                Err(e) => {
                                    debug!(
                                        "Skipping non-download JSON file {}: {}",
                                        path.display(),
                                        e
                                    );
                                }
                            }
                        }
                        Err(e) => {
                            warn!("Failed to read {}: {}", path.display(), e);
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
