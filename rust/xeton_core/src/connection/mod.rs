// xeton_core::connection — HTTP connection management.
//
// Replaces `ir.amirab.downloader.connection.OkHttpHttpDownloaderClient`
// with reqwest (HTTP/1.1 + HTTP/2, TLS, proxy, streaming).

pub mod proxy;

use std::pin::Pin;

use bytes::Bytes;
use reqwest::header::{HeaderMap, ACCEPT_RANGES, CONTENT_LENGTH, CONTENT_RANGE, CONTENT_TYPE, ETAG, LAST_MODIFIED, RANGE};
use reqwest::{Client, Response, StatusCode};
use thiserror::Error;
use tokio_stream::Stream;
use tracing::debug;

use crate::models::{ContentRange, ResponseInfo};
use self::proxy::ProxyConfig;

// ─── Errors ─────────────────────────────────────────────────────────────────

#[derive(Debug, Error)]
pub enum ConnectionError {
    #[error("HTTP request failed: {0}")]
    Request(#[from] reqwest::Error),

    #[error("Server returned {status}: {body}")]
    ServerError { status: u16, body: String },

    #[error("Response indicates failure: {0}")]
    Unsuccessful(String),
}

// ─── HttpClient ─────────────────────────────────────────────────────────────

/// High-level HTTP client wrapping `reqwest::Client`.
///
/// - One instance per download job (shares the connection pool).
/// - Supports HTTP/1.1 and HTTP/2 multiplexing automatically.
/// - Handles Range requests, proxy, and custom user agents.
pub struct HttpClient {
    inner: Client,
    user_agent: String,
}

impl HttpClient {
    /// Build a new HTTP client with the given proxy configuration.
    pub fn new(proxy_config: &ProxyConfig, user_agent: &str) -> Result<Self, ConnectionError> {
        let mut builder = Client::builder()
            .user_agent(user_agent)
            .gzip(true)
            .brotli(true)
            .tcp_keepalive(std::time::Duration::from_secs(30))
            .pool_max_idle_per_host(32)
            .connect_timeout(std::time::Duration::from_secs(30))
            .timeout(std::time::Duration::from_secs(300));

        match proxy_config {
            ProxyConfig::None => {
                builder = builder.no_proxy();
            }
            ProxyConfig::Http(url) => {
                builder = builder.proxy(reqwest::Proxy::all(url)?);
            }
            ProxyConfig::Socks5(url) => {
                builder = builder.proxy(reqwest::Proxy::all(url)?);
            }
            ProxyConfig::System => {
                // reqwest uses system proxy by default
            }
        }

        Ok(Self {
            inner: builder.build()?,
            user_agent: user_agent.to_string(),
        })
    }

    /// Send a HEAD-like GET request to fetch server info without downloading the body.
    /// Mirrors `HttpDownloaderClient.test()`.
    pub async fn test(&self, url: &str, headers: &HeaderMap) -> Result<ResponseInfo, ConnectionError> {
        let resp = self
            .inner
            .get(url)
            .headers(headers.clone())
            .header(RANGE, "bytes=0-0")
            .send()
            .await?;

        let info = Self::extract_response_info(&resp);
        debug!("Test response for {}: status={}, resume={}", url, info.status, info.resume_support);
        Ok(info)
    }

    /// Establish a streaming connection for a byte range.
    /// Returns the response info and the body byte stream.
    /// Mirrors `HttpDownloaderClient.connect()`.
    pub async fn connect(
        &self,
        url: &str,
        headers: &HeaderMap,
        from: i64,
        to: Option<i64>,
    ) -> Result<PartConnection, ConnectionError> {
        let range = match to {
            Some(end) => format!("bytes={}-{}", from, end),
            None => format!("bytes={}-", from),
        };

        let resp = self
            .inner
            .get(url)
            .headers(headers.clone())
            .header(RANGE, &range)
            .send()
            .await?;

        let status = resp.status();
        if !status.is_success() && status != StatusCode::PARTIAL_CONTENT {
            let body = resp.text().await.unwrap_or_default();
            return Err(ConnectionError::ServerError {
                status: status.as_u16(),
                body,
            });
        }

        let info = Self::extract_response_info(&resp);
        let content_length = info.content_length.unwrap_or(-1);
        let stream = resp.bytes_stream();

        Ok(PartConnection {
            info,
            content_length,
            stream: Box::pin(stream),
        })
    }

    /// Parse response headers into `ResponseInfo`.
    fn extract_response_info(resp: &Response) -> ResponseInfo {
        let headers = resp.headers();
        let status = resp.status().as_u16();

        // Resume support: 206 Partial Content or Accept-Ranges: bytes
        let resume_support = status == 206
            || headers
                .get(ACCEPT_RANGES)
                .and_then(|v| v.to_str().ok())
                .is_some_and(|v| v.contains("bytes"));

        // Total length from Content-Range header (full file size)
        let content_range = Self::parse_content_range(headers);
        let total_length = content_range
            .as_ref()
            .and_then(|cr| cr.total)
            .or_else(|| {
                headers
                    .get(CONTENT_LENGTH)
                    .and_then(|v| v.to_str().ok())
                    .and_then(|v| v.parse::<i64>().ok())
            });

        let content_length = headers
            .get(CONTENT_LENGTH)
            .and_then(|v| v.to_str().ok())
            .and_then(|v| v.parse::<i64>().ok());

        let etag = headers
            .get(ETAG)
            .and_then(|v| v.to_str().ok())
            .map(|s| s.to_string());

        let last_modified = headers
            .get(LAST_MODIFIED)
            .and_then(|v| v.to_str().ok())
            .map(|s| s.to_string());

        let is_webpage = headers
            .get(CONTENT_TYPE)
            .and_then(|v| v.to_str().ok())
            .is_some_and(|ct| ct.contains("text/html"));

        ResponseInfo {
            status,
            resume_support,
            total_length,
            content_length,
            content_range,
            etag,
            last_modified,
            is_webpage,
        }
    }

    /// Parse `Content-Range: bytes 0-999/5000` header.
    fn parse_content_range(headers: &HeaderMap) -> Option<ContentRange> {
        let value = headers.get(CONTENT_RANGE)?.to_str().ok()?;
        // Format: "bytes start-end/total" or "bytes start-end/*"
        let bytes_part = value.strip_prefix("bytes ")?;
        let (range_part, total_part) = bytes_part.split_once('/')?;
        let (start_str, end_str) = range_part.split_once('-')?;
        let start = start_str.parse::<i64>().ok()?;
        let end = end_str.parse::<i64>().ok()?;
        let total = if total_part == "*" {
            None
        } else {
            total_part.parse::<i64>().ok()
        };
        Some(ContentRange { start, end, total })
    }

    /// Get a reference to the inner reqwest client for advanced use.
    pub fn inner(&self) -> &Client {
        &self.inner
    }
}

// ─── PartConnection ─────────────────────────────────────────────────────────

/// An active connection streaming bytes for a specific range.
/// Mirrors `ir.amirab.downloader.connection.Connection<HttpResponseInfo>`.
pub struct PartConnection {
    pub info: ResponseInfo,
    pub content_length: i64,
    pub stream: Pin<Box<dyn Stream<Item = Result<Bytes, reqwest::Error>> + Send>>,
}

// ─── User Agent ─────────────────────────────────────────────────────────────

/// Default user agent string.
pub const DEFAULT_USER_AGENT: &str =
    "Xeton Download Manager/1.0 (https://github.com/AminBhst/xeton-download-manager)";
