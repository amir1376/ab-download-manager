package com.abdownloadmanager.shared.util.keepawake

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidWakeLock : KeepAwake, KoinComponent {
    val context: Context by inject()

    private val wakeLock = context.getSystemService<PowerManager>()
        ?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ABDM:Downloading"
        )

    override fun keepAwake() {
        wakeLock?.let { wakeLock ->
            if (!wakeLock.isHeld) {
                wakeLock.acquire()
            }
        }
    }

    override fun allowSleep() {
        wakeLock?.let { wakeLock ->
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
