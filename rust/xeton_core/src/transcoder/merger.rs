// xeton_core::transcoder::merger — Lossless audio/video muxing.

use std::path::Path;
use tracing::info;

/// Losslessly merges a video and audio file into a single output file.
pub async fn merge_video_audio(
    video_path: &Path,
    audio_path: &Path,
    output_path: &Path,
) -> anyhow::Result<()> {
    info!(
        "Merging {} and {} into {}",
        video_path.display(),
        audio_path.display(),
        output_path.display()
    );

    let video_str = video_path.to_str().ok_or_else(|| anyhow::anyhow!("Invalid video path"))?;
    let audio_str = audio_path.to_str().ok_or_else(|| anyhow::anyhow!("Invalid audio path"))?;
    let output_str = output_path.to_str().ok_or_else(|| anyhow::anyhow!("Invalid output path"))?;

    let args = [
        "-i", video_str,
        "-i", audio_str,
        "-c", "copy",
        "-y",
        output_str,
    ];

    super::ffi::run_ffmpeg_cli(&args).await
}
