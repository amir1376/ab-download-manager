// xeton_core::queue — Download queue and queue manager.
//
// Port of `ir.amirab.downloader.queue.DownloadQueue` and `QueueManager`.

pub mod manager;

use std::collections::HashSet;
use std::sync::Arc;

use tokio::sync::{broadcast, watch, Mutex, RwLock};
use tokio::task::JoinHandle;
use tokio::time::{sleep, Duration};
use tracing::{debug, info, warn};

use crate::db::QueueDb;
use crate::models::{ManagerEvent, QueueModel, ScheduleTimes};

/// A download queue that manages concurrent download scheduling.
///
/// Mirrors `ir.amirab.downloader.queue.DownloadQueue`.
/// Uses the "shake" pattern: when state changes, the queue re-evaluates
/// which items should be active and starts/stops them accordingly.
pub struct Queue {
    pub model: Arc<RwLock<QueueModel>>,
    active_ids: Mutex<HashSet<i64>>,
    canceled_ids: Mutex<HashSet<i64>>,
    trimmed_ids: Mutex<HashSet<i64>>,
    active_flow: watch::Sender<bool>,
    pub active_rx: watch::Receiver<bool>,
    /// Channel to send start/stop commands to the manager.
    command_tx: broadcast::Sender<QueueCommand>,
    pub command_rx: broadcast::Receiver<QueueCommand>,
    /// Queue DB for persistence.
    queue_db: Arc<dyn QueueDb>,
    /// Debounce handle.
    shake_handle: Mutex<Option<JoinHandle<()>>>,
    stopping: Mutex<bool>,
}

/// Commands emitted by the queue to the download manager.
#[derive(Clone, Debug)]
pub enum QueueCommand {
    StartJob { id: i64, queue_id: i64 },
    StopJob { id: i64, queue_id: i64 },
}

/// Events that can occur on a queue.
#[derive(Clone, Debug)]
pub enum QueueEvent {
    QueueBecomesEmpty { queue_id: i64 },
    QueueStartTimeReached { queue_id: i64, was_active: bool },
    QueueEndTimeReached { queue_id: i64, was_active: bool },
}

impl Queue {
    pub fn new(model: QueueModel, queue_db: Arc<dyn QueueDb>) -> Arc<Self> {
        let (active_flow, active_rx) = watch::channel(false);
        let (command_tx, command_rx) = broadcast::channel(128);

        Arc::new(Self {
            model: Arc::new(RwLock::new(model)),
            active_ids: Mutex::new(HashSet::new()),
            canceled_ids: Mutex::new(HashSet::new()),
            trimmed_ids: Mutex::new(HashSet::new()),
            active_flow,
            active_rx,
            command_tx,
            command_rx,
            queue_db,
            shake_handle: Mutex::new(None),
            stopping: Mutex::new(false),
        })
    }

    pub async fn id(&self) -> i64 {
        self.model.read().await.id
    }

    pub async fn is_active(&self) -> bool {
        *self.active_rx.borrow()
    }

    /// Start the queue — begin scheduling downloads.
    pub async fn start(self: &Arc<Self>) {
        if *self.stopping.lock().await {
            return;
        }
        self.canceled_ids.lock().await.clear();
        self.trimmed_ids.lock().await.clear();
        let _ = self.active_flow.send(true);
        self.shake(false).await;
    }

    /// Stop the queue — pause all active downloads.
    pub async fn stop(&self) {
        let mut stopping = self.stopping.lock().await;
        if *stopping {
            return;
        }
        *stopping = true;
        let _ = self.active_flow.send(false);

        let active_ids: Vec<i64> = self.active_ids.lock().await.iter().copied().collect();
        let queue_id = self.id().await;
        for id in active_ids {
            let _ = self.command_tx.send(QueueCommand::StopJob {
                id,
                queue_id,
            });
        }

        *stopping = false;
    }

    /// Notify the queue that a download was canceled.
    pub async fn on_download_canceled(&self, id: i64) {
        let removed = self.active_ids.lock().await.remove(&id);
        if self.is_active().await {
            let was_trimmed = self.trimmed_ids.lock().await.remove(&id);
            if !was_trimmed {
                self.canceled_ids.lock().await.insert(id);
            }
        }
        if removed {
            self.shake(true).await;
        }
    }

    /// Notify the queue that a download completed.
    pub async fn on_download_finished(&self, id: i64) {
        self.remove_from_queue(&[id]).await;
        self.shake(true).await;
    }

    /// Notify the queue that a download was removed.
    pub async fn on_download_removed(&self, id: i64) {
        self.remove_from_queue(&[id]).await;
    }

