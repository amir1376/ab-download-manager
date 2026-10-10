package com.abdownloadmanager.shared.util.systemusage

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration

/**
 * Android implementation of [SystemUsageMonitor].
 *
 * ### Platform Capabilities & Limitations:
 * On Android, the OS security sandbox isolates applications and strictly prohibits non-system
 * apps from observing touch, keyboard, or gesture events across other applications unless configured
 * as an Accessibility Service (which introduces privacy/security concerns and Google Play policy violations).
 *
 * To monitor system-wide user presence across other applications without root or invasive permissions:
 * - **Interactive Display State**: Uses [PowerManager.isInteractive] to determine if the screen is turned
 *   on and actively presenting content to the user.
 * - **Keyguard / Lock State**: Uses [KeyguardManager.isKeyguardLocked] to ensure the device is not merely
 *   woken up by background notifications while locked.
 * - **Broadcast Events**: Dynamically listens to [Intent.ACTION_SCREEN_ON], [Intent.ACTION_SCREEN_OFF],
 *   and [Intent.ACTION_USER_PRESENT] to react immediately to user wake, lock, and unlock transitions.
 */
class AndroidSystemUsageMonitor(
    context: Context? = null,
    override val idleThreshold: Duration = SystemUsageMonitor.DEFAULT_IDLE_THRESHOLD,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher),
) : SystemUsageMonitor, KoinComponent, AutoCloseable {

    private val injectedContext: Context by inject()
    val context: Context = context ?: injectedContext

    private val powerManager: PowerManager? by lazy {
        this.context.getSystemService<PowerManager>()
    }

    private val keyguardManager: KeyguardManager? by lazy {
        this.context.getSystemService<KeyguardManager>()
    }

    private fun checkIsUserInteracting(): Boolean {
        val isInteractive = powerManager?.isInteractive ?: false
        if (!isInteractive) return false
        val isLocked = keyguardManager?.isKeyguardLocked ?: false
        return !isLocked
    }

    private val rawInteractionFlow: Flow<Boolean> = callbackFlow {
        // Emit current state immediately upon subscription
        trySend(checkIsUserInteracting())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        trySend(false)
                    }
                    Intent.ACTION_SCREEN_ON,
                    Intent.ACTION_USER_PRESENT -> {
                        trySend(checkIsUserInteracting())
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        runCatching {
            this@AndroidSystemUsageMonitor.context.registerReceiver(receiver, filter)
        }

        awaitClose {
            runCatching {
                this@AndroidSystemUsageMonitor.context.unregisterReceiver(receiver)
            }
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
        get() = checkIsUserInteracting()

    override fun close() {
        scope.cancel()
    }
}

actual fun platformSystemUsageMonitor(
    idleThreshold: Duration,
): SystemUsageMonitor {
    return AndroidSystemUsageMonitor(idleThreshold = idleThreshold)
}
