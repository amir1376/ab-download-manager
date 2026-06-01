// xeton_core::hls::job — HLS (M3U8) download job.
//
// Port of `ir.amirab.downloader.downloaditem.hls.HLSDownloadJob`.
// Uses `m3u8-rs` for playlist parsing and `aes`/`cbc` for segment decryption.

#[cfg(feature = "hls")]
use std::path::PathBuf;
#[cfg(feature = "hls")]
use std::sync::Arc;

#[cfg(feature = "hls")]
use aes::Aes128;
#[cfg(feature = "hls")]
use bytes::{Bytes, BytesMut};
#[cfg(feature = "hls")]
use cbc::cipher::{BlockDecryptMut, KeyIvInit};
#[cfg(feature = "hls")]
use reqwest::header::HeaderMap;
#[cfg(feature = "hls")]
use tokio::sync::{watch, Mutex, RwLock};
#[cfg(feature = "hls")]
use tracing::{debug, error, info, warn};

#[cfg(feature = "hls")]
use crate::connection::HttpClient;
#[cfg(feature = "hls")]
use crate::db::{DownloadDb, PartDb};
#[cfg(feature = "hls")]
use crate::destination::DiskActor;
#[cfg(feature = "hls")]
use crate::models::*;
#[cfg(feature = "hls")]
use crate::throttle::Throttler;

#[cfg(feature = "hls")]
type Aes128CbcDec = cbc::Decryptor<Aes128>;

/// HLS download job — downloads M3U8 playlists and their TS segments.
///
/// Segments are downloaded sequentially (HLS requires ordering) and
/// concatenated into a single output file. AES-128-CBC decryption is
/// applied when `#EXT-X-KEY` specifies it.
#[cfg(feature = "hls")]
pub struct HlsJob {
    pub item: Arc<RwLock<DownloadItem>>,
    client: Arc<HttpClient>,
    dl_db: Arc<dyn DownloadDb>,
    part_db: Arc<dyn PartDb>,
    settings: Arc<RwLock<DownloadSettings>>,
    global_throttler: Arc<Throttler>,
    status_tx: watch::Sender<JobStatus>,
    pub status_rx: watch::Receiver<JobStatus>,
    disk: Mutex<Option<DiskActor>>,
    /// Parsed segments from the M3U8 playlist.
    segments: Mutex<Vec<HlsSegment>>,
    /// Index of the next segment to download.
    next_segment: Mutex<usize>,
    data_dir: PathBuf,
}

/// Represents a single HLS media segment.
#[cfg(feature = "hls")]
#[derive(Clone, Debug)]
pub struct HlsSegment {
    pub uri: String,
    pub duration: f64,
    pub byte_offset: i64,
    /// Encryption key URI (if EXT-X-KEY is present).
    pub key_uri: Option<String>,
    /// Encryption IV (16 bytes). If absent, derived from media sequence number.
    pub iv: Option<[u8; 16]>,
    pub downloaded: bool,
}

#[cfg(feature = "hls")]
impl HlsJob {
    pub fn new(
        item: DownloadItem,
        settings: Arc<RwLock<DownloadSettings>>,
        dl_db: Arc<dyn DownloadDb>,
        part_db: Arc<dyn PartDb>,
        global_throttler: Arc<Throttler>,
        client: Arc<HttpClient>,
        data_dir: PathBuf,
    ) -> Arc<Self> {
        let (status_tx, status_rx) = watch::channel(JobStatus::Idle);
        Arc::new(Self {
            item: Arc::new(RwLock::new(item)),
            client,
            dl_db,
            part_db,
            settings,
            global_throttler,
            status_tx,
            status_rx,
            disk: Mutex::new(None),
            segments: Mutex::new(Vec::new()),
            next_segment: Mutex::new(0),
            data_dir,
        })
    }

    /// Boot: load persisted segment state.
    pub async fn boot(&self) -> anyhow::Result<()> {
        let id = self.item.read().await.numeric_id;
        let saved_parts = self.part_db.get_parts(id).await?;

        let mut segments = self.segments.lock().await;
        for (i, part) in saved_parts.iter().enumerate() {
            if let Some(seg) = segments.get_mut(i) {
                seg.downloaded = part.is_completed();
            }
        }

        // Find next un-downloaded segment
        let next = segments.iter().position(|s| !s.downloaded).unwrap_or(segments.len());
        *self.next_segment.lock().await = next;

        Ok(())
    }