    /// The "shake" operation: re-evaluate queue state and start/stop downloads.
    /// Uses debouncing (500ms) to batch rapid state changes.
    async fn shake(&self, delayed: bool) {
        if delayed {
            // Cancel any pending shake
            if let Some(handle) = self.shake_handle.lock().await.take() {
                handle.abort();
            }

            let model = self.model.clone();
            let active_ids = self.active_ids.clone();
            let canceled_ids = self.canceled_ids.clone();
            let active_flow = self.active_rx.clone();
            let command_tx = self.command_tx.clone();
            let queue_db = self.queue_db.clone();

            // Dummy reference — actual debounce logic inlined
            let handle = tokio::spawn(async move {
                sleep(Duration::from_millis(500)).await;
                Self::actual_shake_static(
                    &model, &active_ids, &canceled_ids, &active_flow, &command_tx,
                )
                .await;
            });
            *self.shake_handle.lock().await = Some(handle);
        } else {
            Self::actual_shake_static(
                &self.model,
                &self.active_ids,
                &self.canceled_ids,
                &self.active_rx,
                &self.command_tx,
            )
            .await;
        }
    }

    async fn actual_shake_static(
        model: &Arc<RwLock<QueueModel>>,
        active_ids: &Mutex<HashSet<i64>>,
        canceled_ids: &Mutex<HashSet<i64>>,
        active_flow: &watch::Receiver<bool>,
        command_tx: &broadcast::Sender<QueueCommand>,
    ) {
        let is_active = *active_flow.borrow();
        if !is_active {
            return;
        }

        let m = model.read().await;
        let queue_id = m.id;
        let max = m.max_concurrent;
        let queue_items = m.queue_items.clone();
        drop(m);

        let mut active = active_ids.lock().await;
        let canceled = canceled_ids.lock().await;

        let active_count = active.len();
        if active_count < max {
            // Start more downloads
            let to_start = max - active_count;
            let mut started = 0;
            for &item_id in &queue_items {
                if started >= to_start {
                    break;
                }
                if active.contains(&item_id) || canceled.contains(&item_id) {
                    continue;
                }
                active.insert(item_id);
                let _ = command_tx.send(QueueCommand::StartJob {
                    id: item_id,
                    queue_id,
                });
                started += 1;
            }
        } else if active_count > max {
            // Stop excess downloads
            let to_stop = active_count - max;
            let stop_ids: Vec<i64> = active.iter().rev().take(to_stop).copied().collect();
            for id in stop_ids {
                active.remove(&id);
                let _ = command_tx.send(QueueCommand::StopJob {
                    id,
                    queue_id,
                });
            }
        }
    }

    /// Add an item to the queue.
    pub async fn add_to_queue(&self, item_id: i64) {
        let mut m = self.model.write().await;
        if !m.queue_items.contains(&item_id) {
            m.queue_items.push(item_id);
        }
        drop(m);
        self.persist().await;
        self.shake(true).await;
    }

    /// Remove items from the queue.
    pub async fn remove_from_queue(&self, ids: &[i64]) {
        let mut m = self.model.write().await;
        m.queue_items.retain(|id| !ids.contains(id));
        drop(m);

        let mut active = self.active_ids.lock().await;
        let mut canceled = self.canceled_ids.lock().await;
        let mut trimmed = self.trimmed_ids.lock().await;
        for id in ids {
            active.remove(id);
            canceled.remove(id);
            trimmed.remove(id);
        }

        self.persist().await;
    }

    /// Set the maximum concurrent downloads.
    pub async fn set_max_concurrent(&self, value: usize) {
        let mut m = self.model.write().await;
        m.max_concurrent = value;
        drop(m);
        self.persist().await;
        self.shake(false).await;
    }

    /// Set the queue name.
    pub async fn set_name(&self, name: String) {
        let mut m = self.model.write().await;
        m.name = name;
        drop(m);
        self.persist().await;
    }

    /// Persist queue model to the database.
    async fn persist(&self) {
        let m = self.model.read().await;
        if let Err(e) = self.queue_db.set_queue(&m).await {
            warn!("Failed to persist queue: {}", e);
        }
    }

    /// Move items up in the queue.
    pub async fn move_up(&self, ids: &[i64]) {
        self.move_items(ids, -1).await;
    }

    /// Move items down in the queue.
    pub async fn move_down(&self, ids: &[i64]) {
        self.move_items(ids, 1).await;
    }

    async fn move_items(&self, ids: &[i64], diff: i32) {
        if diff == 0 {
            return;
        }
        let mut m = self.model.write().await;
        let items = &mut m.queue_items;
        let len = items.len();

        let mut indices: Vec<usize> = ids
            .iter()
            .filter_map(|id| items.iter().position(|x| x == id))
            .collect();

        if diff < 0 {
            indices.sort();
        } else {
            indices.sort_by(|a, b| b.cmp(a));
        }

        let mut blocked = HashSet::new();
        for idx in indices {
            let new_pos = (idx as i32 + diff) as usize;
            if new_pos >= len || blocked.contains(&new_pos) {
                blocked.insert(idx);
                continue;
            }
            items.swap(idx, new_pos);
        }

        drop(m);
        self.persist().await;
    }
}
