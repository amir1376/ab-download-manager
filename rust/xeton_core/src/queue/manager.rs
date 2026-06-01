// xeton_core::queue::manager — Queue manager coordinating multiple queues.
//
// Port of `ir.amirab.downloader.queue.QueueManager`.

use std::sync::Arc;

use dashmap::DashMap;
use tracing::info;

use crate::db::QueueDb;
use crate::models::QueueModel;
use super::Queue;

/// Manages all download queues.
///
/// Mirrors `ir.amirab.downloader.queue.QueueManager`.
pub struct QueueManager {
    queues: DashMap<i64, Arc<Queue>>,
    queue_db: Arc<dyn QueueDb>,
}

impl QueueManager {
    pub fn new(queue_db: Arc<dyn QueueDb>) -> Self {
        Self {
            queues: DashMap::new(),
            queue_db,
        }
    }

    /// Boot: load all persisted queues from the database.
    pub async fn boot(&self) -> anyhow::Result<()> {
        let models = self.queue_db.get_all_queues().await?;
        for model in models {
            let queue = Queue::new(model, self.queue_db.clone());
            self.queues.insert(queue.id().await, queue);
        }
        info!("QueueManager booted with {} queues", self.queues.len());
        Ok(())
    }

    /// Create a new queue with the given model.
    pub async fn create_queue(&self, model: QueueModel) -> anyhow::Result<Arc<Queue>> {
        self.queue_db.set_queue(&model).await?;
        let queue = Queue::new(model, self.queue_db.clone());
        let id = queue.id().await;
        self.queues.insert(id, queue.clone());
        info!("Created queue #{}", id);
        Ok(queue)
    }

    /// Remove a queue by ID.
    pub async fn remove_queue(&self, id: i64) -> anyhow::Result<()> {
        if let Some((_, queue)) = self.queues.remove(&id) {
            queue.stop().await;
        }
        self.queue_db.remove_queue(id).await?;
        info!("Removed queue #{}", id);
        Ok(())
    }

    /// Get a queue by ID.
    pub fn get_queue(&self, id: i64) -> Option<Arc<Queue>> {
        self.queues.get(&id).map(|entry| entry.value().clone())
    }

    /// Get all queues.
    pub fn all_queues(&self) -> Vec<Arc<Queue>> {
        self.queues.iter().map(|entry| entry.value().clone()).collect()
    }

    /// Notify all queues about a download event.
    pub async fn on_download_canceled(&self, download_id: i64) {
        for entry in self.queues.iter() {
            entry.value().on_download_canceled(download_id).await;
        }
    }

    pub async fn on_download_finished(&self, download_id: i64) {
        for entry in self.queues.iter() {
            entry.value().on_download_finished(download_id).await;
        }
    }

    pub async fn on_download_removed(&self, download_id: i64) {
        for entry in self.queues.iter() {
            entry.value().on_download_removed(download_id).await;
        }
    }
}
