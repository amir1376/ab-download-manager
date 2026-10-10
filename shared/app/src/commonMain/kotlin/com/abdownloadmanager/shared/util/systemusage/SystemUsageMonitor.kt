package com.abdownloadmanager.shared.util.systemusage

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * A multiplatform monitor that detects whether the user is actively interacting with the system,
 * including mouse movement, mouse clicks, keyboard input, and touch gestures where supported.
 *
 * Platform capabilities:
 * - **Desktop (Windows, macOS, Linux)**: Monitors system-wide input across the entire OS,
 *   detecting user interaction even when other applications have focus.
 * - **Android**: Monitors app-scoped interaction (touch, key, and gesture events within the
 *   application windows in combination with device screen interactive state), as Android's
 *   security sandbox restricts global input monitoring without Accessibility Services.
 */
interface SystemUsageMonitor {
    /**
     * Emits `true` when the user is actively interacting with the system, and `false` after
     * a period of inactivity exceeding [idleThreshold].
     *
     * Consecutive duplicate state emissions are conflated.
     * When there are active collectors, monitoring begins and resources are allocated.
     * When there are no collectors, background monitoring is paused and resources are released.
     */
    val isUserInteractingWithSystemFlow: Flow<Boolean>

    /**
     * The configured idle timeout duration after which the user is considered inactive.
     */
    val idleThreshold: Duration

    /**
     * Synchronously returns whether the user is currently considered to be actively interacting
     * with the system based on the latest evaluated state.
     */
    val isUserInteracting: Boolean

    companion object {
        /**
         * Default inactivity duration before transitioning to idle state (5 seconds).
         */
        val DEFAULT_IDLE_THRESHOLD: Duration = 5.seconds
    }
}

/**
 * Factory function creating a platform-specific [SystemUsageMonitor] instance.
 *
 * @param idleThreshold The duration of inactivity before emitting `false`.
 */
expect fun platformSystemUsageMonitor(
    idleThreshold: Duration = SystemUsageMonitor.DEFAULT_IDLE_THRESHOLD,
): SystemUsageMonitor
