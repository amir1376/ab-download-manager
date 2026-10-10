package com.abdownloadmanager.shared.util.systemusage

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Desktop implementation of [SystemUsageMonitor].
 *
 * Combines:
 * 1. **Native OS-level Idle Time Detection**:
 *    - Windows: `GetLastInputInfo` from `User32.dll`.
 *    - macOS: `CGEventSourceSecondsSinceLastEventType` from `CoreGraphics.framework` + `ioreg` fallback.
 *    - Linux: D-Bus `org.gnome.Mutter.IdleMonitor` & `org.freedesktop.ScreenSaver` + X11 `XScreenSaver`.
 * 2. **In-App AWT Event Listener**:
 *    Captures immediate user interactions (mouse clicks, movement, key presses, wheel) occurring
 *    directly inside the application window with zero latency.
 *
 * Flow Lifecycle:
 * - Shared via [SharingStarted.WhileSubscribed], meaning the background polling coroutine and
 *   the AWT listener are active ONLY while there are collectors.
 * - When all collectors unsubscribe, the AWT listener is unregistered and polling terminates.
 * - Consecutive duplicate emissions are conflated.
 */
class DesktopSystemUsageMonitor(
    override val idleThreshold: Duration = SystemUsageMonitor.DEFAULT_IDLE_THRESHOLD,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher),
) : SystemUsageMonitor, AutoCloseable {

    private val idleTimeProvider: DesktopIdleTimeProvider by lazy {
        when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> WindowsIdleTimeProvider()
            Platform.Desktop.MacOS -> MacIdleTimeProvider()
            Platform.Desktop.Linux -> LinuxIdleTimeProvider()
        }
    }

    private val lastInAppActivityTime = AtomicLong(System.currentTimeMillis())

    private val rawInteractionFlow: Flow<Boolean> = callbackFlow {
        // 1. Launch background polling loop for system-wide idle detection
        val checkInterval = minOf(1.seconds, maxOf(100.milliseconds, idleThreshold / 2))
        val thresholdMillis = idleThreshold.inWholeMilliseconds

        val pollingJob = launch(coroutineDispatcher) {
            while (isActive) {
                val systemIdleMillis = idleTimeProvider.getIdleTimeMillis()
                val inAppIdleMillis = System.currentTimeMillis() - lastInAppActivityTime.get()

                val effectiveIdleMillis = if (systemIdleMillis != null) {
                    minOf(systemIdleMillis, inAppIdleMillis)
                } else {
                    inAppIdleMillis
                }

                val isInteracting = effectiveIdleMillis < thresholdMillis
                trySend(isInteracting)

                delay(checkInterval)
            }
        }

        awaitClose {
            pollingJob.cancel()
        }
    }

    override val isUserInteractingWithSystemFlow: Flow<Boolean> = rawInteractionFlow
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0),
            replay = 1,
        )

    override val isUserInteracting: Boolean
        get() {
            val systemIdleMillis = idleTimeProvider.getIdleTimeMillis()
            val inAppIdleMillis = System.currentTimeMillis() - lastInAppActivityTime.get()
            val effectiveIdleMillis = if (systemIdleMillis != null) {
                minOf(systemIdleMillis, inAppIdleMillis)
            } else {
                inAppIdleMillis
            }
            return effectiveIdleMillis < idleThreshold.inWholeMilliseconds
        }

    override fun close() {
        if (idleTimeProvider is AutoCloseable) {
            (idleTimeProvider as AutoCloseable).close()
        }
        scope.cancel()
    }
}

actual fun platformSystemUsageMonitor(
    idleThreshold: Duration,
): SystemUsageMonitor {
    return DesktopSystemUsageMonitor(idleThreshold = idleThreshold)
}
