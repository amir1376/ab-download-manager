// xeton_core::throttle — Token-bucket rate limiter.
//
// Replaces `okio.Throttler` used in Kotlin for both global and per-job speed limits.
// Uses `governor` crate for a non-blocking, async-compatible token-bucket.

use std::num::NonZeroU32;
use std::sync::Arc;
use std::sync::atomic::{AtomicI64, Ordering};

use governor::{Quota, RateLimiter, clock::DefaultClock, state::{InMemoryState, NotKeyed}};

/// Async-compatible rate limiter wrapping `governor::RateLimiter`.
///
/// When the speed limit is 0 (unlimited), `acquire()` is a no-op.
/// The limiter operates on byte counts, not request counts.
pub struct Throttler {
    /// Current speed limit in bytes per second. 0 = unlimited.
    limit_bps: AtomicI64,
    /// The underlying governor rate limiter. `None` when unlimited.
    limiter: tokio::sync::RwLock<Option<Arc<RateLimiter<NotKeyed, InMemoryState, DefaultClock>>>>,
}

impl Throttler {
    /// Create a new throttler with the given bytes-per-second limit.
    /// Pass 0 for unlimited.
    pub fn new(bytes_per_second: i64) -> Self {
        let limiter = Self::build_limiter(bytes_per_second);
        Self {
            limit_bps: AtomicI64::new(bytes_per_second),
            limiter: tokio::sync::RwLock::new(limiter),
        }
    }

    /// Create an unlimited throttler.
    pub fn unlimited() -> Self {
        Self::new(0)
    }

    /// Update the speed limit. 0 = unlimited.
    pub async fn set_limit(&self, bytes_per_second: i64) {
        let old = self.limit_bps.swap(bytes_per_second, Ordering::Relaxed);
        if old != bytes_per_second {
            let new_limiter = Self::build_limiter(bytes_per_second);
            let mut guard = self.limiter.write().await;
            *guard = new_limiter;
        }
    }

    /// Current limit in bytes per second. 0 = unlimited.
    pub fn limit(&self) -> i64 {
        self.limit_bps.load(Ordering::Relaxed)
    }

    /// Acquire permission to transfer `byte_count` bytes.
    ///
    /// If the limiter is active, this will asynchronously wait until enough
    /// tokens are available. If unlimited, returns immediately.
    pub async fn acquire(&self, byte_count: u32) {
        if byte_count == 0 {
            return;
        }
        let guard = self.limiter.read().await;
        if let Some(limiter) = guard.as_ref() {
            // Governor works with NonZeroU32 cell counts.
            // We split large requests into governor-sized chunks to avoid
            // exceeding the burst capacity.
            let max_burst = self.limit_bps.load(Ordering::Relaxed).max(1) as u32;
            let mut remaining = byte_count;
            while remaining > 0 {
                let chunk = remaining.min(max_burst);
                if let Some(nz) = NonZeroU32::new(chunk) {
                    limiter.until_n_ready(nz).await.ok();
                }
                remaining -= chunk;
            }
        }
    }

    /// Build the governor rate limiter for the given bps, or None if unlimited.
    fn build_limiter(
        bytes_per_second: i64,
    ) -> Option<Arc<RateLimiter<NotKeyed, InMemoryState, DefaultClock>>> {
        if bytes_per_second <= 0 {
            return None;
        }

        let bps = bytes_per_second as u32;
        // Burst size: allow up to 1 second worth of data to be acquired at once.
        let burst = NonZeroU32::new(bps).unwrap_or(NonZeroU32::MIN);
        let quota = Quota::per_second(burst);
        Some(Arc::new(RateLimiter::direct(quota)))
    }
}

impl Default for Throttler {
    fn default() -> Self {
        Self::unlimited()
    }
}

/// A chain of throttlers applied in sequence (global + per-job).
pub struct ThrottleChain {
    throttlers: Vec<Arc<Throttler>>,
}

impl ThrottleChain {
    pub fn new(throttlers: Vec<Arc<Throttler>>) -> Self {
        Self { throttlers }
    }

    /// Acquire permission from all throttlers in the chain.
    pub async fn acquire(&self, byte_count: u32) {
        for throttler in &self.throttlers {
            throttler.acquire(byte_count).await;
        }
    }
}
