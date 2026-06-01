// xeton_core::db::progress_batcher — Debounced database writer for download progress.
//
// Prevents database write-bottlenecks during high-speed downloads by debouncing
// progress updates (DownloadItem, RangedParts, and BlockUpdates) using a channel-based aggregator.

use std::collections::HashMap;
use std::sync::Arc;
use std::time::Duration;
use tokio::sync::mpsc;
use tokio::time::sleep;
use crate::models::{DownloadItem, RangedPart};
use crate::db::{DownloadDb, PartDb, SurrealStore};

#[derive(Clone, Debug)]
pub struct BlockUpdate {
    pub task_id: i64,
    pub offset: u64,
    pub bytes_written: u64,
}

pub enum BatchUpdate {
    Item(DownloadItem),
    Parts { download_id: i64, parts: Vec<RangedPart> },
    Block(BlockUpdate),
}

#[derive(Clone)]
pub struct ProgressBatcher {
    tx: mpsc::Sender<BatchUpdate>,
}

impl ProgressBatcher {
    /// Create a new progress batcher with the specified flush interval.
    pub fn new(store: Arc<SurrealStore>, flush_interval: Duration) -> Self {
        let (tx, mut rx) = mpsc::channel(2048);

        tokio::spawn(async move {
            let mut items: HashMap<i64, DownloadItem> = HashMap::new();
            let mut parts_map: HashMap<i64, Vec<RangedPart>> = HashMap::new();
            let mut block_updates: HashMap<i64, Vec<BlockUpdate>> = HashMap::new();

            loop {
                let has_pending = !items.is_empty() || !parts_map.is_empty() || !block_updates.is_empty();

                let update = if has_pending {
                    tokio::select! {
                        val = rx.recv() => val,
                        _ = sleep(flush_interval) => {
                            None
                        }
                    }
                } else {
                    rx.recv().await
                };

                match update {
                    Some(BatchUpdate::Item(item)) => {
                        items.insert(item.numeric_id, item);
                    }
                    Some(BatchUpdate::Parts { download_id, parts }) => {
                        parts_map.insert(download_id, parts);
                    }
                    Some(BatchUpdate::Block(block)) => {
                        block_updates.entry(block.task_id).or_default().push(block);
                    }
                    None => {
                        // Flush items
                        for (_, item) in items.drain() {
                            if let Err(e) = store.update(&item).await {
                                tracing::error!("Failed to flush item {} progress: {}", item.numeric_id, e);
                            }
                        }
                        
                        // Apply block updates to parts first to preserve recent progress
                        for (task_id, updates) in block_updates.drain() {
                            // Fetch current parts from database or local map
                            let mut parts = if let Some(p) = parts_map.remove(&task_id) {
                                p
                            } else {
                                match store.get_parts(task_id).await {
                                    Ok(p) => p,
                                    Err(e) => {
                                        tracing::error!("Failed to load parts for task {} during block update flush: {}", task_id, e);
                                        continue;
                                    }
                                }
                            };

                            for update in updates {
                                if let Some(part) = parts.iter_mut().find(|p| {
                                    let from = p.from;
                                    let to = p.to.unwrap_or(std::i64::MAX);
                                    (update.offset as i64) >= from && (update.offset as i64) < to
                                }) {
                                    let new_current = (update.offset + update.bytes_written) as i64;
                                    if new_current > part.current {
                                        part.current = new_current;
                                    }
                                }
                            }

                            if let Err(e) = store.set_parts(task_id, &parts).await {
                                tracing::error!("Failed to save parts for task {} after block updates: {}", task_id, e);
                            }
                        }

                        // Flush remaining parts updates
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

    /// Queue a block write completion update.
    pub async fn update_block(&self, update: BlockUpdate) -> anyhow::Result<()> {
        self.tx.send(BatchUpdate::Block(update))
            .await
            .map_err(|_| anyhow::anyhow!("ProgressBatcher channel closed"))
    }
}
