use std::sync::Arc;
use tokio::sync::RwLock;

use crate::models::DownloadStatus;

/// Low-level Downloader exposed via FFI to replace Kotlin's OkHttp calls.
#[derive(uniffi::Object)]
pub struct Downloader {
    url: String,
    output_path: String,
    segments: u16,
    status: Arc<RwLock<DownloadStatus>>,
}

#[uniffi::export]
impl Downloader {
    #[uniffi::constructor]
    pub fn new(url: String, output_path: String, segments: u16) -> Arc<Self> {
        Arc::new(Self {
            url,
            output_path,
            segments,
            status: Arc::new(RwLock::new(DownloadStatus::Added)),
        })
    }

    pub async fn start(&self) {
        // Here we would wire up the actual download process with reqwest and tokio.
        // For now, we update the status to indicate the start.
        let mut s = self.status.write().await;
        *s = DownloadStatus::Downloading;
        
        // TODO: Implement dynamic re-segmenting logic here
    }

    pub async fn pause(&self) {
        let mut s = self.status.write().await;
        *s = DownloadStatus::Paused;
    }

    pub fn get_status(&self) -> DownloadStatus {
        // Note: For UniFFI, synchronous functions cannot easily read from an async RwLock without blocking.
        // We will do a try_read or use a standard Mutex for status if it needs to be synchronous.
        // Since get_status isn't marked [Async] in UDL, we will use a blocking read.
        self.status.blocking_read().clone()
    }
}
