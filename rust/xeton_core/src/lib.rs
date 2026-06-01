// xeton_core — Rust backend for the Xeton Download Manager
// This crate is the UniFFI-exposed engine replacing the Kotlin downloader/core module.
// Full implementation is built up phase by phase.

pub mod db;
pub mod models;
pub mod connection;
pub mod destination;
pub mod part;
pub mod http;
pub mod hls;
pub mod queue;
pub mod manager;
pub mod extractors;
pub mod protocols;
pub mod transcoder;
pub mod throttle;
pub mod uniffi_api;

pub use uniffi_api::{extract_audio, merge_video_audio, extract_media_info};

uniffi::setup_scaffolding!();

