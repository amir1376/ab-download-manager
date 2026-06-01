// xeton_core::db::progress_batcher — Debounced database writer for download progress.
//
// Prevents database write-bottlenecks during high-speed downloads by debouncing
// progress updates (DownloadItem and RangedParts) using a channel-based aggregator.

use std::collections::HashMap;
use std::sync::Arc;
use std::time::Duration;
use tokio::sync::mpsc;
use tokio::time::sleep;
use crate::models::{DownloadItem, RangedPart};
use crate::db::{DownloadDb, PartDb, SurrealStore};

pub enum BatchUpdate {
    Item(DownloadItem),
    Parts { download_id: i64, parts: Vec<RangedPart> },
}

#[derive(Clone)]
pub struct ProgressBatcher {
    tx: mpsc::Sender<BatchUpdate>,
}

impl ProgressBatcher {
    /// Create a new progress batcher with the specified flush interval.
    pub fn new(store: Arc<SurrealStore>, flush_interval: Duration) -> Self {
        let (tx, mut rx) = mpsc::channel(1024);

        tokio::spawn(async move {
            let mut items: HashMap<i64, DownloadItem> = HashMap::new();
            let mut parts_map: HashMap<i64, Vec<RangedPart>> = HashMap::new();

            loop {
                // Determine if we have any pending updates to flush
                let has_pending = !items.is_empty() || !parts_map.is_empty();

                let update = if has_pending {
                    tokio::select! {
                        val = rx.recv() => val,
                        _ = sleep(flush_interval) => {
                            // Flush timeout reached, trigger flush by yielding None
                            None
                        }
                    }
                } else {
                    // Block indefinitely until we receive the first update
                    rx.recv().await
                };

                match update {
                    Some(BatchUpdate::Item(item)) => {
                        items.insert(item.numeric_id, item);
                    }
                    Some(BatchUpdate::Parts { download_id, parts }) => {
                        parts_map.insert(download_id, parts);
                    }
                    None => {
                        // Flush all accumulated updates
                        for (_, item) in items.drain() {
                            if let Err(e) = store.update(&item).await {
                                tracing::error!("Failed to flush item {} progress: {}", item.numeric_id, e);
                            }
                        }
                        for (download_id, parts) in parts_map.drain() {
                            if let Err(e) = store.set_parts(download_id, &parts).await {
                                tracing::error!("Failed to flush parts for job {}: {}", download_id, e);
                            }
                        }
                    }
                }
            }
        });

        Self { tx }
    }

    /// Queue a DownloadItem progress update.
    pub async fn update_item(&self, item: DownloadItem) -> anyhow::Result<()> {
        self.tx.send(BatchUpdate::Item(item))
            .await
            .map_err(|_| anyhow::anyhow!("ProgressBatcher channel closed"))
    }

    /// Queue a parts list progress update.
    pub async fn update_parts(&self, download_id: i64, parts: Vec<RangedPart>) -> anyhow::Result<()> {
        self.tx.send(BatchUpdate::Parts { download_id, parts })
            .await
            .map_err(|_| anyhow::anyhow!("ProgressBatcher channel closed"))
    }
}
