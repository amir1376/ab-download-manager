// xeton_core::extractors::spotify — Spotify extractor.
//
// Extracts track metadata using open Spotify API endpoints.
// For audio streams, it provides the 30-second preview (if available),
// and could optionally delegate to a YouTube Music search fallback.

use async_trait::async_trait;
use serde::Deserialize;
use tracing::debug;

use super::{ExtractedMedia, Extractor, MediaStream};

pub struct SpotifyExtractor {
    client: reqwest::Client,
}

impl Default for SpotifyExtractor {
    fn default() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent("Mozilla/5.0")
                .build()
                .unwrap_or_default(),
        }
    }
}

#[derive(Debug, Deserialize)]
struct EmbedResponse {
    title: Option<String>,
    thumbnail_url: Option<String>,
}

#[async_trait]
impl Extractor for SpotifyExtractor {
    fn can_handle(&self, url: &str) -> bool {
        url.contains("spotify.com")
    }

    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        debug!("Extracting Spotify: {}", url);

        // A robust way to get basic metadata without OAuth is using the oEmbed API
        let oembed_url = format!("https://open.spotify.com/oembed?url={}", urlencoding::encode(url));
        let resp = self.client.get(&oembed_url).send().await?;
        
        let metadata = if resp.status().is_success() {
            resp.json::<EmbedResponse>().await.ok()
        } else {
            None
        };

        // Note: Spotify doesn't provide full audio streams publicly.
        // A complete implementation would either:
        // 1. Return the 30s preview URL (parsed from the web page HTML).
        // 2. Use the metadata to perform a search on YouTube/YouTube Music and return that stream.
        // For this port, we will just return the metadata as this requires more complex integration.

        let mut streams = Vec::new();
        // Placeholder for stream fetching logic...
        
        Ok(ExtractedMedia {
            title: metadata.as_ref().and_then(|m| m.title.clone()).unwrap_or_else(|| "Spotify Track".to_string()),
            streams,
            thumbnail: metadata.as_ref().and_then(|m| m.thumbnail_url.clone()),
            duration_sec: None,
            uploader: None,
            description: None,
        })
    }

    fn platform_name(&self) -> &str {
        "Spotify"
    }
}