    /// Resume the HLS download.
    pub async fn resume(self: &Arc<Self>) -> anyhow::Result<()> {
        let id = self.item.read().await.numeric_id;
        info!("Resuming HLS job #{}", id);

        let _ = self.status_tx.send(JobStatus::Resuming);

        // Fetch and parse playlist
        self.fetch_playlist().await?;

        // Prepare output file
        self.prepare_destination().await?;

        // Download segments sequentially
        self.download_segments().await?;

        Ok(())
    }

    /// Fetch and parse the M3U8 playlist.
    async fn fetch_playlist(&self) -> anyhow::Result<()> {
        loop {
            let url = {
                let item = self.item.read().await;
                item.link.clone()
            };
            let headers = HeaderMap::new();

            let resp = self.client.inner().get(&url).headers(headers).send().await?;
            let body = resp.bytes().await?;
            let body_str = String::from_utf8_lossy(&body);

            let parsed = m3u8_rs::parse_playlist_res(body_str.as_bytes());
            match parsed {
                Ok(m3u8_rs::Playlist::MediaPlaylist(playlist)) => {
                    let mut segments = self.segments.lock().await;
                    segments.clear();

                    let mut current_key_uri: Option<String> = None;
                    let mut current_iv: Option<[u8; 16]> = None;
                    let mut byte_offset: i64 = 0;

                    for (seq, segment) in playlist.segments.iter().enumerate() {
                        // Check for key changes
                        if let Some(ref key) = segment.key {
                            if key.method == m3u8_rs::KeyMethod::AES128 {
                                current_key_uri = key.uri.clone();
                                current_iv = key.iv.as_ref().and_then(|iv_str| {
                                    parse_hex_iv(iv_str)
                                });
                            } else if key.method == m3u8_rs::KeyMethod::None {
                                current_key_uri = None;
                                current_iv = None;
                            }
                        }

                        let iv = current_iv.unwrap_or_else(|| {
                            // Default IV: media sequence number as big-endian 16-byte value
                            let seq_num = (playlist.media_sequence + seq as u64) as u128;
                            seq_num.to_be_bytes()
                        });

                        segments.push(HlsSegment {
                            uri: resolve_url(&url, &segment.uri),
                            duration: segment.duration as f64,
                            byte_offset,
                            key_uri: current_key_uri.clone(),
                            iv: Some(iv),
                            downloaded: false,
                        });

                        // Estimate offset (will be corrected after actual download)
                        byte_offset += 1; // placeholder
                    }

                    info!("Parsed {} HLS segments", segments.len());
                    return Ok(());
                }
                Ok(m3u8_rs::Playlist::MasterPlaylist(master)) => {
                    // Select best variant (highest bandwidth)
                    if let Some(best) = master.variants.iter().max_by_key(|v| v.bandwidth) {
                        let mut item = self.item.write().await;
                        item.link = resolve_url(&url, &best.uri);
                        drop(item);
                        // Loop instead of recursing
                        continue;
                    }
                    anyhow::bail!("No variants found in master playlist");
                }
                Err(e) => anyhow::bail!("Failed to parse M3U8 playlist: {:?}", e),
            }
        }
    }

    /// Prepare the output file.
    async fn prepare_destination(&self) -> anyhow::Result<()> {
        let _ = self.status_tx.send(JobStatus::PreparingFile { progress: None });

        let item = self.item.read().await;
        let settings = self.settings.read().await;
        let output_path = PathBuf::from(&item.folder).join(&item.name);

        let file_path = if settings.append_extension_to_incomplete {
            crate::destination::IncompleteFileUtil::add_indicator(&output_path, item.numeric_id)
        } else {
            output_path
        };

        // HLS files are written sequentially — no pre-allocation needed
        let disk = DiskActor::spawn(file_path, None).await?;
        *self.disk.lock().await = Some(disk);

        Ok(())
    }

