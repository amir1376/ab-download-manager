package com.abdownloadmanager.android.pages.browser

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

/**
 * Process-wide singleton that owns the single [GeckoRuntime] instance.
 *
 * GeckoView strongly advises creating only one runtime per process. We lazy-initialise it on
 * the first browser tab creation and keep it alive for the whole process lifetime (Android will
 * kill the process when the app is removed from the recent-apps list anyway).
 */
object GeckoEngineProvider {

    @Volatile
    private var _runtime: GeckoRuntime? = null

    /**
     * Returns the shared [GeckoRuntime], creating it on first call.
     *
     * Must be called from the main thread (GeckoRuntime creation is synchronous and requires the
     * application [Context]).
     */
    fun getOrCreate(context: Context): GeckoRuntime {
        return _runtime ?: synchronized(this) {
            _runtime ?: buildRuntime(context.applicationContext).also { _runtime = it }
        }
    }

    /**
     * True once [getOrCreate] has been called at least once. Used by composables to decide which
     * rendering path to take without forcing eager initialisation.
     */
    val isInitialized: Boolean
        get() = _runtime != null

    // ── private ──────────────────────────────────────────────────────────────

    private fun buildRuntime(appContext: Context): GeckoRuntime {
        val settings = GeckoRuntimeSettings.Builder()
            // Never open DevTools protocol over TCP in production builds.
            .remoteDebuggingEnabled(false)
            // Suppress Gecko's own console output; the host app handles logging.
            .consoleOutput(false)
            // Disable double-tap-to-zoom; the app's pinch-zoom is sufficient.
            .doubleTapZoomingEnabled(false)
            // Prevent the engine auto-zooming into focused form inputs.
            .inputAutoZoomEnabled(false)
            // Allow HTTP loads — the app's AndroidManifest already sets
            // android:usesCleartextTraffic="true" globally.
            .allowInsecureConnections(GeckoRuntimeSettings.ALLOW_ALL)
            .build()

        return GeckoRuntime.create(appContext, settings)
    }
}
