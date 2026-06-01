// xeton_core::extractors::ytdlp — yt-dlp subprocess extractor.
//
// Handles: YouTube, TikTok, Instagram, Twitter/X, Facebook, Vimeo,
// Dailymotion, and 1800+ other sites supported by yt-dlp.
//
// Spawns `yt-dlp --dump-json --no-playlist <url>` and parses the JSON output.

use std::path::PathBuf;
use std::process::Stdio;

use async_trait::async_trait;
use serde::Deserialize;
use tokio::process::Command;
use tracing::{debug, warn};

use super::{ExtractedMedia, Extractor, MediaStream};

/// Extractor that delegates to the `yt-dlp` command-line tool.
pub struct YtDlpExtractor {
    /// Path to the yt-dlp binary. Defaults to "yt-dlp" (on PATH).
    pub binary_path: PathBuf,
}

impl Default for YtDlpExtractor {
    fn default() -> Self {
        Self {
            binary_path: PathBuf::from("yt-dlp"),
        }
    }
}

/// yt-dlp JSON output format (subset of fields we care about).
#[derive(Debug, Deserialize)]
struct YtDlpOutput {
    title: Option<String>,
    thumbnail: Option<String>,
    duration: Option<f64>,
    uploader: Option<String>,
    description: Option<String>,
    formats: Option<Vec<YtDlpFormat>>,
    #[serde(default)]
    requested_formats: Vec<YtDlpFormat>,
}

#[derive(Debug, Deserialize)]
struct YtDlpFormat {
    url: Option<String>,
    format_id: Option<String>,
    ext: Option<String>,
    vcodec: Option<String>,
    acodec: Option<String>,
    height: Option<u32>,
    abr: Option<f64>,
    filesize: Option<i64>,
    filesize_approx: Option<i64>,
}

#[async_trait]
impl Extractor for YtDlpExtractor {
    fn can_handle(&self, url: &str) -> bool {
        // yt-dlp handles a very wide range of sites.
        // We match the most common ones explicitly and fall back to yt-dlp for anything else.
        let patterns = [
            "youtube.com",
            "youtu.be",
            "tiktok.com",
            "instagram.com",
            "twitter.com",
            "x.com",
            "facebook.com",
            "fb.watch",
            "vimeo.com",
            "dailymotion.com",
            "twitch.tv",
            "reddit.com",
            "pinterest.com",
        ];
        patterns.iter().any(|p| url.contains(p))
    }

    async fn extract(&self, url: &str) -> anyhow::Result<ExtractedMedia> {
        debug!("Extracting with yt-dlp: {}", url);

        let output = Command::new(&self.binary_path)
            .args([
                "--dump-json",
                "--no-playlist",
                "--no-warnings",
                "--skip-download",
                url,
            ])
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
            .output()
            .await
            .map_err(|e| {
                anyhow::anyhow!(
                    "Failed to run yt-dlp (is it installed?): {}. \
                     Install with: pip install yt-dlp",
                    e
                )
            })?;

        if !output.status.success() {
            let stderr = String::from_utf8_lossy(&output.stderr);
            anyhow::bail!("yt-dlp failed: {}", stderr.trim());
        }

        let stdout = String::from_utf8_lossy(&output.stdout);
        let parsed: YtDlpOutput = serde_json::from_str(&stdout)
            .map_err(|e| anyhow::anyhow!("Failed to parse yt-dlp output: {}", e))?;

        // Build streams from formats
        let mut streams = Vec::new();

        let format_list = if !parsed.requested_formats.is_empty() {
            &parsed.requested_formats
        } else {
            parsed.formats.as_ref().map(|f| f.as_slice()).unwrap_or(&[])
        };

        for fmt in format_list {
            let url = match &fmt.url {
                Some(u) if !u.is_empty() => u.clone(),
                _ => continue,
            };

            let has_video = fmt
                .vcodec
                .as_ref()
                .is_some_and(|v| v != "none");
            let has_audio = fmt
                .acodec
                .as_ref()
                .is_some_and(|a| a != "none");

            let stream_type = match (has_video, has_audio) {
                (true, true) => "video+audio",
                (true, false) => "video",
                (false, true) => "audio",
                (false, false) => continue,
            };

            streams.push(MediaStream {
                url,
                format_id: fmt.format_id.clone().unwrap_or_default(),
                stream_type: stream_type.to_string(),
                container: fmt.ext.clone(),
                video_codec: fmt.vcodec.clone().filter(|v| v != "none"),
                audio_codec: fmt.acodec.clone().filter(|a| a != "none"),
                height: fmt.height,
                audio_bitrate: fmt.abr.map(|b| b as u32),
                filesize: fmt.filesize.or(fmt.filesize_approx),
            });
        }

        Ok(ExtractedMedia {
            title: parsed.title.unwrap_or_else(|| "Untitled".to_string()),
            streams,
            thumbnail: parsed.thumbnail,
            duration_sec: parsed.duration.map(|d| d as u32),
            uploader: parsed.uploader,
            description: parsed.description,
        })
    }

    fn platform_name(&self) -> &str {
        "yt-dlp"
    }
}
