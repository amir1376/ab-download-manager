// xeton_core::protocols::torrent — BitTorrent download engine.
//
// Uses `librqbit` — a production-quality, Tokio-native BitTorrent client.
// Features: rarest-first piece selection, endgame mode, DHT, tracker support.

use std::path::PathBuf;
use std::sync::Arc;

use tokio::sync::{watch, RwLock};
use tracing::{debug, info};

use crate::db::DownloadDb;
use crate::models::*;

/// BitTorrent download engine wrapping `librqbit`.
///
/// Architecture:
/// - `librqbit::Session` manages all peer connections, piece scheduling,
///   tracker announces, and DHT.
/// - Rarest-first piece selection and endgame mode are built into librqbit.
/// - Torrent progress is mapped to `DownloadItem.content_length` and reported
///   through the standard job status channel.
pub struct TorrentJob {
    pub item: Arc<RwLock<DownloadItem>>,
    dl_db: Arc<dyn DownloadDb>,
    settings: Arc<RwLock<DownloadSettings>>,
    status_tx: watch::Sender<JobStatus>,
    pub status_rx: watch::Receiver<JobStatus>,
    data_dir: PathBuf,
    /// librqbit session handle.
    session: RwLock<Option<Arc<librqbit::Session>>>,
    /// Active torrent handle index.
    torrent_handle: RwLock<Option<Arc<librqbit::ManagedTorrent>>>,
}

impl TorrentJob {
    pub fn new(
        item: DownloadItem,
        settings: Arc<RwLock<DownloadSettings>>,
        dl_db: Arc<dyn DownloadDb>,
        data_dir: PathBuf,
    ) -> Arc<Self> {
        let (status_tx, status_rx) = watch::channel(JobStatus::Idle);
        Arc::new(Self {
            item: Arc::new(RwLock::new(item)),
            dl_db,
            settings,
            status_tx,
            status_rx,
            data_dir,
            session: RwLock::new(None),
            torrent_handle: RwLock::new(None),
        })
    }

    /// Boot the torrent job.
    pub async fn boot(&self) -> anyhow::Result<()> {
        debug!("Booting torrent job");
        Ok(())
    }

    /// Resume (start) the torrent download.
    pub async fn resume(self: &Arc<Self>) -> anyhow::Result<()> {
        let item = self.item.read().await;
        let id = item.numeric_id;
        let link = item.link.clone();
        let output_dir = PathBuf::from(&item.folder);
        drop(item);

        info!("Resuming torrent job #{}", id);
        let _ = self.status_tx.send(JobStatus::Resuming);

        // Create librqbit session
        let session = librqbit::Session::new_with_opts(
            output_dir,
            librqbit::SessionOptions {
                disable_dht: false,
                ..Default::default()
            },
        )
        .await
        .map_err(|e| anyhow::anyhow!("Failed to create torrent session: {}", e))?;

        // Add torrent (from magnet link, URL, or file path)
        let add_opts = librqbit::AddTorrentOptions::default();
        let add_result = if link.starts_with("magnet:") {
            session
                .add_torrent(
                    librqbit::AddTorrent::from_url(&link),
                    Some(add_opts),
                )
                .await
        } else if link.ends_with(".torrent") || link.starts_with("http") {
            session
                .add_torrent(
                    librqbit::AddTorrent::from_url(&link),
                    Some(add_opts),
                )
                .await
        } else {
            // Try as file path
            let data = tokio::fs::read(&link).await?;
            session
                .add_torrent(
                    librqbit::AddTorrent::from_bytes(data),
                    Some(add_opts),
                )
                .await
        };

        let handle = add_result
            .map_err(|e| anyhow::anyhow!("Failed to add torrent: {}", e))?
            .into_handle()
            .ok_or_else(|| anyhow::anyhow!("Torrent handle not available (listing only?)"))?;

        // Update item with torrent metadata
        {
            let mut item = self.item.write().await;
            if let Some(total_bytes) = handle.metadata.load().as_ref().map(|r| r.lengths.total_length()) {
                item.content_length = total_bytes as i64;
            }
            item.status = DownloadStatus::Downloading;
            item.start_time = Some(
                std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_millis() as i64,
            );
            self.dl_db.update(&item).await?;
        }

        let _ = self.status_tx.send(JobStatus::Downloading);

        *self.torrent_handle.write().await = Some(handle);
        *self.session.write().await = Some(session);

        // Monitor progress in a background task
        let job = self.clone();
        tokio::spawn(async move {
            job.monitor_progress().await;
        });

        Ok(())
    }

    /// Monitor torrent download progress.
    async fn monitor_progress(&self) {
        loop {
            tokio::time::sleep(tokio::time::Duration::from_secs(1)).await;

            let handle_guard = self.torrent_handle.read().await;
            let handle = match handle_guard.as_ref() {
                Some(h) => h,
                None => break,
            };

            let stats = handle.stats();
            let _progress_bytes = stats.progress_bytes;
            let total_bytes = stats.total_bytes;

            // Update item
            {
                let mut item = self.item.write().await;
                item.content_length = total_bytes as i64;
            }

            // Check completion
            if stats.finished {
                let mut item = self.item.write().await;
                item.status = DownloadStatus::Completed;
                item.complete_time = Some(
                    std::time::SystemTime::now()
                        .duration_since(std::time::UNIX_EPOCH)
                        .unwrap_or_default()
                        .as_millis() as i64,
                );
                let _ = self.dl_db.update(&item).await;
                let _ = self.status_tx.send(JobStatus::Finished);
                info!(
                    "Torrent job #{} completed ({} bytes)",
                    item.numeric_id, total_bytes
                );
                break;
            }
        }
    }

    /// Pause the torrent.
    pub async fn pause(&self) -> anyhow::Result<()> {
        if let Some(handle) = self.torrent_handle.write().await.take() {
            if let Some(session) = self.session.read().await.as_ref() {
                let _ = session.pause(&handle).await;
            }
        }
        let _ = self.status_tx.send(JobStatus::Canceled {
            reason: "paused".into(),
        });
        Ok(())
    }
}
