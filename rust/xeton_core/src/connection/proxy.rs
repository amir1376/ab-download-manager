// xeton_core::connection::proxy — Proxy configuration types.
//
// Mirrors `ir.amirab.downloader.connection.proxy.*`.

use serde::{Deserialize, Serialize};

/// Proxy configuration for the HTTP client.
#[derive(Clone, Debug, Default, Serialize, Deserialize, uniffi::Enum)]
pub enum ProxyConfig {
    /// No proxy — direct connections.
    #[default]
    None,
    /// HTTP/HTTPS proxy URL (e.g., "http://proxy.example.com:8080").
    Http(String),
    /// SOCKS5 proxy URL (e.g., "socks5://proxy.example.com:1080").
    Socks5(String),
    /// Use the system's default proxy configuration.
    System,
}
