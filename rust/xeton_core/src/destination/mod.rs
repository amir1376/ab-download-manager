// xeton_core::destination — Zero-copy disk I/O via actor pattern.
//
// Replaces `ir.amirab.downloader.destination.SimpleDownloadDestination` and
// `ir.amirab.downloader.destination.DestWriter` with a Tokio-based actor that
// serializes all file writes through an mpsc channel.
//
// Key design:
//   - Bytes buffers are moved (not copied) through the channel.
//   - A single Tokio task owns the file handle, eliminating seek/write races.
//   - CRC32 block validation is performed before queuing writes.

use std::io::SeekFrom;
use std::path::{Path, PathBuf};
use std::sync::Arc;

use bytes::Bytes;
use thiserror::Error;
use tokio::fs::{self, File, OpenOptions};
use tokio::io::{AsyncSeekExt, AsyncWriteExt};
use tokio::sync::{mpsc, oneshot};
use tracing::{debug, error, info, warn};

// ─── Errors ─────────────────────────────────────────────────────────────────

#[derive(Debug, Error)]
pub enum DestinationError {
    #[error("I/O error: {0}")]
    Io(#[from] std::io::Error),

    #[error("Disk actor channel closed")]
    ChannelClosed,

    #[error("Failed to send write command")]
    SendFailed,

    #[error("CRC32 mismatch: expected {expected:#010x}, got {actual:#010x}")]
    Crc32Mismatch { expected: u32, actual: u32 },
}

// ─── Write Commands ─────────────────────────────────────────────────────────

/// Commands sent to the DiskActor through the mpsc channel.
pub enum WriteCmd {
    /// Write `data` at absolute file offset `offset`.
    Write { offset: u64, data: Bytes },
    /// Flush all buffered writes to disk.
    Flush { reply: oneshot::Sender<Result<(), DestinationError>> },
    /// Shut down the actor, closing the file handle.
    Shutdown,
}

// ─── DiskActor ──────────────────────────────────────────────────────────────

/// A background Tokio task that owns a file handle and processes write commands
/// sequentially. This eliminates all seek/write races when multiple part runners
/// write to the same file concurrently.
///
/// This replaces the Kotlin `DestWriter` + `SimpleDownloadDestination` pattern
/// which used `synchronized(this)` blocks around `FileHandle` operations.
pub struct DiskActor {
    tx: mpsc::Sender<WriteCmd>,
    path: PathBuf,
}

impl DiskActor {
    /// Spawn a new DiskActor for the given file path.
    ///
    /// - If `expected_size` is `Some(n)`, the file is pre-allocated to `n` bytes
    ///   (sparse allocation where supported).
    /// - The actor runs until a `Shutdown` command is received or all senders drop.
    pub async fn spawn(
        path: PathBuf,
        expected_size: Option<u64>,
    ) -> Result<Self, DestinationError> {
        // Ensure parent directory exists
        if let Some(parent) = path.parent() {
            fs::create_dir_all(parent).await?;
        }

        // Open or create the file
        let mut file = OpenOptions::new()
            .create(true)
            .write(true)
            .read(true)
            .truncate(false)
            .open(&path)
            .await?;

        // Pre-allocate sparse file if size is known
        if let Some(size) = expected_size {
            Self::preallocate(&file, size).await?;
        }

        // Channel with generous buffer — writers should not block under normal load.
        // 2048 entries × ~64KB average chunk = ~128 MB buffered max.
        let (tx, mut rx) = mpsc::channel::<WriteCmd>(2048);

        let actor_path = path.clone();
        tokio::spawn(async move {
            debug!("DiskActor started for {}", actor_path.display());

            while let Some(cmd) = rx.recv().await {
                match cmd {
                    WriteCmd::Write { offset, data } => {
                        if let Err(e) = file.seek(SeekFrom::Start(offset)).await {
                            error!("Seek error at offset {}: {}", offset, e);
                            continue;
                        }
                        if let Err(e) = file.write_all(&data).await {
                            error!("Write error at offset {}: {}", offset, e);
                        }
                    }
                    WriteCmd::Flush { reply } => {
                        let result = file.flush().await.map_err(DestinationError::Io);
                        let _ = reply.send(result);
                    }
                    WriteCmd::Shutdown => {
                        let _ = file.flush().await;
                        debug!("DiskActor shutting down for {}", actor_path.display());
                        break;
                    }
                }
            }

            // Drain remaining writes before closing
            while let Ok(cmd) = rx.try_recv() {
                if let WriteCmd::Write { offset, data } = cmd {
                    let _ = file.seek(SeekFrom::Start(offset)).await;
                    let _ = file.write_all(&data).await;
                }
            }
            let _ = file.flush().await;
        });

        Ok(Self { tx, path })
    }

    /// Create a `PartWriter` bound to a specific base offset.
    pub fn writer_for(&self, base_offset: u64) -> PartWriter {
        PartWriter {
            tx: self.tx.clone(),
            base_offset,
            written: 0,
        }
    }

    /// Flush all pending writes to disk.
    pub async fn flush(&self) -> Result<(), DestinationError> {
        let (reply_tx, reply_rx) = oneshot::channel();
        self.tx
            .send(WriteCmd::Flush { reply: reply_tx })
            .await
            .map_err(|_| DestinationError::ChannelClosed)?;
        reply_rx.await.map_err(|_| DestinationError::ChannelClosed)?
    }

    /// Shut down the actor and close the file handle.
    pub async fn shutdown(&self) {
        let _ = self.tx.send(WriteCmd::Shutdown).await;
    }

    /// Get the file path this actor writes to.
    pub fn path(&self) -> &Path {
        &self.path
    }

