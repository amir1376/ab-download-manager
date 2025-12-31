package com.abdownloadmanager.android.pages.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import com.abdownloadmanager.android.ui.widget.AccompanistWebChromeClient
import com.abdownloadmanager.android.ui.widget.AccompanistWebViewClient
import com.abdownloadmanager.android.ui.widget.WebContent
import com.abdownloadmanager.android.ui.widget.WebViewNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class WebViewRegistry(
    private val scope: CoroutineScope,
    private val browserComponent: BrowserComponent,
) : WebViewFactory {
    val viewHolders = mutableMapOf<ABDMBrowserTabId, WebViewHolder>()
    fun onTabsUpdated(
        webViewStates: ABDMTabs,
    ) {
        val webViewStateIds = webViewStates.tabs.map { it.tabId }.toSet()
        for (viewHolderKey in viewHolders.keys.toList()) {
            if (viewHolderKey !in webViewStateIds) {
                removeViewHolder(viewHolderKey)
            }
        }
    }

    fun getWebViewHolder(
        tab: ABDMBrowserTab
    ): WebViewHolder {
        return viewHolders.getOrPut(tab.tabId, {
            WebViewHolder(
                tab = tab,
                navigator = WebViewNavigator(scope),
                webView = null,
                client = ABDMWebViewClient(browserComponent.downloadInterceptor, scope),
                chromeClient = ABDMChromeClient(browserComponent, ::getWebViewHolder),
                webViewFactory = this,
            )
        })
    }

    fun removeViewHolder(id: String) {
        viewHolders.remove(id)?.release()
    }

    fun disposeAll() {
        viewHolders.forEach { (_, holder) ->
            holder.release()
        }
        viewHolders.clear()
    }

    override fun createWebView(
        context: Context,
        tab: ABDMBrowserTab,
    ): ABDMWebView {
        return ABDMWebView(context).apply {
            val webView = this
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.setSupportZoom(true)
            webView.settings.builtInZoomControls = false
            webView.settings.setSupportMultipleWindows(true)
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            webView.isLongClickable = true
            webView.setOnLongClickListener {
                val hit = webView.hitTestResult

                if (hit.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                    hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                ) {
                    val url = hit.extra ?: return@setOnLongClickListener false

                    browserComponent.onLinkSelected(
                        url,
                        tab,
                    )
                    true
                } else {
                    false
                }
            }
            webView.setDownloadListener { url, userAgent, _, _, _ ->
                scope.launch(Dispatchers.Main) {
                    if (!webView.canGoBack() && webView.originalUrl == null) {
                        browserComponent.closeTab(tab.tabId)
                    }
                    browserComponent.downloadInterceptor.onDownloadStart(
                        url,
                        userAgent,
                        webView.originalUrl ?: webView.openedBy,
                        tab,
                    )
                }
            }
            webView.tabId = tab.tabId
        }
    }

}

data class WebViewHolder(
    val tab: ABDMBrowserTab,
    var webView: ABDMWebView? = null,
    val navigator: WebViewNavigator,
    val client: ABDMWebViewClient,
    val chromeClient: ABDMChromeClient,
    private val webViewFactory: WebViewFactory,
) {

    fun activate(context: Context): ABDMWebView {
        return if (webView != null) {
            (webView!!).also {
                it.onResume()
            }
        } else {
            webViewFactory.createWebView(context, tab).also { webView = it }
        }
    }

    fun deactivate() {
        webView?.onPause()
        // prevent reloading after activated again
        tab.tabState.content = WebContent.NavigatorOnly
    }

    fun release() {
        webView?.onPause()
        webView?.destroy()
        webView = null
    }
}

interface WebViewFactory {
    fun createWebView(
        context: Context,
        tab: ABDMBrowserTab,
    ): ABDMWebView
}

class ABDMWebViewClient(
    private val requestInterceptor: DownloadInterceptor,
    private val scope: CoroutineScope,
) : AccompanistWebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        if (request != null) {
            scope.launch(Dispatchers.Main) {
                requestInterceptor.interceptRequest(
                    ABDMWebRequest(
                        url = request.url.toString(),
                        headers = request.requestHeaders,
                        page = view?.originalUrl ?: view?.url
                    )
                )
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {

        val url = request.url.toString()

        // Let WebView load normal web pages
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }

        // Handle intent:// URIs
        if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val pm = view.context.packageManager

                if (intent.resolveActivity(pm) != null) {
                    view.context.startActivity(intent)
                } else {
                    intent.getStringExtra("browser_fallback_url")?.let {
                        view.loadUrl(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }

        // Handle ALL other schemes (deep links)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val pm = view.context.packageManager

            if (intent.resolveActivity(pm) != null) {
                view.context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }
}

class ABDMChromeClient(
    private val browserComponent: BrowserComponent,
    private val createWebViewHolder: (tab: ABDMBrowserTab) -> WebViewHolder,
) : AccompanistWebChromeClient() {
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (view == null) return false
        val transport = (resultMsg?.obj as? WebView.WebViewTransport) ?: return false
        val newTab = browserComponent.newTab(
            id = UUID.randomUUID().toString(),
            switch = true,
            url = null,
            openedBy = (view as? ABDMWebView)?.tabId
        )
        val newWebView = createWebViewHolder(newTab).activate(view.context)
        newWebView.openedBy = view.originalUrl ?: view.url
        transport.webView = newWebView
        resultMsg.sendToTarget()
        return true
    }
}

class ABDMWebView(
    context: Context,
) : WebView(context) {
    var openedBy: String? = null
    var tabId: String? = null
}
