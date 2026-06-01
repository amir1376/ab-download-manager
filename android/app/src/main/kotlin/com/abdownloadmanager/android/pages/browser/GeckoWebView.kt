package com.abdownloadmanager.android.pages.browser

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoView

/**
 * A Compose wrapper around [GeckoView] that binds the provided [GeckoTabState]'s session to the
 * native view and manages attach/detach of the session surface.
 *
 * The composable:
 * - Ensures [GeckoTabState.open] is called once the [Context] is available (inside `factory`).
 * - Creates the [GeckoView] once per composition key (i.e. per tab ID).
 * - Attaches `tabState.session` to the view's surface.
 * - Detaches the session when the composable leaves the composition via [DisposableEffect].
 *
 * @param tabState The [GeckoTabState] whose [org.mozilla.geckoview.GeckoSession] drives this view.
 * @param modifier Compose [Modifier] applied to the [AndroidView] container.
 */
@Composable
fun GeckoWebView(
    tabState: GeckoTabState,
    modifier: Modifier = Modifier,
) {
    // Remember a stable reference so the DisposableEffect below can release the session surface
    // even though the GeckoView is created inside AndroidView.factory.
    val nativeViewRef = remember(tabState.tab.tabId) { GeckoViewRef() }

    DisposableEffect(tabState.tab.tabId) {
        onDispose {
            // Release the session from the GeckoView surface when this composable is removed.
            // The GeckoSession itself remains alive (it is owned by GeckoTabState / BrowserComponent)
            // so tab state (history, scroll position) is preserved for later re-attachment.
            nativeViewRef.view?.releaseSession()
            nativeViewRef.view = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            // Ensure the Gecko session is open against the process-wide runtime.
            val runtime = GeckoEngineProvider.getOrCreate(context)
            tabState.open(runtime)

            createGeckoView(context).also { view ->
                nativeViewRef.view = view
                view.setSession(tabState.session)
            }
        },
        update = { view ->
            // Re-attach the session if it changed between recompositions (e.g. after process death
            // and a session restore produces a new GeckoSession instance).
            if (view.session !== tabState.session) {
                view.releaseSession()
                view.setSession(tabState.session)
            }
        },
        onRelease = { view ->
            view.releaseSession()
            nativeViewRef.view = null
        },
    )
}

/** Mutable holder allowing [DisposableEffect] to reach the [GeckoView] created in [AndroidView]. */
private class GeckoViewRef {
    var view: GeckoView? = null
}

/**
 * Constructs a [GeckoView] with layout parameters matching the parent container.
 * Setting these eagerly prevents a zero-size measurement on the first layout pass.
 */
private fun createGeckoView(context: Context): GeckoView {
    return GeckoView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }
}