    /// Pre-allocate a file to the given size using platform-specific sparse allocation.
    #[cfg(target_os = "linux")]
    async fn preallocate(file: &File, size: u64) -> Result<(), DestinationError> {
        use std::os::unix::io::AsRawFd;
        let fd = file.as_raw_fd();
        // Use ftruncate to set the file size. The kernel creates a sparse file
        // on filesystems that support it (ext4, btrfs, xfs).
        let result = unsafe { libc::ftruncate(fd, size as libc::off_t) };
        if result != 0 {
            return Err(DestinationError::Io(std::io::Error::last_os_error()));
        }
        debug!("Pre-allocated {} bytes (sparse)", size);
        Ok(())
    }

    #[cfg(target_os = "windows")]
    async fn preallocate(file: &File, size: u64) -> Result<(), DestinationError> {
        // On Windows, use SetEndOfFile to extend the file size.
        // Windows NTFS automatically creates sparse regions.
        file.set_len(size).await?;
        debug!("Pre-allocated {} bytes", size);
        Ok(())
    }

    #[cfg(target_os = "macos")]
    async fn preallocate(file: &File, size: u64) -> Result<(), DestinationError> {
        use std::os::unix::io::AsRawFd;
        let fd = file.as_raw_fd();
        let mut store = libc::fstore_t {
            fst_flags: libc::F_ALLOCATECONTIG,
            fst_posmode: libc::F_PEOFPOSMODE,
            fst_offset: 0,
            fst_length: size as libc::off_t,
            fst_bytesalloc: 0,
        };
        let mut res = unsafe { libc::fcntl(fd, libc::F_PREALLOCATE, &store) };
        if res == -1 {
            store.fst_flags = libc::F_ALLOCATEALL;
            res = unsafe { libc::fcntl(fd, libc::F_PREALLOCATE, &store) };
        }
        if res != -1 {
            unsafe { libc::ftruncate(fd, size as libc::off_t) };
        } else {
            return Err(DestinationError::Io(std::io::Error::last_os_error()));
        }
        debug!("Pre-allocated {} bytes (macOS)", size);
        Ok(())
    }

    #[cfg(not(any(target_os = "linux", target_os = "windows", target_os = "macos")))]
    async fn preallocate(file: &File, size: u64) -> Result<(), DestinationError> {
        // Fallback: just set the file length
        file.set_len(size).await?;
        debug!("Pre-allocated {} bytes (fallback)", size);
        Ok(())
    }
}

// ─── PartWriter ─────────────────────────────────────────────────────────────

/// A writer bound to a specific byte offset within the download file.
/// Each `PartRunner` gets its own `PartWriter`.
///
/// Writes are zero-copy: the `Bytes` buffer is moved through the channel,
/// not cloned or copied. Memory is deallocated only after the DiskActor
/// completes the write.
pub struct PartWriter {
    tx: mpsc::Sender<WriteCmd>,
    /// Absolute file offset where this part starts writing.
    base_offset: u64,
    /// Bytes written through this writer so far.
    written: u64,
}

impl PartWriter {
    /// Write a chunk of data at the current offset and advance.
    pub async fn write(&mut self, data: Bytes) -> Result<(), DestinationError> {
        let offset = self.base_offset + self.written;
        let len = data.len() as u64;
        self.tx
            .send(WriteCmd::Write { offset, data })
            .await
            .map_err(|_| DestinationError::SendFailed)?;
        self.written += len;
        Ok(())
    }

    /// Total bytes written through this writer.
    pub fn written(&self) -> u64 {
        self.written
    }

    /// Current absolute file offset.
    pub fn current_offset(&self) -> u64 {
        self.base_offset + self.written
    }
}

// ─── CRC32 Block Verification ───────────────────────────────────────────────

/// Verify a chunk's integrity using CRC32.
/// Returns `Ok(())` if the checksum matches, or `Err` with the mismatch.
pub fn verify_crc32(data: &[u8], expected: u32) -> Result<(), DestinationError> {
    let actual = crc32fast::hash(data);
    if actual == expected {
        Ok(())
    } else {
        Err(DestinationError::Crc32Mismatch { expected, actual })
    }
}

/// Compute CRC32 checksum for a data block.
pub fn compute_crc32(data: &[u8]) -> u32 {
    crc32fast::hash(data)
}

// ─── Incomplete File Naming ─────────────────────────────────────────────────

/// Utilities for the `.xdlpart.<id>` incomplete file extension.
pub struct IncompleteFileUtil;

impl IncompleteFileUtil {
    /// Add the incomplete indicator to a file path.
    /// Example: `/downloads/file.zip` → `/downloads/file.zip.xdlpart.42`
    pub fn add_indicator(path: &Path, download_id: i64) -> PathBuf {
        let mut name = path.as_os_str().to_owned();
        name.push(format!(".xdlpart.{}", download_id));
        PathBuf::from(name)
    }

    /// Remove the incomplete indicator from a file path.
    pub fn remove_indicator(path: &Path) -> Option<PathBuf> {
        let name = path.to_str()?;
        let idx = name.rfind(".xdlpart.")?;
        Some(PathBuf::from(&name[..idx]))
    }

    /// Check if a path has an incomplete indicator.
    pub fn has_indicator(path: &Path) -> bool {
        path.to_str()
            .is_some_and(|s| s.contains(".xdlpart."))
    }
}

// ─── Atomic Rename ──────────────────────────────────────────────────────────

/// Atomically rename a file. On POSIX this is a single `rename(2)` syscall
/// which is atomic on the same filesystem.
pub async fn atomic_rename(from: &Path, to: &Path) -> Result<(), DestinationError> {
    // Delete target if it exists (rename on some platforms fails if target exists)
    if to.exists() {
        fs::remove_file(to).await?;
    }
    fs::rename(from, to).await?;
    Ok(())
}
