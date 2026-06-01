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
 * - Creates the [GeckoView] once per composition key (i.e. per tab ID).
 * - Attaches `tabState.session` to the view inside `AndroidView.factory`.
 * - Detaches the session when the composable leaves the composition.
 *
 * @param tabState The [GeckoTabState] whose [org.mozilla.geckoview.GeckoSession] drives this view.
 * @param modifier Compose [Modifier] applied to the [AndroidView] container.
 */
@Composable
fun GeckoWebView(
    tabState: GeckoTabState,
    modifier: Modifier = Modifier,
) {
    // Remember a stable reference to the GeckoView so AndroidView can reuse it across
    // recompositions without recreating the native view.
    val geckoView = remember(tabState.tab.tabId) {
        // Creation is deferred until factory is called with a Context, but we capture the
        // reference here so the DisposableEffect below can clean up.
        GeckoViewHolder()
    }

    DisposableEffect(tabState.tab.tabId) {
        onDispose {
            // Detach the session from the GeckoView surface when the composable is removed.
            // The session itself remains alive (owned by GeckoTabState) so tab state is preserved.
            geckoView.view?.releaseSession()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            createGeckoView(context).also { view ->
                geckoView.view = view
                // Attach the session — this causes Gecko to render into the view's surface.
                view.setSession(tabState.session)
            }
        },
        update = { view ->
            // If the session changed (e.g. after a session restore) re-attach it.
            if (view.session !== tabState.session) {
                view.releaseSession()
                view.setSession(tabState.session)
            }
        },
        onRelease = { view ->
            view.releaseSession()
            geckoView.view = null
        },
    )
}

/** Mutable holder so [DisposableEffect] can reach the [GeckoView] created inside [AndroidView]. */
private class GeckoViewHolder {
    var view: GeckoView? = null
}

/**
 * Constructs a [GeckoView] with layout parameters matching the parent's full size.
 * We set these eagerly so Compose's measurement pass can correctly size the AndroidView container.
 */
private fun createGeckoView(context: Context): GeckoView {
    return GeckoView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }
}
