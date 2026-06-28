package com.abdownloadmanager.android.pages.browser

import com.xeton.util.HttpUrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.ContentDelegate
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoSession.ProgressDelegate
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.WebRequestError

/**
 * Wraps a [GeckoSession] and exposes observable navigation/progress state as [StateFlow]s so
 * Compose composables can collect them reactively.
 *
 * Lifecycle:
 * - Created by [WebViewRegistry] when a new tab is opened.
 * - [open] is called when the session is first attached to a [org.mozilla.geckoview.GeckoView].
 * - [close] is called when the owning [WebViewHolder] is released (tab closed / registry disposed).
 *
 * @param downloadInterceptor Receives binary download URLs that GeckoView would otherwise try
 *   (and fail) to render, forwarding them to the download dialog.
 * @param tab The [ABDMBrowserTab] this state is associated with (needed for the interceptor).
 * @param onNewTabRequested Callback invoked when a page calls `window.open()`.
 * @param initialUrl Optional URL to load immediately after the session is opened.
 */
class GeckoTabState(
    private val downloadInterceptor: DownloadInterceptor,
    val tab: ABDMBrowserTab,
    private val onNewTabRequested: (url: String?, openedBy: ABDMBrowserTabId) -> Unit,
    initialUrl: String? = null,
) {
    // ── public session ───────────────────────────────────────────────────────

    val session: GeckoSession = GeckoSession(
        GeckoSessionSettings.Builder()
            .usePrivateMode(false)
            .useTrackingProtection(true) // Gecko's built-in tracker blocking
            .allowJavascript(true)
            .build()
    )

    // ── observable state ─────────────────────────────────────────────────────

    private val _urlFlow = MutableStateFlow<String?>(initialUrl)
    val urlFlow: StateFlow<String?> = _urlFlow.asStateFlow()

    private val _titleFlow = MutableStateFlow<String?>(null)
    val titleFlow: StateFlow<String?> = _titleFlow.asStateFlow()

    /** 0.0 … 1.0 while loading; null when idle / fully loaded. */
    private val _progressFlow = MutableStateFlow<Float?>(null)
    val progressFlow: StateFlow<Float?> = _progressFlow.asStateFlow()

    private val _canGoBackFlow = MutableStateFlow(false)
    val canGoBackFlow: StateFlow<Boolean> = _canGoBackFlow.asStateFlow()

    private val _canGoForwardFlow = MutableStateFlow(false)
    val canGoForwardFlow: StateFlow<Boolean> = _canGoForwardFlow.asStateFlow()

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow.asStateFlow()

    // ── initialisation ───────────────────────────────────────────────────────

    init {
        session.navigationDelegate = navigationDelegate
        session.progressDelegate = progressDelegate
        session.contentDelegate = contentDelegate
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Opens the Gecko session and optionally loads [initialUrl].
     * Must be called on the main thread.
     */
    fun open(runtime: org.mozilla.geckoview.GeckoRuntime) {
        if (!session.isOpen) {
            session.open(runtime)
        }
        val url = initialUrl
        if (!url.isNullOrBlank() && url != ABDMBrowserTab.blankPage) {
            session.loadUri(url)
        }
    }

    /**
     * Closes the underlying [GeckoSession], releasing all associated resources.
     * After this call the object must not be used again.
     */
    fun close() {
        session.close()
    }

    // ── navigation helpers (called from WebViewHolder.navigator forwarding) ───

    fun loadUrl(url: String) = session.loadUri(url)
    fun goBack() = session.goBack()
    fun goForward() = session.goForward()
    fun reload() = session.reload()
    fun stopLoading() = session.stop()

    // ── private delegates ─────────────────────────────────────────────────────

    private val navigationDelegate = object : NavigationDelegate {

        override fun onLocationChange(
            session: GeckoSession,
            url: String?,
            perms: MutableList<NavigationDelegate.PermissionRequest>,
            hasUserGesture: Boolean,
        ) {
            _urlFlow.value = url
        }

        override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
            _canGoBackFlow.value = canGoBack
        }

        override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
            _canGoForwardFlow.value = canGoForward
        }

        override fun onLoadRequest(
            session: GeckoSession,
            request: NavigationDelegate.LoadRequest,
        ): GeckoResult<NavigationDelegate.AllowOrDeny>? {
            val url = request.uri
            // Let the engine handle normal web pages.
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return GeckoResult.allow()
            }
            // For any other scheme (tel:, mailto:, intent:, etc.) deny Gecko and let the
            // system handle it via an implicit intent — but that's outside this layer.
            return GeckoResult.deny()
        }

        override fun onNewSession(
            session: GeckoSession,
            uri: String,
        ): GeckoResult<GeckoSession>? {
            // window.open() or target="_blank" link — create a new tab.
            onNewTabRequested(uri, tab.tabId)
            // Return null to tell GeckoView we will handle the new session ourselves.
            return null
        }

        override fun onLoadError(
            session: GeckoSession,
            uri: String?,
            error: WebRequestError,
        ): GeckoResult<String>? {
            // Let Gecko display its default error page.
            return null
        }
    }

    private val progressDelegate = object : ProgressDelegate {

        override fun onPageStart(session: GeckoSession, url: String) {
            _isLoadingFlow.value = true
            _progressFlow.value = 0.0f
            _titleFlow.value = null
        }

        override fun onPageStop(session: GeckoSession, success: Boolean) {
            _isLoadingFlow.value = false
            _progressFlow.value = null
        }

        override fun onProgressChange(session: GeckoSession, progress: Int) {
            _progressFlow.value = progress / 100.0f
        }

        override fun onSecurityChange(
            session: GeckoSession,
            securityInfo: ProgressDelegate.SecurityInformation,
        ) {
            // Security info could be used to display a lock icon. Currently unused.
        }
    }

    private val contentDelegate = object : ContentDelegate {

        override fun onTitleChange(session: GeckoSession, title: String?) {
            _titleFlow.value = title
        }

        override fun onContextMenu(
            session: GeckoSession,
            screenX: Int,
            screenY: Int,
            element: ContentDelegate.ContextElement,
        ) {
            // Surface long-press on links to the download interceptor.
            val linkUrl = element.linkUri ?: element.srcUri ?: return
            if (HttpUrlUtils.isValidUrl(linkUrl)) {
                downloadInterceptor.onDownloadStart(
                    url = linkUrl,
                    userAgent = null,
                    page = _urlFlow.value,
                    tab = tab,
                )
            }
        }

        override fun onExternalResponse(
            session: GeckoSession,
            response: org.mozilla.geckoview.WebResponse,
        ) {
            // GeckoView cannot render this MIME type — forward it as a download.
            val url = response.uri
            if (HttpUrlUtils.isValidUrl(url)) {
                downloadInterceptor.onDownloadStart(
                    url = url,
                    userAgent = null,
                    page = _urlFlow.value,
                    tab = tab,
                )
            }
        }
    }
}
