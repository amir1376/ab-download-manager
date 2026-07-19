package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.proxy.*
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.IHttpBasedDownloadCredentials
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.downloader.utils.await
import okhttp3.*
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.Socket
import javax.net.SocketFactory

class OkHttpHttpDownloaderClient(
    private val okHttpClient: OkHttpClient,
    private val customUserAgentProvider: UserAgentProvider,
    private val proxyStrategyProvider: ProxyStrategyProvider,
    private val systemProxySelectorProvider: SystemProxySelectorProvider,
    private val autoConfigurableProxyProvider: AutoConfigurableProxyProvider,
    private val networkInterfaceBinder: NetworkInterfaceBinder? = null,
) : HttpDownloaderClient() {
    private fun newCall(
        downloadCredentials: IHttpBasedDownloadCredentials,
        start: Long?,
        end: Long?,
        extraBuilder: Request.Builder.() -> Unit,
    ): Call {
        val rangeHeader = start?.let {
            createRangeHeader(start, end)
        }
        return okHttpClient
            .applyProxy(downloadCredentials)
            .newCall(
                Request.Builder()
                    .url(downloadCredentials.link)
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

    private fun OkHttpClient.applyProxy(
        downloadCredentials: IHttpBasedDownloadCredentials,
    ): OkHttpClient {
        val downloadId = (downloadCredentials as? ir.amirab.downloader.downloaditem.IDownloadItem)?.id
        val boundAddress = downloadId?.let { id ->
            networkInterfaceBinder?.getBoundAddress(id)
        }
        if (boundAddress != null) {
            // bind every connection of this download to the queue's network interface
            return newBuilder()
                .socketFactory(BindingSocketFactory(boundAddress))
                .build()
        }
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
        newCall(
            downloadCredentials = credentials,
            start = start,
            end = end,
            extraBuilder = {
//                head()
            }
        ).await().use { response ->
//            println(response.headers)
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
        val response = newCall(
            downloadCredentials = credentials,
            start = start,
            end = end,
            extraBuilder = {
                get()
            }
        ).await()
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
}

/**
 * A [SocketFactory] that binds created sockets to a specific local address,
 * forcing egress through the corresponding network interface. This mirrors
 * `dispatch`'s per-connection `bind_socket` but is applied statically per queue.
 */
private class BindingSocketFactory(
    private val localAddress: InetAddress,
) : SocketFactory() {
    private fun newBoundSocket(): Socket {
        return Socket().apply {
            bind(InetSocketAddress(localAddress, 0))
        }
    }

    override fun createSocket(): Socket {
        return newBoundSocket()
    }

    override fun createSocket(host: String?, port: Int): Socket {
        return newBoundSocket().apply { connect(InetSocketAddress(host, port)) }
    }

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
        return newBoundSocket().apply { connect(InetSocketAddress(host, port)) }
    }

    override fun createSocket(address: InetAddress?, port: Int): Socket {
        return newBoundSocket().apply { connect(InetSocketAddress(address, port)) }
    }

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
        return newBoundSocket().apply { connect(InetSocketAddress(address, port)) }
    }
}

