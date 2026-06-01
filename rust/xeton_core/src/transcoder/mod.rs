// xeton_core::transcoder — FFmpeg wrappers for media processing.

pub mod ffi;
pub mod merger;
pub mod extractor;

#[derive(Clone, Debug, uniffi::Enum)]
pub enum AudioFormat {
    Mp3,
    Aac,
    Opus,
    Flac,
}
