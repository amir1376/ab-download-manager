package com.abdownloadmanager.android.pages.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.abdownloadmanager.android.ui.widget.WebView

/**
 * Renders the active tab's content using either GeckoView (Mozilla Firefox engine) or the
 * system Android WebView depending on the [WebViewHolder.isGeckoMode] flag.
 *
 * When Gecko mode is active the [WebViewHolder] is used only for its lifecycle hooks
 * ([WebViewHolder.activate] / [WebViewHolder.deactivate] / [WebViewHolder.release]); the actual
 * rendering surface is provided by [GeckoWebView] which binds the [GeckoTabState.session] to a
 * [org.mozilla.geckoview.GeckoView].
 *
 * When running in WebView fallback mode the behaviour is identical to the previous implementation.
 */
@Composable
fun ABDMWebView(
    modifier: Modifier = Modifier,
    webViewHolder: WebViewHolder,
) {
    val tab = webViewHolder.tab
    key(tab.tabId) {
        if (webViewHolder.isGeckoMode && webViewHolder.geckoTabState != null) {
            // ── GeckoView rendering path ──────────────────────────────────
            GeckoWebView(
                tabState = webViewHolder.geckoTabState,
                modifier = modifier,
            )
        } else {
            // ── System WebView fallback path ──────────────────────────────
            val wState = tab.tabState
            val navigator = webViewHolder.navigator
            WebView(
                state = wState,
                modifier = modifier,
                captureBackPresses = false,
                navigator = navigator,
                client = webViewHolder.client,
                chromeClient = webViewHolder.chromeClient,
                onDispose = {
                    webViewHolder.deactivate()
                },
                factory = {
                    // activate() returns a non-null ABDMWebView in WebView mode.
                    webViewHolder.activate(it)!!
                },
            )
        }
    }
}
