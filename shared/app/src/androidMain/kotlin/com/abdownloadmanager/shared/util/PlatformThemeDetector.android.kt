package com.abdownloadmanager.shared.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual typealias PlatformThemeDetector = AndroidSystemThemeDetector

class AndroidSystemThemeDetector(
    private val context: Context,
) : ISystemThemeDetector {
    override val isSupported: Boolean = true
    override fun isDark(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    override val systemThemeFlow: Flow<Boolean> = callbackFlow {
        trySend(isDark())
        val intentFilter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isDark())
            }
        }
        ContextCompat.registerReceiver(context, receiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}