    /// Download all segments sequentially.
    async fn download_segments(&self) -> anyhow::Result<()> {
        let _ = self.status_tx.send(JobStatus::Downloading);

        let mut next = self.next_segment.lock().await;
        let segments = self.segments.lock().await;
        let total = segments.len();
        let segments_snapshot: Vec<HlsSegment> = segments.clone();
        drop(segments);

        let disk_guard = self.disk.lock().await;
        let disk = disk_guard.as_ref().ok_or_else(|| anyhow::anyhow!("No disk"))?;

        let mut write_offset: u64 = 0;

        for i in *next..total {
            let segment = &segments_snapshot[i];

            // Download segment
            let resp = self
                .client
                .inner()
                .get(&segment.uri)
                .send()
                .await?;

            let mut data = resp.bytes().await?;

            // Decrypt if encrypted
            if let Some(ref key_uri) = segment.key_uri {
                let key = self.fetch_key(key_uri).await?;
                let iv = segment.iv.unwrap_or([0u8; 16]);
                data = decrypt_aes128_cbc(&data, &key, &iv)?;
            }

            // Throttle
            self.global_throttler.acquire(data.len() as u32).await;

            // Write to file
            let mut writer = disk.writer_for(write_offset);
            writer.write(data.clone()).await.map_err(|e| anyhow::anyhow!("{}", e))?;
            write_offset += data.len() as u64;

            // Mark as downloaded
            *next = i + 1;

            debug!("Downloaded HLS segment {}/{}", i + 1, total);
        }

        // Finalize
        drop(disk_guard);
        self.on_download_finished().await?;

        Ok(())
    }

    /// Fetch an AES-128 encryption key from the given URI.
    async fn fetch_key(&self, uri: &str) -> anyhow::Result<[u8; 16]> {
        let resp = self.client.inner().get(uri).send().await?;
        let body = resp.bytes().await?;
        if body.len() != 16 {
            anyhow::bail!("AES key must be 16 bytes, got {}", body.len());
        }
        let mut key = [0u8; 16];
        key.copy_from_slice(&body);
        Ok(key)
    }

    /// Handle download completion.
    async fn on_download_finished(&self) -> anyhow::Result<()> {
        let mut item = self.item.write().await;
        item.status = DownloadStatus::Completed;
        item.complete_time = Some(
            std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap_or_default()
                .as_millis() as i64,
        );
        drop(item);

        let item = self.item.read().await;
        self.dl_db.update(&item).await?;

        if let Some(disk) = self.disk.lock().await.as_ref() {
            disk.flush().await.ok();
            disk.shutdown().await;
        }

        // Rename incomplete file if needed
        let settings = self.settings.read().await;
        if settings.append_extension_to_incomplete {
            let output_path = PathBuf::from(&item.folder).join(&item.name);
            let incomplete = crate::destination::IncompleteFileUtil::add_indicator(&output_path, item.numeric_id);
            if incomplete.exists() {
                crate::destination::atomic_rename(&incomplete, &output_path).await?;
            }
        }

        let _ = self.status_tx.send(JobStatus::Finished);
        info!("HLS job #{} completed", item.numeric_id);
        Ok(())
    }
}

// ─── HLS Helpers ────────────────────────────────────────────────────────────

/// Decrypt AES-128-CBC data with PKCS7 padding.
#[cfg(feature = "hls")]
fn decrypt_aes128_cbc(data: &[u8], key: &[u8; 16], iv: &[u8; 16]) -> anyhow::Result<Bytes> {
    use cbc::cipher::block_padding::Pkcs7;

    let mut buf = data.to_vec();
    let decryptor = Aes128CbcDec::new(key.into(), iv.into());
    let decrypted = decryptor
        .decrypt_padded_mut::<Pkcs7>(&mut buf)
        .map_err(|e| anyhow::anyhow!("AES decryption failed: {:?}", e))?;

    Ok(Bytes::copy_from_slice(decrypted))
}

/// Resolve a potentially relative URL against a base URL.
#[cfg(feature = "hls")]
fn resolve_url(base: &str, relative: &str) -> String {
    if relative.starts_with("http://") || relative.starts_with("https://") {
        return relative.to_string();
    }

    // Simple URL resolution
    if let Some(last_slash) = base.rfind('/') {
        format!("{}/{}", &base[..last_slash], relative)
    } else {
        relative.to_string()
    }
}

/// Parse a hex IV string (e.g., "0x00000000000000000000000000000001") into bytes.
#[cfg(feature = "hls")]
fn parse_hex_iv(hex: &str) -> Option<[u8; 16]> {
    let hex = hex.strip_prefix("0x").unwrap_or(hex);
    if hex.len() != 32 {
        return None;
    }
    let mut result = [0u8; 16];
    for i in 0..16 {
        result[i] = u8::from_str_radix(&hex[i * 2..i * 2 + 2], 16).ok()?;
    }
    Some(result)
}
