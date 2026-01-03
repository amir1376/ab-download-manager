package com.abdownloadmanager.android.pages.browser

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val tab = webViewHolder.tab
    key(tab.tabId) {
        val wState = tab.tabState
        val navigator = webViewHolder.navigator

        var isPullToRefreshInProgress by rememberSaveable {
            mutableStateOf(false)
        }
        LaunchedEffect(wState.loadingState) {
            if (wState.loadingState is LoadingState.Finished) {
                isPullToRefreshInProgress = false
            }
        }

        PullToRefreshBox(
            modifier = modifier,
            isRefreshing = isPullToRefreshInProgress,
            onRefresh = {
                if (wState.loadingState is LoadingState.Finished) {
                    isPullToRefreshInProgress = true
                    navigator.reload()
                }
            },
            state = pullToRefreshState
        ) {
            val webView = remember {
                webViewHolder.activate(context)
            }

            /**
             * Although WebView is inherently scrollable, we apply a scrollable modifier
             * and manage scrolling manually via scrollableState to support pull-to-refresh.
             *
             * Because the pullToRefresh requires a scrollable child, which is why this
             * additional scroll handling is necessary.
             */
            val scrollState = rememberScrollableState(
                consumeScrollDelta = { scroll ->
                    val inverseScroll = -scroll
                    val consumed = when {

                        inverseScroll < 0 -> {
                            inverseScroll.coerceAtLeast(-webView.scrollY.toFloat())
                        }

                        inverseScroll > 0 -> {
                            if (webView.canScrollVertically(1)) {
                                inverseScroll
                            } else 0f
                        }

                        else -> 0f
                    }
                    webView.scrollBy(0, consumed.roundToInt())
                    -consumed
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
                    webView
                },
            )
        }
    }

}
