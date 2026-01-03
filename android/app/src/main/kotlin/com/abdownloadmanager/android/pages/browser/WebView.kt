package com.abdownloadmanager.android.pages.browser

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.abdownloadmanager.android.ui.PullToRefreshBox
import com.abdownloadmanager.android.ui.PullToRefreshState
import com.abdownloadmanager.android.ui.rememberPullToRefreshState
import com.abdownloadmanager.android.ui.widget.LoadingState
import com.abdownloadmanager.android.ui.widget.WebView
import kotlin.math.roundToInt


@Composable
fun ABDMWebView(
    modifier: Modifier = Modifier,
    pullToRefreshState: PullToRefreshState = rememberPullToRefreshState(),
    webViewHolder: WebViewHolder,
) {
    val tab = webViewHolder.tab
    key(tab.tabId) {
        val wState = tab.tabState
        val navigator = webViewHolder.navigator
        PullToRefreshBox(
            modifier = modifier,
            isRefreshing = wState.isPullToRefreshInProgress,
            onRefresh = {
                if (wState.loadingState is LoadingState.Finished) {
                    wState.isPullToRefreshInProgress = true
                    navigator.reload()
                }
            },
            state = pullToRefreshState
        ) {
            /**
             * Although WebView is inherently scrollable, we apply a scrollable modifier
             * and manage scrolling manually via scrollableState to support pull-to-refresh.
             *
             * Because the pullToRefresh requires a scrollable child, which is why this
             * additional scroll handling is necessary.
             */
            val scrollState = rememberScrollableState(
                consumeScrollDelta = { scroll ->
                    wState.webView
                        ?.let { view ->
                            val inverseScroll = -scroll
                            val consumed = when {

                                inverseScroll < 0 -> {
                                    inverseScroll.coerceAtLeast(-view.scrollY.toFloat())
                                }

                                inverseScroll > 0 -> {
                                    if (view.canScrollVertically(1)) {
                                        inverseScroll
                                    } else 0f
                                }

                                else -> 0f
                            }
                            view.scrollBy(0, consumed.roundToInt())
                            -consumed
                        } ?: 0f
                }
            )
            WebView(
                state = wState,
                modifier = Modifier
                    .fillMaxSize()
                    .scrollable(
                        scrollState,
                        Orientation.Vertical
                    ),
                captureBackPresses = true,
                navigator = navigator,
                client = webViewHolder.client,
                chromeClient = webViewHolder.chromeClient,
                onDispose = {
                    webViewHolder.deactivate()
                },
                factory = {
                    webViewHolder.activate(it)
                },
            )
        }
    }

}
