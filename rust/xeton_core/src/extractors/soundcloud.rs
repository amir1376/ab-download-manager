// xeton_core::extractors::soundcloud — SoundCloud direct API extractor.
//
// Resolves high-quality audio streams from SoundCloud public API endpoints.
// Uses client_id discovery and resolves track stream URLs directly.

use async_trait::async_trait;
use serde::Deserialize;
use tracing::debug;

use super::{ExtractedMedia, Extractor, MediaStream};

/// SoundCloud API-based extractor.
pub struct SoundCloudExtractor {
    client: reqwest::Client,
    /// Discovered SoundCloud client_id (changes periodically).
    client_id: tokio::sync::RwLock<Option<String>>,
}

impl Default for SoundCloudExtractor {
    fn default() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent("Mozilla/5.0")
                .build()
                .unwrap_or_default(),
            client_id: tokio::sync::RwLock::new(None),
        }
    }
}

#[derive(Debug, Deserialize)]
struct ScTrack {
    title: Option<String>,
    artwork_url: Option<String>,
    duration: Option<u64>,
    user: Option<ScUser>,
    description: Option<String>,
    media: Option<ScMedia>,
}

#[derive(Debug, Deserialize)]
struct ScUser {
    username: Option<String>,
}

#[derive(Debug, Deserialize)]
struct ScMedia {
    transcodings: Vec<ScTranscoding>,
}

#[derive(Debug, Deserialize)]
struct ScTranscoding {
    url: String,
    preset: String,
    format: ScFormat,
}

#[derive(Debug, Deserialize)]
struct ScFormat {
    protocol: String,
    mime_type: String,
}

#[derive(Debug, Deserialize)]
struct ScStreamUrl {
    url: String,
}

#[async_trait]
impl Extractor for SoundCloudExtractor {
    fn can_handle(&self, url: &str) -> bool {
        url.contains("soundcloud.com")
    }

    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        debug!("Extracting SoundCloud: {}", url);

        let client_id = self.get_or_discover_client_id().await?;

        // Resolve track URL to API data
        let api_url = format!(
            "https://api-v2.soundcloud.com/resolve?url={}&client_id={}",
            urlencoding::encode(url),
            client_id
        );

        let track: ScTrack = self
            .client
            .get(&api_url)
            .send()
            .await?
            .json()
            .await
            .map_err(|e| anyhow::anyhow!("Failed to resolve SoundCloud track: {}", e))?;

        let mut streams = Vec::new();

        if let Some(media) = &track.media {
            for transcoding in &media.transcodings {
                // Resolve the actual stream URL
                let stream_url = format!("{}?client_id={}", transcoding.url, client_id);
                let stream_resp: ScStreamUrl = self
                    .client
                    .get(&stream_url)
                    .send()
                    .await?
                    .json()
                    .await?;

                let codec = if transcoding.preset.contains("opus") {
                    "opus"
                } else if transcoding.preset.contains("mp3") {
                    "mp3"
                } else {
                    "aac"
                };

                let container = if transcoding.format.protocol == "hls" {
                    "m3u8"
                } else {
                    codec
                };

                streams.push(MediaStream {
                    url: stream_resp.url,
                    format_id: transcoding.preset.clone(),
                    stream_type: "audio".to_string(),
                    container: Some(container.to_string()),
                    video_codec: None,
                    audio_codec: Some(codec.to_string()),
                    height: None,
                    audio_bitrate: if transcoding.preset.contains("high") {
                        Some(256)
                    } else {
                        Some(128)
                    },
                    filesize: None,
                });
            }
        }

        Ok(ExtractedMedia {
            title: track.title.unwrap_or_else(|| "Untitled".to_string()),
            streams,
            thumbnail: track.artwork_url.map(|u| u.replace("-large", "-t500x500")),
            duration_sec: track.duration.map(|d| (d / 1000) as u32),
            uploader: track.user.and_then(|u| u.username),
            description: track.description,
        })
    }

    fn platform_name(&self) -> &str {
        "SoundCloud"
    }
}

impl SoundCloudExtractor {
    /// Get or discover the SoundCloud client_id.
    ///
    /// The client_id is embedded in SoundCloud's JavaScript bundles
    /// and changes periodically.
    async fn get_or_discover_client_id(&self) -> anyhow::Result<String> {
        {
            let cached = self.client_id.read().await;
            if let Some(ref id) = *cached {
                return Ok(id.clone());
            }
        }

        // Discover client_id from SoundCloud's main page
        let page = self
            .client
            .get("https://soundcloud.com")
            .send()
            .await?
            .text()
            .await?;

        // Find script URLs
        let script_urls: Vec<&str> = page
            .split("src=\"")
            .skip(1)
            .filter_map(|s| s.split('"').next())
            .filter(|u| u.contains("sndcdn.com") && u.ends_with(".js"))
            .collect();

        // Search each script for client_id
        for script_url in script_urls.iter().rev() {
            if let Ok(resp) = self.client.get(*script_url).send().await {
                if let Ok(js) = resp.text().await {
                    if let Some(pos) = js.find("client_id:\"") {
                        let start = pos + 11;
                        if let Some(end) = js[start..].find('"') {
                            let id = js[start..start + end].to_string();
                            *self.client_id.write().await = Some(id.clone());
                            debug!("Discovered SoundCloud client_id: {}...", &id[..8.min(id.len())]);
                            return Ok(id);
                        }
                    }
                    // Alternative pattern
                    if let Some(pos) = js.find("client_id=") {
                        let start = pos + 10;
                        if let Some(end) = js[start..].find(|c: char| !c.is_alphanumeric()) {
                            let id = js[start..start + end].to_string();
                            if id.len() > 10 {
                                *self.client_id.write().await = Some(id.clone());
                                debug!("Discovered SoundCloud client_id");
                                return Ok(id);
                            }
                        }
                    }
                }
            }
        }

        anyhow::bail!("Could not discover SoundCloud client_id")
    }
}
