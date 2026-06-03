use std::sync::Arc;
use tokio::sync::Mutex;

pub struct PeerDevice {
    pub id: String,
    pub name: String,
    pub ip: String,
    pub port: u16,
}

pub struct SyncService {
    device_name: String,
    devices: Arc<Mutex<Vec<PeerDevice>>>,
}

impl SyncService {
    pub fn new(device_name: String) -> Arc<Self> {
        Arc::new(Self {
            device_name,
            devices: Arc::new(Mutex::new(Vec::new())),
        })
    }

    pub async fn start_discovery(&self) {
        println!("Starting mDNS discovery for device: {}", self.device_name);
        // mdns-sd logic would go here
    }

    pub async fn stop_discovery(&self) {
        println!("Stopping mDNS discovery");
    }

    pub async fn discovered_devices(&self) -> Vec<PeerDevice> {
        let lock = self.devices.lock().await;
        // Need to clone manually because we return Vec<PeerDevice> to UniFFI, so fields need Clone.
        // Actually UniFFI generates the conversion for Records, but we need to derive Clone.
        lock.iter().map(|d| PeerDevice {
            id: d.id.clone(),
            name: d.name.clone(),
            ip: d.ip.clone(),
            port: d.port,
        }).collect()
    }

    pub async fn push_link(&self, peer_id: String, url: String) {
        println!("Pushing link {} to peer {}", url, peer_id);
    }

    pub async fn pair(&self, peer_id: String, pin: String) {
        println!("Pairing with peer {} using PIN {}", peer_id, pin);
    }
}
