// xeton_core::extractors::social — Social media extractors.
//
// Fast-path extractors for TikTok and Instagram using basic regex scraping,
// with automatic fallback to yt-dlp for robust handling.

use async_trait::async_trait;
use regex::Regex;
use tracing::{debug, warn};

use super::ytdlp::YtDlpExtractor;
use super::{ExtractedMedia, Extractor, MediaStream};

/// Fast-path extractor for TikTok
pub struct TikTokExtractor {
    client: reqwest::Client,
    fallback: YtDlpExtractor,
}

impl Default for TikTokExtractor {
    fn default() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
                .unwrap_or_default(),
            fallback: YtDlpExtractor::default(),
        }
    }
}

#[async_trait]
impl Extractor for TikTokExtractor {
    fn can_handle(&self, url: &str) -> bool {
        url.contains("tiktok.com")
    }

    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        debug!("TikTokExtractor (fast-path) trying: {}", url);
        
        let html = match self.client.get(url).send().await {
            Ok(resp) if resp.status().is_success() => resp.text().await.unwrap_or_default(),
            _ => String::new(),
        };

        // Try to find the playAddr in the universal data
        // This is a brittle regex but serves as a fast-path
        let re = Regex::new(r#""playAddr":"([^"]+)""#).unwrap();
        if let Some(caps) = re.captures(&html) {
            let encoded_url = caps.get(1).unwrap().as_str();
            let decoded_url = encoded_url.replace("\\u002F", "/").replace("\\u0026", "&");
            
            if decoded_url.starts_with("http") {
                debug!("TikTok fast-path succeeded");
                
                // Get title (rudimentary)
                let title_re = Regex::new(r#"<title>([^<]+)</title>"#).unwrap();
                let title = title_re
                    .captures(&html)
                    .map(|c| c.get(1).unwrap().as_str().to_string())
                    .unwrap_or_else(|| "TikTok Video".to_string());
                
                return Ok(ExtractedMedia {
                    title,
                    streams: vec![MediaStream {
                        url: decoded_url,
                        format_id: "fast_mp4".to_string(),
                        stream_type: "video+audio".to_string(),
                        container: Some("mp4".to_string()),
                        video_codec: Some("h264".to_string()),
                        audio_codec: Some("aac".to_string()),
                        height: None,
                        audio_bitrate: None,
                        filesize: None,
                    }],
                    thumbnail: None,
                    duration_sec: None,
                    uploader: None,
                    description: None,
                });
            }
        }

        warn!("TikTok fast-path failed, falling back to yt-dlp");
        self.fallback.extract(url).await
    }

    fn platform_name(&self) -> &str {
        "TikTok"
    }
}

/// Fast-path extractor for Instagram
pub struct InstagramExtractor {
    client: reqwest::Client,
    fallback: YtDlpExtractor,
}

impl Default for InstagramExtractor {
    fn default() -> Self {
        Self {
            client: reqwest::Client::builder()
                .user_agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
                .unwrap_or_default(),
            fallback: YtDlpExtractor::default(),
        }
    }
}

#[async_trait]
impl Extractor for InstagramExtractor {
    fn can_handle(&self, url: &str) -> bool {
        url.contains("instagram.com")
    }

    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        debug!("InstagramExtractor (fast-path) trying: {}", url);
        
        let html = match self.client.get(url).send().await {
            Ok(resp) if resp.status().is_success() => resp.text().await.unwrap_or_default(),
            _ => String::new(),
        };

        // Instagram embedded JSON often contains "video_url"
        let re = Regex::new(r#""video_url":"([^"]+)""#).unwrap();
        if let Some(caps) = re.captures(&html) {
            let encoded_url = caps.get(1).unwrap().as_str();
            let decoded_url = encoded_url.replace("\\u002F", "/").replace("\\u0026", "&");
            
            if decoded_url.starts_with("http") {
                debug!("Instagram fast-path succeeded");
                
                return Ok(ExtractedMedia {
                    title: "Instagram Reel/Video".to_string(),
                    streams: vec![MediaStream {
                        url: decoded_url,
                        format_id: "fast_mp4".to_string(),
                        stream_type: "video+audio".to_string(),
                        container: Some("mp4".to_string()),
                        video_codec: Some("h264".to_string()),
                        audio_codec: Some("aac".to_string()),
                        height: None,
                        audio_bitrate: None,
                        filesize: None,
                    }],
                    thumbnail: None,
                    duration_sec: None,
                    uploader: None,
                    description: None,
                });
            }
        }

        warn!("Instagram fast-path failed, falling back to yt-dlp");
        self.fallback.extract(url).await
    }

    fn platform_name(&self) -> &str {
        "Instagram"
    }
}
