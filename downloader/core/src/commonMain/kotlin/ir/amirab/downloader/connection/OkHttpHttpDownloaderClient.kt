package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.proxy.*
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.IHttpBasedDownloadCredentials
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.exception.DownloadLinkResolutionException
import ir.amirab.downloader.exception.UnSuccessfulResponseException
import ir.amirab.downloader.utils.await
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector

class OkHttpHttpDownloaderClient(
    private val okHttpClient: OkHttpClient,
    private val customUserAgentProvider: UserAgentProvider,
    private val proxyStrategyProvider: ProxyStrategyProvider,
    private val systemProxySelectorProvider: SystemProxySelectorProvider,
    private val autoConfigurableProxyProvider: AutoConfigurableProxyProvider,
) : HttpDownloaderClient() {
    // The app's shared client deliberately uses HTTP/1.1. This host rejects its
    // button endpoint over HTTP/1.1, so allow ALPN negotiation for this host only.
    // Deriving the client keeps the app's DNS, TLS, timeouts and interceptors.
    private val fuckingFastClient by lazy {
        okHttpClient.newBuilder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .build()
    }

    private data class ResolutionKey(val url: HttpUrl, val headers: Headers)
    private data class ResolvedDownload(val url: HttpUrl, val createdAt: Long = System.nanoTime())

    private val resolutionMutex = Mutex()
    private val resolvedDownloads = linkedMapOf<ResolutionKey, ResolvedDownload>()
    private val pendingResolutions = mutableMapOf<ResolutionKey, CompletableDeferred<ResolvedDownload>>()

    private fun newCall(
        downloadCredentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
        requestUrl: HttpUrl? = null,
        followRedirects: Boolean = true,
        extraBuilder: Request.Builder.() -> Unit,
    ): Call {
        val rangeHeader = start?.let {
            createRangeHeader(start, end)
        }
        val target = requestUrl ?: downloadCredentials.link.toHttpUrlOrNull()
        val client = if (target != null && FuckingFastPage.isDownloadHost(target)) fuckingFastClient else okHttpClient
        return client
            .applyProxy(downloadCredentials)
            .let { client ->
                if (followRedirects) client else client.newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build()
            }
            .newCall(
                Request.Builder()
                    .url(requestUrl?.toString() ?: downloadCredentials.link)
                    .apply {
                        defaultHeadersInFirst().forEach { (k, v) ->
                            header(k, v)
                        }
                        // we don't to add something that we sure that it will be overridden later
                        if (downloadCredentials.userAgent == null) {
                            // only add default user agent if we don't specify it
                            val customUserAgent = customUserAgentProvider.getUserAgent()
                                ?: getDefaultUserAgent()
                            header("User-Agent", customUserAgent)
                        }
                        downloadCredentials.headers
                            ?.filter {
                                //OkHttp handles this header and if we override it,
                                //makes redirected links to have this "Host" instead of their own!, and cause error
                                !it.key.equals("Host", true)
                            }
                            ?.forEach { (k, v) ->
                                header(k, v)
                            }
                        defaultHeadersInLast().forEach { (k, v) ->
                            header(k, v)
                        }
                        val username = downloadCredentials.username
                        val password = downloadCredentials.password
                        if (username?.isNotBlank() == true && password?.isNotBlank() == true) {
                            header("Authorization", Credentials.basic(username, password))
                        }
                        downloadCredentials.userAgent?.let { userAgent ->
                            header("User-Agent", userAgent)
                        }
                    }
                    .apply(extraBuilder)
                    .apply {
                        if (rangeHeader != null) {
                            header(rangeHeader.first, rangeHeader.second)
                        }
                    }
                    .build()
            )
    }

    private suspend fun resolvedCall(
        credentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Response {
        val pageUrl = credentials.link.toHttpUrlOrNull()
            ?.takeIf(FuckingFastPage::isLandingPage)
            ?.newBuilder()?.fragment(null)?.build()
            ?: return newCall(credentials, start, end, extraBuilder = { get() }).await()
        val pageCall = newCall(credentials, null, null, requestUrl = pageUrl) {
            get()
            removeHeader("Range")
        }
        val key = ResolutionKey(pageUrl, pageCall.request().headers)
        repeat(2) { attempt ->
            val resolved = resolveDownload(key, credentials)
            val response = newCall(credentials, start, end, requestUrl = resolved.url) {
                get()
                if (build().header("Referer") == null) header("Referer", pageUrl.toString())
            }.await()
            if (attempt == 0 && response.code in setOf(403, 404)) {
                response.close()
                resolutionMutex.withLock {
                    if (resolvedDownloads[key] == resolved) resolvedDownloads.remove(key)
                }
            } else {
                if (response.isSuccessful && response.body?.contentType()?.subtype
                        ?.contains("html", ignoreCase = true) == true
                ) {
                    response.close()
                    throw DownloadLinkResolutionException(
                        "FuckingFast returned a web page instead of a download. The file may require browser verification."
                    )
                }
                return response
            }
        }
        error("Unreachable")
    }

    private suspend fun resolveDownload(
        key: ResolutionKey,
        credentials: IHttpBasedDownloadCredentials,
    ): ResolvedDownload {
        var ownsResolution = false
        val pending = resolutionMutex.withLock {
            resolvedDownloads[key]?.takeIf {
                System.nanoTime() - it.createdAt < RESOLUTION_LIFETIME_NANOS
            }?.let { return it }
            pendingResolutions.getOrPut(key) {
                ownsResolution = true
                CompletableDeferred()
            }
        }
        if (!ownsResolution) {
            try {
                return pending.await()
            } catch (error: CancellationException) {
                // A canceled metadata probe must not cancel other downloads sharing its lookup.
                currentCoroutineContext().ensureActive()
                resolutionMutex.withLock {
                    if (pendingResolutions[key] === pending) pendingResolutions.remove(key)
                }
                return resolveDownload(key, credentials)
            }
        }
        try {
            val resolved = ResolvedDownload(resolvePage(credentials, key.url))
            resolutionMutex.withLock {
                resolvedDownloads[key] = resolved
                while (resolvedDownloads.size > MAX_CACHED_RESOLUTIONS) {
                    resolvedDownloads.remove(resolvedDownloads.keys.first())
                }
            }
            pending.complete(resolved)
            return resolved
        } catch (error: Throwable) {
            pending.completeExceptionally(error)
            throw error
        } finally {
            withContext(NonCancellable) {
                resolutionMutex.withLock {
                    if (pendingResolutions[key] === pending) pendingResolutions.remove(key)
                }
            }
        }
    }

    private suspend fun resolvePage(credentials: IHttpBasedDownloadCredentials, pageUrl: HttpUrl): HttpUrl {
        // Current pages return the button's target in HX-Redirect, not in the HTML.
        newCall(
            credentials, null, null,
            requestUrl = FuckingFastPage.downloadButtonUrl(pageUrl),
            followRedirects = false,
        ) {
            post(FormBody.Builder().build())
            removeHeader("Range")
            header("HX-Request", "true")
            header("HX-Current-URL", pageUrl.toString())
            header("Referer", pageUrl.toString())
            header("Origin", pageUrl.resolve("/").toString().removeSuffix("/"))
        }.await().use { response ->
            checkBrowserVerification(response)
            val target = response.header("HX-Redirect") ?: response.header("Location")
            if (target != null && response.code in 200..399) {
                return FuckingFastPage.parseDownloadUrl(target, pageUrl)
                    ?: throw DownloadLinkResolutionException("FuckingFast returned an invalid download target.")
            }
            if (response.code !in setOf(404, 405)) {
                if (!response.isSuccessful) throw UnSuccessfulResponseException(response.code, response.message)
                FuckingFastPage.extractDownloadUrl(readPage(response), pageUrl)?.let { return it }
            }
        }
        // Compatibility with older pages whose button opens a literal /dl/ URL.
        newCall(credentials, null, null, requestUrl = pageUrl, followRedirects = false) {
            get()
            removeHeader("Range")
        }.await().use { response ->
            checkBrowserVerification(response)
            response.header("Location")?.takeIf { response.code in 300..399 }?.let { target ->
                return FuckingFastPage.parseDownloadUrl(target, pageUrl)
                    ?: throw DownloadLinkResolutionException("FuckingFast returned an invalid download target.")
            }
            if (!response.isSuccessful) throw UnSuccessfulResponseException(response.code, response.message)
            return FuckingFastPage.extractDownloadUrl(readPage(response), pageUrl)
                ?: throw DownloadLinkResolutionException("No download button was found on the FuckingFast page.")
        }
    }

    private fun checkBrowserVerification(response: Response) {
        if (response.header("cf-mitigated").equals("challenge", ignoreCase = true) || response.code == 403) {
            throw DownloadLinkResolutionException(
                "FuckingFast requires browser verification. Open the page with the browser extension and import its direct download link."
            )
        }
    }

    private fun readPage(response: Response): String {
        val body = requireNotNull(response.body) { "FuckingFast response has no body" }
        if (body.contentLength() > MAX_PAGE_BYTES) {
            throw DownloadLinkResolutionException("FuckingFast page exceeds the link resolution size limit.")
        }
        val source = body.source()
        source.request(MAX_PAGE_BYTES + 1)
        if (source.buffer.size > MAX_PAGE_BYTES) {
            throw DownloadLinkResolutionException("FuckingFast page exceeds the link resolution size limit.")
        }
        return source.readString(body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8)
    }

    private fun OkHttpClient.applyProxy(
        downloadCredentials: IHttpBasedDownloadCredentials,
    ): OkHttpClient {
        return when (
            val strategy = proxyStrategyProvider.getProxyStrategyFor(downloadCredentials.link)
        ) {
            ProxyStrategy.Direct -> return this
            ProxyStrategy.UseSystem -> {
                newBuilder()
                    .proxySelector(
                        systemProxySelectorProvider.getSystemProxySelector()
                            ?: ProxySelector.getDefault()
                    )
                    .build()
            }

            is ProxyStrategy.ByScript -> {
                val proxySelector = autoConfigurableProxyProvider.getAutoConfigurableProxy(strategy.scriptPath)
                if (proxySelector != null) {
                    newBuilder()
                        .proxySelector(proxySelector)
                        .build()
                } else {
                    this
                }
            }

            is ProxyStrategy.ManualProxy -> {
                val proxy = strategy.proxy
                return newBuilder()
                    .proxy(
                        Proxy(
                            when (proxy.type) {
                                ProxyType.HTTP -> Proxy.Type.HTTP
                                ProxyType.SOCKS -> Proxy.Type.SOCKS
                            },
                            InetSocketAddress(proxy.host, proxy.port)
                        )
                    ).let {
                        if (proxy.username != null && proxy.type == ProxyType.HTTP) {
                            it.proxyAuthenticator { _, r ->
                                val credentials = Credentials.basic(
                                    proxy.username,
                                    proxy.password.orEmpty()
                                )
                                r.request
                                    .newBuilder()
                                    .header("Proxy-Authorization", credentials)
                                    .build()
                            }
                        } else {
                            it
                        }
                    }.build()
            }
        }
    }


    override suspend fun actualHead(
        credentials: IHttpDownloadCredentials,
        start: Long?,
        end: Long?,
    ): HttpResponseInfo {
        resolvedCall(credentials, start, end).use { response ->
            return createFileInfo(response)
        }
    }

    private fun createFileInfo(response: Response): HttpResponseInfo {
        return HttpResponseInfo(
            statusCode = response.code,
            message = response.message,
            requestUrl = response.request.url.toString(),
            requestHeaders = response.request.headers.associate { (key, value) ->
                key.lowercase() to value
            },
            responseHeaders = response.headers.associate { (key, value) ->
                key.lowercase() to value
            },
        )
    }

    override suspend fun actualConnect(
        credentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
    ): Connection<HttpResponseInfo> {
        val response = resolvedCall(credentials, start, end)
        val body = runCatching {
            requireNotNull(response.body) {
                "body is null"
            }
        }.onFailure {
            response.close()
        }.getOrThrow()
        return Connection(
            source = body.source(),
            contentLength = body.contentLength(),
            responseInfo = createFileInfo(response)
        )
    }

    private companion object {
        const val MAX_PAGE_BYTES = 256L * 1024
        const val MAX_CACHED_RESOLUTIONS = 128
        const val RESOLUTION_LIFETIME_NANOS = 2L * 60 * 1_000_000_000
    }
}
