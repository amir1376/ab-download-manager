package com.abdownloadmanager.shared.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
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
        val callback = GlobalActivityLifecycleCallbacks {
            trySend(isDark())
        }
        val application = context.applicationContext as? Application
        application?.registerActivityLifecycleCallbacks(callback)
        awaitClose {
            application?.unregisterActivityLifecycleCallbacks(callback)
        }
    }
}

private class GlobalActivityLifecycleCallbacks(
    private val recheck: () -> Unit,
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        recheck()
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

}

