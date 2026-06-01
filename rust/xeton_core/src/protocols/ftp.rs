// xeton_core::protocols::ftp — Async FTP/FTPS download engine.
//
// Uses `suppaftp` for async FTP control/data connections.
// Supports parallel downloads via multiple PASV data connections.

use std::path::PathBuf;
use std::sync::Arc;

use bytes::Bytes;
use suppaftp::{AsyncRustlsFtpStream, AsyncRustlsConnector};
use suppaftp::types::FileType;
use tokio::sync::{watch, Mutex, RwLock};
use tracing::{debug, error, info, warn};

use crate::db::{DownloadDb, PartDb};
use crate::part::split_to_ranges;
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
    disk: Mutex<Option<Arc<DiskActor>>>,
    data_dir: PathBuf,
}

/// Parsed FTP URL components.
#[derive(Clone)]
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
    async fn connect_control(ftp_url: &FtpUrl) -> anyhow::Result<AsyncRustlsFtpStream> {
        let mut control = AsyncRustlsFtpStream::connect(format!("{}:{}", ftp_url.host, ftp_url.port))
            .await
            .map_err(|e| anyhow::anyhow!("FTP connect failed: {}", e))?;

        if ftp_url.secure {
            let mut root_store = suppaftp::rustls::RootCertStore::empty();
            let native_certs = rustls_native_certs::load_native_certs();
            for cert in native_certs.certs {
                let _ = root_store.add(cert);
            }
            if !native_certs.errors.is_empty() {
                warn!("Some errors occurred while loading native certificates: {:?}", native_certs.errors);
            }
            for cert in webpki_roots::TLS_SERVER_ROOTS {
                let _ = root_store.add(cert.clone());
            }

            let config = suppaftp::rustls::ClientConfig::builder()
                .with_root_certificates(root_store)
                .with_no_client_auth();
            
            let connector = AsyncRustlsConnector::from(futures_rustls::TlsConnector::from(std::sync::Arc::new(config)));
            control = control
                .into_secure(connector, &ftp_url.host)
                .await
                .map_err(|e| anyhow::anyhow!("FTPS upgrade failed: {}", e))?;
        }

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

        Ok(control)
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
        let mut control = Self::connect_control(&ftp_url).await?;

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

        // Prepare parts
        let mut parts = self.part_db.get_parts(id).await?;
        let conn_count = self.settings.read().await.default_thread_count as i32;
        let min_part_size = self.settings.read().await.min_part_size as i64;
        
        let should_init = parts.is_empty() || parts.len() != conn_count as usize;
        if should_init {
            parts.clear();
            let ranges = split_to_ranges(file_size as i64, conn_count as u32, min_part_size);
            for (start, end) in ranges {
                parts.push(RangedPart::new(start, Some(end), start));
            }
        } else {
            if parts.is_empty() {
                let end = if file_size > 0 { Some(file_size as i64 - 1) } else { None };
                parts.push(RangedPart::new(0, end, 0));
            }
        }
        self.part_db.set_parts(id, &parts).await?;

        // Prepare destination
        let item_read = self.item.read().await;
        let output_path = PathBuf::from(&item_read.folder).join(&item_read.name);
        drop(item_read);
        let disk = Arc::new(DiskActor::spawn(output_path, Some(file_size as u64)).await?);
        *self.disk.lock().await = Some(Arc::clone(&disk));

        let _ = self.status_tx.send(JobStatus::Downloading);

        // Spawn a task for each part
        let mut join_set = tokio::task::JoinSet::new();

        for (idx, part) in parts.into_iter().enumerate() {
            let part = Arc::new(RwLock::new(part));
            let ftp_url = ftp_url.clone();
            let disk = Arc::clone(&disk);
            let throttler = Arc::clone(&self.global_throttler);
            let part_db = Arc::clone(&self.part_db);

            join_set.spawn(async move {
                let mut p = part.read().await.clone();
                if p.is_completed() {
                    return Ok::<(), anyhow::Error>(());
                }

                let mut control = Self::connect_control(&ftp_url).await?;

                control.resume_transfer(p.current as usize).await.map_err(|e| anyhow::anyhow!("FTP part REST failed: {}", e))?;

                let data_stream = control.retr_as_stream(&ftp_url.path).await.map_err(|e| anyhow::anyhow!("FTP part RETR failed: {}", e))?;

                use tokio_util::compat::FuturesAsyncReadCompatExt;
                let mut data_stream = data_stream.compat();
                let mut writer = disk.writer_for(p.current as u64);

                use tokio::io::AsyncReadExt;
                let mut buf = vec![0u8; 65536];
                
                let target_len = p.remaining().unwrap_or(std::i64::MAX);
                let mut bytes_read = 0;

                while bytes_read < target_len {
                    let to_read = std::cmp::min(buf.len() as i64, target_len - bytes_read) as usize;
                    let n = data_stream.read(&mut buf[..to_read]).await.map_err(|e| anyhow::anyhow!("FTP read failed: {}", e))?;
                    if n == 0 { break; }
                    
                    throttler.acquire(n as u32).await;
                    let data = Bytes::copy_from_slice(&buf[..n]);
                    writer.write(data).await.map_err(|e| anyhow::anyhow!("Write failed: {}", e))?;
                    
                    bytes_read += n as i64;
                    
                    // Simple auto-save simulation
                    p.current += n as i64;
                    *part.write().await = p.clone();
                }

                control.finalize_retr_stream(data_stream.into_inner()).await.map_err(|e| anyhow::anyhow!("FTP finalize failed: {}", e))?;
                control.quit().await.ok();

                Ok(())
            });
        }

        // Wait for all to finish
        let mut total_success = true;
        while let Some(res) = join_set.join_next().await {
            match res {
                Ok(Ok(_)) => {},
                Ok(Err(e)) => {
                    error!("Part failed: {}", e);
                    total_success = false;
                },
                Err(e) => {
                    error!("Join error: {}", e);
                    total_success = false;
                }
            }
        }

        if !total_success {
            return Err(anyhow::anyhow!("Some parts failed"));
        }

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
        info!("FTP job #{} completed", id);
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
