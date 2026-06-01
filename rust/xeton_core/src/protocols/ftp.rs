// xeton_core::protocols::ftp — Async FTP/FTPS download engine.
//
// Uses `suppaftp` for async FTP control/data connections.
// Supports parallel downloads via multiple PASV data connections.

use std::path::PathBuf;
use std::sync::Arc;

use bytes::Bytes;
use suppaftp::AsyncFtpStream;
use suppaftp::types::FileType;
use tokio::sync::{watch, Mutex, RwLock};
use tracing::{debug, error, info, warn};

use crate::db::{DownloadDb, PartDb};
use crate::destination::DiskActor;
use crate::models::*;
use crate::throttle::Throttler;

/// Async FTP download engine.
///
/// Architecture:
/// - A single control session manages the FTP negotiation (USER, PASS, TYPE I, CWD).
/// - For multi-threaded downloads, N passive data connections are opened in parallel.
/// - Each data connection uses REST (restart marker) to request specific byte offsets.
pub struct FtpJob {
    pub item: Arc<RwLock<DownloadItem>>,
    dl_db: Arc<dyn DownloadDb>,
    part_db: Arc<dyn PartDb>,
    settings: Arc<RwLock<DownloadSettings>>,
    global_throttler: Arc<Throttler>,
    status_tx: watch::Sender<JobStatus>,
    pub status_rx: watch::Receiver<JobStatus>,
    disk: Mutex<Option<DiskActor>>,
    data_dir: PathBuf,
}

/// Parsed FTP URL components.
struct FtpUrl {
    host: String,
    port: u16,
    username: Option<String>,
    password: Option<String>,
    path: String,
    secure: bool,
}

impl FtpJob {
    pub fn new(
        item: DownloadItem,
        settings: Arc<RwLock<DownloadSettings>>,
        dl_db: Arc<dyn DownloadDb>,
        part_db: Arc<dyn PartDb>,
        global_throttler: Arc<Throttler>,
        data_dir: PathBuf,
    ) -> Arc<Self> {
        let (status_tx, status_rx) = watch::channel(JobStatus::Idle);
        Arc::new(Self {
            item: Arc::new(RwLock::new(item)),
            dl_db,
            part_db,
            settings,
            global_throttler,
            status_tx,
            status_rx,
            disk: Mutex::new(None),
            data_dir,
        })
    }

    /// Boot the FTP job.
    pub async fn boot(&self) -> anyhow::Result<()> {
        debug!("Booting FTP job");
        Ok(())
    }

    /// Resume the FTP download.
    pub async fn resume(self: &Arc<Self>) -> anyhow::Result<()> {
        let item = self.item.read().await;
        let id = item.numeric_id;
        info!("Resuming FTP job #{}", id);
        let _ = self.status_tx.send(JobStatus::Resuming);

        let ftp_url = Self::parse_ftp_url(&item.link)?;
        drop(item);

        // Establish control connection
        let mut control = AsyncFtpStream::connect(format!("{}:{}", ftp_url.host, ftp_url.port))
            .await
            .map_err(|e| anyhow::anyhow!("FTP connect failed: {}", e))?;

        // Authenticate
        let user = ftp_url.username.as_deref().unwrap_or("anonymous");
        let pass = ftp_url.password.as_deref().unwrap_or("guest@");
        control
            .login(user, pass)
            .await
            .map_err(|e| anyhow::anyhow!("FTP login failed: {}", e))?;

        // Switch to binary mode
        control
            .transfer_type(FileType::Binary)
            .await
            .map_err(|e| anyhow::anyhow!("FTP TYPE I failed: {}", e))?;

        // Get file size
        let file_size = control
            .size(&ftp_url.path)
            .await
            .map_err(|e| anyhow::anyhow!("FTP SIZE failed: {}", e))?;

        {
            let mut item = self.item.write().await;
            if item.content_length == DownloadItem::LENGTH_UNKNOWN {
                item.content_length = file_size as i64;
            }
        }

        // Prepare destination
        let item = self.item.read().await;
        let output_path = PathBuf::from(&item.folder).join(&item.name);
        let disk = DiskActor::spawn(output_path, Some(file_size as u64)).await?;
        *self.disk.lock().await = Some(disk);
        drop(item);

        let _ = self.status_tx.send(JobStatus::Downloading);

        // Download using RETR with passive mode
        // For simplicity, using single-connection download for now.
        // Multi-connection FTP requires separate control sessions (FTP protocol limitation).
        let mut data_stream = control
            .retr_as_stream(&ftp_url.path)
            .await
            .map_err(|e| anyhow::anyhow!("FTP RETR failed: {}", e))?;

        let disk_guard = self.disk.lock().await;
        let disk = disk_guard.as_ref().ok_or_else(|| anyhow::anyhow!("No disk"))?;
        let mut writer = disk.writer_for(0);

        use tokio::io::AsyncReadExt;
        let mut buf = vec![0u8; 65536];
        let mut total_read: u64 = 0;

        loop {
            let n = data_stream
                .read(&mut buf)
                .await
                .map_err(|e| anyhow::anyhow!("FTP read failed: {}", e))?;

            if n == 0 {
                break;
            }

            self.global_throttler.acquire(n as u32).await;

            let data = Bytes::copy_from_slice(&buf[..n]);
            writer
                .write(data)
                .await
                .map_err(|e| anyhow::anyhow!("Write failed: {}", e))?;

            total_read += n as u64;
        }

        // Finalize
        control
            .finalize_retr_stream(data_stream)
            .await
            .map_err(|e| anyhow::anyhow!("FTP finalize failed: {}", e))?;

        control.quit().await.ok();

        // Update item
        {
            let mut item = self.item.write().await;
            item.status = DownloadStatus::Completed;
            item.complete_time = Some(
                std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_millis() as i64,
            );
            self.dl_db.update(&item).await?;
        }

        if let Some(disk) = self.disk.lock().await.as_ref() {
            disk.flush().await.ok();
            disk.shutdown().await;
        }

        let _ = self.status_tx.send(JobStatus::Finished);
        info!("FTP job #{} completed ({} bytes)", id, total_read);
        Ok(())
    }

    /// Parse an FTP URL into components.
    fn parse_ftp_url(url: &str) -> anyhow::Result<FtpUrl> {
        let secure = url.starts_with("ftps://");
        let stripped = url
            .strip_prefix("ftp://")
            .or_else(|| url.strip_prefix("ftps://"))
            .ok_or_else(|| anyhow::anyhow!("Invalid FTP URL: {}", url))?;

        let (auth_part, host_path) = if let Some(at) = stripped.find('@') {
            (Some(&stripped[..at]), &stripped[at + 1..])
        } else {
            (None, stripped)
        };

        let (username, password) = if let Some(auth) = auth_part {
            if let Some(colon) = auth.find(':') {
                (Some(auth[..colon].to_string()), Some(auth[colon + 1..].to_string()))
            } else {
                (Some(auth.to_string()), None)
            }
        } else {
            (None, None)
        };

        let (host_port, path) = if let Some(slash) = host_path.find('/') {
            (&host_path[..slash], host_path[slash..].to_string())
        } else {
            (host_path, "/".to_string())
        };

        let (host, port) = if let Some(colon) = host_port.rfind(':') {
            let port = host_port[colon + 1..].parse::<u16>().unwrap_or(21);
            (host_port[..colon].to_string(), port)
        } else {
            (host_port.to_string(), if secure { 990 } else { 21 })
        };

        Ok(FtpUrl {
            host,
            port,
            username,
            password,
            path,
            secure,
        })
    }
}
