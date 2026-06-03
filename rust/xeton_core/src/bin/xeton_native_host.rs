// src/bin/xeton_native_host.rs
//
// Native Messaging Host binary for communicating with browser extensions.
// Implements the standard 4-byte length prefix standard I/O protocol.

use serde::{Deserialize, Serialize};
use std::io::{self, Read, Write};
use std::net::TcpStream;
use std::process::Command;
use std::time::Duration;

#[derive(Deserialize, Debug)]
struct IncomingMessage {
    action: String,
    url: String,
    title: Option<String>,
    referrer: Option<String>,
    cookies: Option<String>,
    user_agent: Option<String>,
}

#[derive(Serialize, Debug)]
struct OutgoingMessage {
    status: String,
    message: String,
}

// Structures to match AddDownloadsFromIntegration JSON payload
#[derive(Serialize)]
struct HttpDownloadCredentials {
    #[serde(rename = "type")]
    type_name: String,
    link: String,
    headers: std::collections::HashMap<String, String>,
    #[serde(rename = "downloadPage")]
    download_page: Option<String>,
    #[serde(rename = "suggestedName")]
    suggested_name: Option<String>,
}

#[derive(Serialize)]
struct AddDownloadOptions {
    #[serde(rename = "silentAdd")]
    silent_add: bool,
    #[serde(rename = "silentStart")]
    silent_start: bool,
}

#[derive(Serialize)]
struct AddDownloadsPayload {
    items: Vec<HttpDownloadCredentials>,
    options: AddDownloadOptions,
}

/// Reads a single native message from stdin.
fn read_message() -> io::Result<Option<Vec<u8>>> {
    let mut length_bytes = [0u8; 4];
    let mut stdin = io::stdin();

    match stdin.read_exact(&mut length_bytes) {
        Ok(_) => {}
        Err(e) if e.kind() == io::ErrorKind::UnexpectedEof => return Ok(None),
        Err(e) => return Err(e),
    }

    let length = u32::from_ne_bytes(length_bytes) as usize;
    if length > 1024 * 1024 * 10 {
        return Err(io::Error::new(io::ErrorKind::InvalidData, "Message too large"));
    }

    let mut message_bytes = vec![0u8; length];
    stdin.read_exact(&mut message_bytes)?;

    Ok(Some(message_bytes))
}

/// Writes a single native message to stdout.
fn write_message(msg: &OutgoingMessage) -> io::Result<()> {
    let serialized = serde_json::to_string(msg).unwrap();
    let bytes = serialized.as_bytes();
    let length = bytes.len() as u32;

    let mut stdout = io::stdout();
    stdout.write_all(&length.to_ne_bytes())?;
    stdout.write_all(bytes)?;
    stdout.flush()?;

    Ok(())
}

/// Gets the integration port from the running instance by calling the main executable.
fn get_integration_port() -> Option<u16> {
    // We try multiple binary names. "ABDownloadManager" is the default install name.
    // "xeton" is the future rebranding.
    let bin_names = ["ABDownloadManager", "xeton", "abdownloadmanager"];
    
    for bin in bin_names {
        if let Ok(output) = Command::new(bin).arg("--get-integration-port").output() {
            if output.status.success() {
                let stdout = String::from_utf8_lossy(&output.stdout);
                if let Ok(port) = stdout.trim().parse::<u16>() {
                    // IntegrationPortBroadcaster.INTEGRATION_UNKNOWN is likely <= 0, 
                    // but parse::<u16>() will fail for negative numbers or 0.
                    if port > 0 {
                        return Some(port);
                    }
                }
            }
        }
    }
    None
}

/// Sends the payload to the running desktop app with retries.
fn send_to_app(payload: &AddDownloadsPayload) -> Result<(), String> {
    let max_retries = 3;
    let mut last_err = String::new();

    for attempt in 1..=max_retries {
        let port = match get_integration_port() {
            Some(p) => p,
            None => {
                last_err = "Could not discover integration port. Is Xeton running?".to_string();
                std::thread::sleep(Duration::from_millis(500));
                continue;
            }
        };

        let json = serde_json::to_string(payload).map_err(|e| e.to_string())?;
        
        // Simple raw HTTP POST over TCP stream to avoid heavy dependencies
        match TcpStream::connect(format!("127.0.0.1:{}", port)) {
            Ok(mut stream) => {
                let request = format!(
                    "POST /add HTTP/1.1\r\n\
                    Host: 127.0.0.1:{}\r\n\
                    Content-Type: application/json\r\n\
                    Content-Length: {}\r\n\
                    Connection: close\r\n\
                    \r\n\
                    {}",
                    port,
                    json.len(),
                    json
                );
                
                if let Err(e) = stream.write_all(request.as_bytes()) {
                    last_err = format!("Failed to write to integration server: {}", e);
                } else {
                    return Ok(()); // Success
                }
            }
            Err(e) => {
                last_err = format!("Connection failed to port {}: {}", port, e);
            }
        }

        std::thread::sleep(Duration::from_millis(500));
    }

    Err(format!("Failed after {} attempts: {}", max_retries, last_err))
}

fn main() {
    loop {
        match read_message() {
            Ok(Some(bytes)) => {
                let msg_str = String::from_utf8_lossy(&bytes);
                match serde_json::from_str::<IncomingMessage>(&msg_str) {
                    Ok(parsed) => {
                        if parsed.action == "add_download" || parsed.action == "add_hls_stream" {
                            let mut headers = std::collections::HashMap::new();
                            if let Some(r) = parsed.referrer.clone() {
                                headers.insert("Referer".to_string(), r);
                            }
                            if let Some(c) = parsed.cookies.clone() {
                                headers.insert("Cookie".to_string(), c);
                            }
                            if let Some(ua) = parsed.user_agent.clone() {
                                headers.insert("User-Agent".to_string(), ua);
                            }

                            let creds = HttpDownloadCredentials {
                                type_name: if parsed.action == "add_hls_stream" { "hls".to_string() } else { "http".to_string() },
                                link: parsed.url.clone(),
                                headers,
                                download_page: parsed.referrer.clone(),
                                suggested_name: parsed.title.clone(),
                            };

                            let payload = AddDownloadsPayload {
                                items: vec![creds],
                                options: AddDownloadOptions {
                                    silent_add: false,
                                    silent_start: false,
                                },
                            };

                            match send_to_app(&payload) {
                                Ok(_) => {
                                    let _ = write_message(&OutgoingMessage {
                                        status: "success".to_string(),
                                        message: "Enqueued successfully".to_string(),
                                    });
                                }
                                Err(e) => {
                                    let _ = write_message(&OutgoingMessage {
                                        status: "error".to_string(),
                                        message: e,
                                    });
                                }
                            }
                        } else {
                            let _ = write_message(&OutgoingMessage {
                                status: "error".to_string(),
                                message: format!("Unknown action: {}", parsed.action),
                            });
                        }
                    }
                    Err(e) => {
                        let _ = write_message(&OutgoingMessage {
                            status: "error".to_string(),
                            message: format!("Invalid message format: {}", e),
                        });
                    }
                }
            }
            Ok(None) => break,
            Err(_) => break,
        }
    }
}
