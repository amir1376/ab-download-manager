// src/bin/xeton_native_host.rs
//
// Native Messaging Host binary for communicating with browser extensions.
// Implements the standard 4-byte length prefix standard I/O protocol.

use serde::{Deserialize, Serialize};
use std::io::{self, Read, Write};

#[derive(Deserialize, Debug)]
struct IncomingMessage {
    action: String,
    url: String,
    title: String,
}

#[derive(Serialize, Debug)]
struct OutgoingMessage {
    status: String,
    message: String,
}

/// Reads a single native message from stdin.
/// Returns None on EOF.
fn read_message() -> io::Result<Option<Vec<u8>>> {
    let mut length_bytes = [0u8; 4];
    let mut stdin = io::stdin();

    match stdin.read_exact(&mut length_bytes) {
        Ok(_) => {}
        Err(e) if e.kind() == io::ErrorKind::UnexpectedEof => return Ok(None),
        Err(e) => return Err(e),
    }

    // Chrome uses native byte order (usually little-endian on modern systems)
    let length = u32::from_ne_bytes(length_bytes) as usize;
    
    // Prevent arbitrarily large allocations
    if length > 1024 * 1024 * 10 {
        return Err(io::Error::new(
            io::ErrorKind::InvalidData,
            "Message too large",
        ));
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
    // Write 4-byte length
    stdout.write_all(&length.to_ne_bytes())?;
    // Write JSON payload
    stdout.write_all(bytes)?;
    stdout.flush()?;

    Ok(())
}

fn main() {
    // The native host runs in a loop until stdin is closed by the browser
    loop {
        match read_message() {
            Ok(Some(bytes)) => {
                let msg_str = String::from_utf8_lossy(&bytes);
                
                // Parse the incoming JSON message
                match serde_json::from_str::<IncomingMessage>(&msg_str) {
                    Ok(parsed) => {
                        // Normally, this is where we would forward the request to the
                        // running `xeton_core` daemon via IPC, sockets, or gRPC.
                        // For this phase, we acknowledge receipt.
                        
                        let response = OutgoingMessage {
                            status: "success".to_string(),
                            message: format!("Enqueued '{}': {}", parsed.action, parsed.title),
                        };
                        
                        if write_message(&response).is_err() {
                            break;
                        }
                    }
                    Err(e) => {
                        let response = OutgoingMessage {
                            status: "error".to_string(),
                            message: format!("Invalid message format: {}", e),
                        };
                        
                        if write_message(&response).is_err() {
                            break;
                        }
                    }
                }
            }
            Ok(None) => {
                // EOF received (browser closed connection)
                break;
            }
            Err(_e) => {
                // I/O error, abort loop
                break;
            }
        }
    }
}
