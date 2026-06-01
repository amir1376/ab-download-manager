// xeton_core::extractors — Social media and platform extractors.
//
// Platform resolver pipeline that extracts direct download URLs
// from YouTube, TikTok, Instagram, Spotify, SoundCloud, etc.

pub mod ytdlp;
pub mod soundcloud;
pub mod spotify;
pub mod social;

use async_trait::async_trait;
use serde::{Deserialize, Serialize};

/// A resolved media stream from an extractor.
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record)]
pub struct MediaStream {
    pub url: String,
    pub format_id: String,
    /// "video", "audio", or "video+audio"
    pub stream_type: String,
    /// Container format: "mp4", "webm", "m4a", etc.
    pub container: Option<String>,
    /// Video codec: "h264", "vp9", etc.
    pub video_codec: Option<String>,
    /// Audio codec: "aac", "opus", "mp3", etc.
    pub audio_codec: Option<String>,
    /// Video resolution height (e.g., 1080).
    pub height: Option<u32>,
    /// Audio bitrate in kbps.
    pub audio_bitrate: Option<u32>,
    /// File size in bytes (if known).
    pub filesize: Option<i64>,
}

/// Extracted media metadata from a URL.
#[derive(Clone, Debug, Serialize, Deserialize, uniffi::Record)]
pub struct ExtractedMedia {
    pub title: String,
    pub streams: Vec<MediaStream>,
    pub thumbnail: Option<String>,
    pub duration_sec: Option<u32>,
    pub uploader: Option<String>,
    pub description: Option<String>,
}

/// Trait for platform-specific extractors.
#[async_trait]
pub trait Extractor: Send + Sync {
    /// Check if this extractor can handle the given URL.
    fn can_handle(&self, url: &str) -> bool;

    /// Extract media information from the URL.
    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia>;

    /// Name of the platform this extractor handles.
    fn platform_name(&self) -> &str;
}

/// Route a URL to the appropriate extractor.
pub struct ExtractorRouter {
    extractors: Vec<Box<dyn Extractor>>,
}

impl ExtractorRouter {
    pub fn new() -> Self {
        let mut extractors: Vec<Box<dyn Extractor>> = Vec::new();

        // Register custom fast-path extractors first
        extractors.push(Box::new(social::TikTokExtractor::default()));
        extractors.push(Box::new(social::InstagramExtractor::default()));
        extractors.push(Box::new(soundcloud::SoundCloudExtractor::default()));
        extractors.push(Box::new(spotify::SpotifyExtractor::default()));

        // yt-dlp acts as the generic fallback
        extractors.push(Box::new(ytdlp::YtDlpExtractor::default()));

        Self { extractors }
    }

    /// Find the right extractor and extract media info.
    pub async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        for extractor in &self.extractors {
            if extractor.can_handle(url) {
                return extractor.extract(url).await;
            }
        }
        anyhow::bail!("No extractor found for URL: {}", url)
    }
}

impl Default for ExtractorRouter {
    fn default() -> Self {
        Self::new()
    }
}
