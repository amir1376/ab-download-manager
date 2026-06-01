// xeton_core::transcoder::ffi — FFmpeg CLI fallback logic

use tokio::process::Command;
use tracing::debug;

/// Runs ffmpeg via the command line interface as a fallback
/// when the native ffmpeg-next bindings are not available or fail.
pub async fn run_ffmpeg_cli(args: &[&str]) -> anyhow::Result<()> {
    debug!("Running ffmpeg CLI: ffmpeg {}", args.join(" "));
    
    let output = Command::new("ffmpeg")
        .args(args)
        .output()
        .await
        .map_err(|e| anyhow::anyhow!("Failed to run ffmpeg via CLI (is it installed?): {}", e))?;

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        anyhow::bail!("ffmpeg failed: {}", stderr.trim());
    }

    Ok(())
}
