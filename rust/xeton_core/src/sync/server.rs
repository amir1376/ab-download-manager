use axum::{
    routing::{get, post},
    Router, Json, extract::State,
};
use std::sync::Arc;
use tokio::sync::Mutex;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Clone)]
pub struct PushLinkRequest {
    pub url: String,
    pub peer_id: String,
}

#[derive(Serialize, Deserialize, Clone)]
pub struct DeviceInfo {
    pub name: String,
    pub platform: String,
}

#[derive(Clone)]
pub struct ServerState {
    pub device_name: String,
}

pub async fn start_server(device_name: String, port: u16) {
    let state = ServerState {
        device_name,
    };

    let app = Router::new()
        .route("/api/device-info", get(get_device_info))
        .route("/api/push-link", post(push_link))
        .route("/api/pair", post(pair))
        .with_state(Arc::new(state));

    let listener = tokio::net::TcpListener::bind(format!("0.0.0.0:{}", port)).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn get_device_info(State(state): State<Arc<ServerState>>) -> Json<DeviceInfo> {
    Json(DeviceInfo {
        name: state.device_name.clone(),
        platform: std::env::consts::OS.to_string(),
    })
}

async fn push_link(Json(payload): Json<PushLinkRequest>) -> &'static str {
    // In a real implementation, this would enqueue the download in XetonEngine
    println!("Received push link from {}: {}", payload.peer_id, payload.url);
    "OK"
}

#[derive(Deserialize)]
struct PairRequest {
    peer_id: String,
    pin: String,
}

async fn pair(Json(_payload): Json<PairRequest>) -> &'static str {
    // Handshake logic
    "PAIRED"
}
