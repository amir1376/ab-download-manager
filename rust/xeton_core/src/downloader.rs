use std::path::{Path, PathBuf};
use std::sync::Arc;
use tokio::sync::Mutex;
use tracing::error;

use crate::manager::DownloadManager;
use crate::models::{DownloadStatus, DownloadSettings, NewDownloadProps};

/// Low-level Downloader exposed via FFI.
/// This acts as a lightweight single-file API that wraps the full Xeton engine.
#[derive(uniffi::Object)]
pub struct Downloader {
    manager: Arc<DownloadManager>,
    item_id: Mutex<Option<i64>>,
    url: String,
    output_path: String,
    segments: u16,
}

#[uniffi::export]
impl Downloader {
    #[uniffi::constructor]
    pub fn new(url: String, output_path: String, segments: u16) -> Arc<Self> {
        let rt = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();

        // Create a temporary data dir for the standalone downloader
        let data_dir = std::env::temp_dir().join("xeton_standalone");
        let mut settings = DownloadSettings::default();
        settings.default_thread_count = segments as u32;

        let manager = rt.block_on(DownloadManager::new(data_dir, settings)).unwrap();

        Arc::new(Self {
            manager,
            item_id: Mutex::new(None),
            url,
            output_path,
            segments,
        })
    }

    pub async fn start(&self) {
        let mut id_guard = self.item_id.lock().await;
        if id_guard.is_none() {
            let path = Path::new(&self.output_path);
            let folder = path.parent().unwrap_or_else(|| Path::new("")).to_string_lossy().to_string();
            let name = path.file_name().unwrap_or_default().to_string_lossy().to_string();

            let props = NewDownloadProps {
                link: self.url.clone(),
                folder,
                name,
                protocol: crate::models::DownloadProtocol::Http,
                preferred_connections: Some(self.segments as u32),
                speed_limit: 0,
                on_duplicate: crate::models::DuplicateStrategy::AddNumbered,
            };

            match self.manager.add_download(props).await {
                Ok(id) => *id_guard = Some(id),
                Err(e) => {
                    error!("Failed to start download: {}", e);
                    return;
                }
            }
        }

        if let Some(id) = *id_guard {
            let _ = self.manager.resume(id).await;
        }
    }

    pub async fn pause(&self) {
        let id_guard = self.item_id.lock().await;
        if let Some(id) = *id_guard {
            let _ = self.manager.pause(id).await;
        }
    }

    pub fn get_status(&self) -> DownloadStatus {
        let rt = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();

        rt.block_on(async {
            let id_guard = self.item_id.lock().await;
            if let Some(id) = *id_guard {
                if let Ok(list) = self.manager.get_download_list().await {
                    if let Some(item) = list.into_iter().find(|i| i.numeric_id == id) {
                        return item.status;
                    }
                }
            }
            DownloadStatus::Added
        })
    }
}
