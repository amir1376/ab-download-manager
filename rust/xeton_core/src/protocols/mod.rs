// xeton_core::protocols — Multi-protocol download engines.

#[cfg(feature = "ftp")]
pub mod ftp;

#[cfg(feature = "torrent")]
pub mod torrent;
