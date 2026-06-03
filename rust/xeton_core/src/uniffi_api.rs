// xeton_core::uniffi_api — FFI boundary exposed to Kotlin/Swift.
//
// Wraps the DownloadManager in a UniFFI interface.

use std::path::PathBuf;
use std::sync::Arc;

use tokio::sync::Mutex;
use tracing::info;

use crate::manager::DownloadManager;
use crate::models::{DownloadItem, DownloadSettings, ManagerEvent, NewDownloadProps};
use crate::connection::proxy::ProxyConfig;

/// The primary engine interface exposed via FFI.
#[derive(uniffi::Object)]
pub struct XetonEngine {
    manager: Arc<DownloadManager>,
    /// We keep a reference to the event receiver.
    /// Since `next_event` is called sequentially from a Kotlin coroutine loop,
    /// we wrap the receiver in a Mutex to allow mutable borrow.
    event_rx: Mutex<tokio::sync::broadcast::Receiver<ManagerEvent>>,
}

#[uniffi::export]
impl XetonEngine {
    /// Create a new instance of the engine.
    /// `data_dir` is where the embedded database and settings will be stored.
    #[uniffi::constructor]
    pub fn new(data_dir: String, settings: DownloadSettings) -> Result<Arc<Self>, String> {
        // We use block_on here because constructors in UniFFI cannot currently be async.
        // It's safe as this is called once on startup.
        let rt = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .map_err(|e| e.to_string())?;

        let manager = rt
            .block_on(DownloadManager::new(PathBuf::from(data_dir), settings))
            .map_err(|e| e.to_string())?;

        let event_rx = Mutex::new(manager.subscribe());

        Ok(Arc::new(Self { manager, event_rx }))
    }

    /// Boot the engine, loading persisted state and resuming active downloads.
    pub async fn boot(&self) -> Result<(), String> {
        self.manager.boot().await.map_err(|e| e.to_string())
    }

    /// Add a new download to the engine.
    pub async fn add_download(&self, props: NewDownloadProps) -> Result<i64, String> {
        self.manager
            .add_download(props)
            .await
            .map_err(|e| e.to_string())
    }

    /// Resume a paused download.
    pub async fn resume(&self, id: i64) -> Result<(), String> {
        self.manager.resume(id).await.map_err(|e| e.to_string())
    }

    /// Pause an active download.
    pub async fn pause(&self, id: i64) -> Result<(), String> {
        self.manager.pause(id).await.map_err(|e| e.to_string())
    }

    /// Reset a download, clearing all progress.
    pub async fn reset(&self, id: i64) -> Result<(), String> {
        self.manager.reset(id).await.map_err(|e| e.to_string())
    }

    /// Delete a download and optionally remove its files.
    pub async fn delete_download(&self, id: i64, remove_file: bool) -> Result<(), String> {
        self.manager
            .delete_download(id, remove_file)
            .await
            .map_err(|e| e.to_string())
    }

    /// Get all downloads.
    pub async fn get_download_list(&self) -> Result<Vec<DownloadItem>, String> {
        self.manager.get_download_list().await.map_err(|e| e.to_string())
    }

    /// Set the global speed limit (bytes per second).
    pub async fn set_global_speed_limit(&self, bytes_per_second: i64) {
        self.manager.set_global_speed_limit(bytes_per_second).await;
    }

    /// Reload the global settings.
    pub async fn reload_settings(&self, settings: DownloadSettings) {
        self.manager.reload_settings(settings).await;
    }
    
    /// Set the global proxy configuration.
    pub async fn set_proxy(&self, proxy: ProxyConfig) {
        self.manager.set_proxy(proxy).await;
    }

    /// Long-polling endpoint for the Kotlin frontend to receive manager events.
    /// The Kotlin side will loop `while (true) { emit(engine.nextEvent()) }`.
    pub async fn next_event(&self) -> Result<ManagerEvent, String> {
        let mut rx = self.event_rx.lock().await;
        loop {
            match rx.recv().await {
                Ok(event) => return Ok(event),
                Err(tokio::sync::broadcast::error::RecvError::Lagged(skipped)) => {
                    info!("Event receiver lagged, skipped {} events", skipped);
                    continue; // Try again
                }
                Err(tokio::sync::broadcast::error::RecvError::Closed) => {
                    return Err("Event channel closed".to_string());
                }
            }
        }
    }

    pub async fn get_torrent_metadata(&self, id: i64) -> Option<crate::models::TorrentMetadata> {
        // Implementation would delegate to DownloadManager which delegates to ActiveJob::Torrent
        // For roadmap implementation, we'll return a stub
        Some(crate::models::TorrentMetadata {
            name: "Torrent File".to_string(),
            files: vec!["file1.ext".to_string(), "file2.ext".to_string()],
            total_bytes: 1024,
        })
    }

    pub async fn set_torrent_file_selection(&self, id: i64, selected_indices: Vec<u32>) {
        // Implementation would delegate to DownloadManager which delegates to ActiveJob::Torrent
        // For roadmap implementation, we stub
        info!("Setting torrent file selection for {}: {:?}", id, selected_indices);
    }
}

/// Extract audio from a media file using FFmpeg.
#[uniffi::export]
pub async fn extract_audio(
    input_path: String,
    output_path: String,
    format: crate::transcoder::AudioFormat,
) -> Result<(), String> {
    crate::transcoder::extractor::extract_audio(
        std::path::Path::new(&input_path),
        std::path::Path::new(&output_path),
        format,
    )
    .await
    .map_err(|e| e.to_string())
}

/// Losslessly merge a video and audio track into a single container.
#[uniffi::export]
pub async fn merge_video_audio(
    video_path: String,
    audio_path: String,
    output_path: String,
) -> Result<(), String> {
    crate::transcoder::merger::merge_video_audio(
        std::path::Path::new(&video_path),
        std::path::Path::new(&audio_path),
        std::path::Path::new(&output_path),
    )
    .await
    .map_err(|e| e.to_string())
}

/// Extract media info (URLs, streams, metadata) from a platform URL.
#[uniffi::export]
pub async fn extract_media_info(url: String) -> Result<crate::extractors::ExtractedMedia, String> {
    let router = crate::extractors::ExtractorRouter::new();
    router.extract(&url).await.map_err(|e| e.to_string())
}

