package com.abdownloadmanager.shared.util.systemusage

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Desktop implementation of [SystemUsageMonitor].
 *
 * Uses native OS-level idle-time detection:
 * - Windows: GetLastInputInfo
 * - macOS: CoreGraphics, with an optional native fallback
 * - Linux: D-Bus and/or X11
 *
 * Monitoring is active only while the shared flow has subscribers.
 * No AWT listeners are used.
 */
class DesktopSystemUsageMonitor(
    override val idleThreshold: Duration = SystemUsageMonitor.DEFAULT_IDLE_THRESHOLD,
    private val defaultIsUserInteractingIfItsNotSupported: () -> Boolean = SystemUsageMonitor::defaultInteractingWhenIdleTimeNotSupportedByOS,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SystemUsageMonitor, AutoCloseable {

    private val closed = AtomicBoolean(false)

    private val scope = CoroutineScope(SupervisorJob() + coroutineDispatcher)

    private val idleTimeProviderDelegate = lazy {
        when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> WindowsIdleTimeProvider()
            Platform.Desktop.MacOS -> MacIdleTimeProvider()
            Platform.Desktop.Linux -> LinuxIdleTimeProvider()
        }
    }

    private val idleTimeProvider by idleTimeProviderDelegate

    private val checkInterval = minOf(
        1.seconds,
        maxOf(
            250.milliseconds,
            idleThreshold / 2
        )
    )

    init {
        require(idleThreshold.isFinite() && idleThreshold > Duration.ZERO) {
            "idleThreshold must be finite and greater than zero"
        }
    }

    /**
     * Returns true when the native provider reports recent user input.
     *
     * Throws if native idle-time detection is unavailable.
     */
    override val isUserInteracting: Boolean
        get() {
            check(!closed.get()) {
                "SystemUsageMonitor has been closed"
            }

            val idleMillis = idleTimeProvider.getIdleTimeMillis()
            // Native system idle-time detection is unavailable
            // using default supplied value
                ?: return defaultIsUserInteractingIfItsNotSupported()

            return idleMillis < idleThreshold.inWholeMilliseconds
        }

    private val rawInteractionFlow: Flow<Boolean> = flow {
        while (currentCoroutineContext().isActive) {
            emit(isUserInteracting)
            delay(checkInterval)
        }
    }

    override val isUserInteractingWithSystemFlow: Flow<Boolean> =
        rawInteractionFlow
            .distinctUntilChanged()
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 0,
                    replayExpirationMillis = 0,
                ),
                replay = 1,
            )

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        try {
            if (idleTimeProviderDelegate.isInitialized()) {
                (idleTimeProvider as? AutoCloseable)?.close()
            }
        } finally {
            scope.cancel()
        }
    }
}

actual fun platformSystemUsageMonitor(
    idleThreshold: Duration,
): SystemUsageMonitor {
    return DesktopSystemUsageMonitor(
        idleThreshold = idleThreshold,
    )
}
