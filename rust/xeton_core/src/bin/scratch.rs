use suppaftp::tokio::{AsyncRustlsFtpStream, AsyncRustlsConnector};
use suppaftp::types::FileType;
use std::sync::Arc;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let _ = suppaftp::rustls::crypto::ring::default_provider().install_default();
    
    // Enable logging
    tracing_subscriber::fmt::init();
    
    println!("Connecting to test.rebex.net:21...");
    let mut ftp_stream = AsyncRustlsFtpStream::connect("test.rebex.net:21").await?;
    println!("Connected to control channel. Upgrading to TLS...");
    
    let mut root_store = suppaftp::rustls::RootCertStore::empty();
    root_store.extend(webpki_roots::TLS_SERVER_ROOTS.iter().cloned());
    
    let config = suppaftp::rustls::ClientConfig::builder()
        .with_root_certificates(root_store)
        .with_no_client_auth();
        
    let connector = AsyncRustlsConnector::from(suppaftp::tokio_rustls::TlsConnector::from(Arc::new(config)));
    
    ftp_stream = ftp_stream.into_secure(connector, "test.rebex.net").await?;
    println!("TLS handshake completed. Logging in...");
    
    ftp_stream.login("demo", "password").await?;
    println!("Logged in successfully. Setting transfer type to Binary...");
    
    ftp_stream.transfer_type(FileType::Binary).await?;
    
    println!("Fetching /readme.txt size...");
    let sz = ftp_stream.size("/readme.txt").await?;
    println!("Size of /readme.txt: {} bytes", sz);
    
    // Read the file as stream
    let reader = ftp_stream.retr_as_stream("/readme.txt").await?;
    let mut data = Vec::new();
    use tokio::io::AsyncReadExt;
    let mut compat_reader = reader;
    compat_reader.read_to_end(&mut data).await?;
    
    let text = String::from_utf8_lossy(&data);
    println!("--- File Content ---");
    println!("{}", text);
    println!("--------------------");
    
    // Finalize retrieve
    ftp_stream.finalize_retr_stream(compat_reader).await?;
    
    ftp_stream.quit().await?;
    println!("Done!");
    Ok(())
}
