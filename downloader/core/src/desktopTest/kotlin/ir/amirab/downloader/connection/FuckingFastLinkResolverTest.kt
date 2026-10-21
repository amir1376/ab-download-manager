package ir.amirab.downloader.connection

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import ir.amirab.downloader.connection.proxy.ProxyStrategy
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.SystemProxySelectorProvider
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.exception.DownloadLinkResolutionException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.Closeable
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.UnknownHostException
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class FuckingFastLinkResolverTest {
    @Test
    fun identifiesLandingPagesWithoutInterceptingOtherHostsOrDirectLinks() {
        assertTrue(FuckingFastPage.isLandingPage("https://fuckingfast.co/abc123DEF456".toHttpUrl()))
        assertTrue(FuckingFastPage.isLandingPage("https://fuckingfast.co/f/abc123DEF456".toHttpUrl()))
        assertFalse(FuckingFastPage.isLandingPage("https://fuckingfast.co/dl/token/archive.rar".toHttpUrl()))
        assertFalse(FuckingFastPage.isLandingPage("https://example.test/abc123DEF456".toHttpUrl()))
        assertFalse(FuckingFastPage.isLandingPage("https://fuckingfast.co.example.test/abc123DEF456".toHttpUrl()))
    }

    @Test
    fun extractsStaticDownloadLinksAndDecodesEscapedSeparators() {
        val page = "https://fuckingfast.co/abc123DEF456".toHttpUrl()
        val attribute = """<a href="/dl/token/archive.part01.rar?download=1&amp;name=archive">Download</a>"""
        assertEquals(
            "https://fuckingfast.co/dl/token/archive.part01.rar?download=1&name=archive",
            FuckingFastPage.extractDownloadUrl(attribute, page).toString(),
        )
        val script = """window.open("https:\/\/dl.fuckingfast.co\/dl\/token\/archive.part02.rar")"""
        assertEquals(
            "https://dl.fuckingfast.co/dl/token/archive.part02.rar",
            FuckingFastPage.extractDownloadUrl(script, page).toString(),
        )
        assertEquals(
            "https://www.fuckingfast.co/dl/token/archive.part01.rar?download=1&name=archive",
            FuckingFastPage.extractDownloadUrl(attribute, "https://www.fuckingfast.co/abc123DEF456".toHttpUrl()).toString(),
        )
    }

    @Test
    fun rejectsMissingExternalAndExecutableDownloadTargets() {
        val page = "https://fuckingfast.co/abc123DEF456".toHttpUrl()
        listOf(
            "<html>No download available</html>",
            """window.open("https://example.test/dl/token/archive.rar")""",
            """window.open("https://fuckingfast.co.example.test/dl/token/archive.rar")""",
            """<a href="javascript:/dl/token/archive.rar">Download</a>""",
        ).forEach { html ->
            assertNull(html, FuckingFastPage.extractDownloadUrl(html, page))
        }
    }

    @Test
    fun resolvesMetadataAndRangesToArchiveInsteadOfLandingHtml() = runBlocking {
        Fixture().use { fixture ->
            val client = fixture.client()
            val credentials = HttpDownloadCredentials(fixture.url("/abc123DEF456"))
            val info = client.test(credentials)
            assertFalse(info.isWebPage)
            assertTrue(info.resumeSupport)
            assertEquals(1024L, info.totalLength)
            assertEquals("archive.part01.rar", info.fileName)
            assertEquals(fixture.url(Fixture.ARCHIVE_PATH), info.requestUrl)
            client.connect(credentials, 512L, 767L).use { connection ->
                assertEquals(256L, connection.contentLength)
                assertFalse(connection.responseInfo.isWebPage)
                assertArrayEquals(fixture.archive.copyOfRange(512, 768), connection.source.buffer().readByteArray())
            }
            val resolutionRequest = fixture.requestsTo(Fixture.GO_PATH).single()
            assertEquals("POST", resolutionRequest.method)
            assertNull(resolutionRequest.header("Range"))
            assertEquals("true", resolutionRequest.header("HX-Request"))
            assertEquals(credentials.link, resolutionRequest.header("HX-Current-URL"))
            assertEquals(listOf("bytes=0-255", "bytes=512-767"), fixture.requestsTo(Fixture.ARCHIVE_PATH).map { it.header("Range") })
        }
    }

    @Test
    fun preservesCredentialsAndHeadersForPageAndArchiveRequests() = runBlocking {
        Fixture().use { fixture ->
            val credentials = HttpDownloadCredentials(
                link = fixture.url("/abc123DEF456"),
                headers = mapOf("Cookie" to "session=fixture", "Referer" to "https://example.test/source", "X-Fixture" to "preserved"),
                username = "fixture",
                password = "secret",
                userAgent = "FixtureAgent/1.0",
            )
            val client = fixture.client()
            client.test(credentials)
            client.connect(credentials, 256L, 511L).use { it.source.buffer().readByteArray() }
            assertEquals(3, fixture.requests.size)
            fixture.requests.forEach { request ->
                assertEquals("session=fixture", request.header("Cookie"))
                assertEquals("preserved", request.header("X-Fixture"))
                assertEquals("FixtureAgent/1.0", request.header("User-Agent"))
                assertEquals("Basic Zml4dHVyZTpzZWNyZXQ=", request.header("Authorization"))
            }
            fixture.requestsTo(Fixture.ARCHIVE_PATH).forEach { request ->
                assertEquals("https://example.test/source", request.header("Referer"))
            }
            assertEquals(credentials.link, fixture.requestsTo(Fixture.GO_PATH).single().header("Referer"))
        }
    }

    @Test
    fun leavesOrdinaryUrlsAndAlreadyDirectDownloadsUnchanged() = runBlocking {
        Fixture().use { fixture ->
            val client = fixture.client()
            val ordinary = fixture.url("/ordinary", "example.test")
            client.connect(HttpDownloadCredentials(ordinary), null, null).use { connection ->
                assertTrue(connection.responseInfo.isWebPage)
                assertEquals(ordinary, connection.responseInfo.requestUrl)
                assertTrue(connection.source.buffer().readUtf8().contains("fixture ordinary page"))
            }
            val direct = fixture.url(Fixture.ARCHIVE_PATH)
            client.connect(HttpDownloadCredentials(direct), 0L, 15L).use { connection ->
                assertEquals(direct, connection.responseInfo.requestUrl)
                assertArrayEquals(fixture.archive.copyOfRange(0, 16), connection.source.buffer().readByteArray())
            }
            assertEquals(listOf("/ordinary", Fixture.ARCHIVE_PATH), fixture.requests.map { it.path })
        }
    }

    @Test
    fun sharesResolutionAcrossConcurrentRangeRequests() = runBlocking {
        Fixture().use { fixture ->
            val client = fixture.client()
            val credentials = HttpDownloadCredentials(fixture.url("/abc123DEF456"))
            coroutineScope {
                (0 until 8).map { index ->
                    async {
                        client.connect(credentials, index * 16L, index * 16L + 15L).use { connection ->
                            assertArrayEquals(fixture.archive.copyOfRange(index * 16, index * 16 + 16), connection.source.buffer().readByteArray())
                        }
                    }
                }.awaitAll()
            }
            assertEquals(1, fixture.requestsTo(Fixture.GO_PATH).size)
            assertEquals(8, fixture.requestsTo(Fixture.ARCHIVE_PATH).size)
        }
    }

    @Test
    fun failsClearlyWhenLandingPageHasNoDownloadLink() = runBlocking {
        Fixture().use { fixture ->
            val failure = runCatching {
                fixture.client().connect(HttpDownloadCredentials(fixture.url("/missing12345")), null, null).close()
            }.exceptionOrNull()
            assertTrue("Expected link resolution failure, got $failure", failure is DownloadLinkResolutionException)
            assertEquals(listOf("/f/missing12345/go", "/missing12345"), fixture.requests.map { it.path })
        }
    }

    @Test
    fun fallsBackToLegacyStaticPageWhenGoEndpointIsUnavailable() = runBlocking {
        Fixture().use { fixture ->
            fixture.client().connect(HttpDownloadCredentials(fixture.url("/legacy123456")), 0L, 15L).use { connection ->
                assertFalse(connection.responseInfo.isWebPage)
                assertArrayEquals(fixture.archive.copyOfRange(0, 16), connection.source.buffer().readByteArray())
            }
            assertEquals(listOf("/f/legacy123456/go", "/legacy123456", Fixture.ARCHIVE_PATH), fixture.requests.map { it.path })
            assertNull(fixture.requestsTo("/legacy123456").single().header("Range"))
        }
    }

    @Test
    fun readsLocationWithoutFollowingAnUnrangedArchiveRedirect() = runBlocking {
        Fixture().use { fixture ->
            fixture.client().connect(HttpDownloadCredentials(fixture.url("/redir1234567")), 100L, 115L).use { connection ->
                assertArrayEquals(fixture.archive.copyOfRange(100, 116), connection.source.buffer().readByteArray())
            }
            assertEquals(listOf("/f/redir1234567/go", Fixture.ARCHIVE_PATH), fixture.requests.map { it.path })
            assertEquals("bytes=100-115", fixture.requestsTo(Fixture.ARCHIVE_PATH).single().header("Range"))
        }
    }

    @Test
    fun readsLegacyPageRedirectWithoutFollowingItBeforeApplyingTheRange() = runBlocking {
        Fixture().use { fixture ->
            fixture.client().connect(HttpDownloadCredentials(fixture.url("/getredir1234")), 200L, 215L).use { connection ->
                assertArrayEquals(fixture.archive.copyOfRange(200, 216), connection.source.buffer().readByteArray())
            }
            assertEquals(listOf("/f/getredir1234/go", "/getredir1234", Fixture.ARCHIVE_PATH), fixture.requests.map { it.path })
            assertNull(fixture.requestsTo("/getredir1234").single().header("Range"))
            assertEquals("bytes=200-215", fixture.requestsTo(Fixture.ARCHIVE_PATH).single().header("Range"))
        }
    }

    @Test
    fun refreshesCachedResolutionAfterArchiveAuthorizationExpires() = runBlocking {
        Fixture().use { fixture ->
            val client = fixture.client()
            val credentials = HttpDownloadCredentials(fixture.url("/abc123DEF456"))
            client.test(credentials)
            fixture.rejectNextArchive.set(true)
            client.connect(credentials, 0L, 15L).use { connection ->
                assertArrayEquals(fixture.archive.copyOfRange(0, 16), connection.source.buffer().readByteArray())
            }
            assertEquals(2, fixture.requestsTo(Fixture.GO_PATH).size)
            assertEquals(3, fixture.requestsTo(Fixture.ARCHIVE_PATH).size)
        }
    }

    @Test
    fun reportsChallengeWithoutFallingBackOrReturningChallengeHtml() = runBlocking {
        Fixture().use { fixture ->
            val failure = runCatching {
                fixture.client().connect(HttpDownloadCredentials(fixture.url("/blocked12345")), null, null).close()
            }.exceptionOrNull()
            assertTrue("Expected challenge resolution failure, got $failure", failure is DownloadLinkResolutionException)
            assertEquals(listOf("/f/blocked12345/go"), fixture.requests.map { it.path })
        }
    }

    @Test
    fun rejectsHtmlReturnedByTheResolvedArchiveEndpoint() = runBlocking {
        Fixture().use { fixture ->
            val failure = runCatching {
                fixture.client().connect(HttpDownloadCredentials(fixture.url("/htmlfile1234")), null, null).close()
            }.exceptionOrNull()
            assertTrue("Expected HTML rejection, got $failure", failure is DownloadLinkResolutionException)
            assertEquals(listOf("/f/htmlfile1234/go", "/dl/html/error.rar"), fixture.requests.map { it.path })
        }
    }

    @Test
    fun boundsResolutionResponseSize() = runBlocking {
        Fixture().use { fixture ->
            val failure = runCatching {
                fixture.client().connect(HttpDownloadCredentials(fixture.url("/oversize1234")), null, null).close()
            }.exceptionOrNull()
            assertTrue("Expected resolution size limit, got $failure", failure is DownloadLinkResolutionException)
            assertEquals(listOf("/f/oversize1234/go"), fixture.requests.map { it.path })
        }
    }

    @Test
    fun keepsResolutionCacheSeparateForDifferentRequestCredentials() = runBlocking {
        Fixture().use { fixture ->
            val client = fixture.client()
            listOf("first", "second").forEach { session ->
                client.test(HttpDownloadCredentials(fixture.url("/abc123DEF456"), headers = mapOf("Cookie" to "session=$session")))
            }
            assertEquals(listOf("session=first", "session=second"), fixture.requestsTo(Fixture.GO_PATH).map { it.header("Cookie") })
        }
    }

    @Test
    fun canceledResolutionOwnerDoesNotCancelAnotherWaitingDownload() = runBlocking(Dispatchers.IO) {
        Fixture().use { fixture ->
            fixture.holdFirstResolution.set(true)
            val client = fixture.client()
            val credentials = HttpDownloadCredentials(fixture.url("/abc123DEF456"))
            val owner = async(start = CoroutineStart.UNDISPATCHED) { client.head(credentials, 0L, 15L) }
            assertTrue("Resolution request did not reach fixture", fixture.firstResolutionStarted.await(3, TimeUnit.SECONDS))
            val waiter = async(start = CoroutineStart.UNDISPATCHED) {
                client.connect(credentials, 16L, 31L).use { it.source.buffer().readByteArray() }
            }
            try {
                owner.cancelAndJoin()
                assertArrayEquals(fixture.archive.copyOfRange(16, 32), withTimeout(3000) { waiter.await() })
                assertEquals(2, fixture.requestsTo(Fixture.GO_PATH).size)
            } finally {
                fixture.releaseFirstResolution.countDown()
            }
        }
    }

    private class Fixture : Closeable {
        val archive = ByteArray(1024) { (it % 251).toByte() }
        val requests: MutableList<RecordedRequest> = Collections.synchronizedList(mutableListOf())
        val rejectNextArchive = AtomicBoolean(false)
        val holdFirstResolution = AtomicBoolean(false)
        val firstResolutionStarted = CountDownLatch(1)
        val releaseFirstResolution = CountDownLatch(1)
        private val executor = Executors.newCachedThreadPool()
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            executor = this@Fixture.executor
            createContext("/") { exchange -> handle(exchange) }
            start()
        }
        private val httpClient = OkHttpClient.Builder()
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    if (hostname != "fuckingfast.co" && hostname != "example.test") throw UnknownHostException(hostname)
                    return listOf(InetAddress.getByName("127.0.0.1"))
                }
            })
            .proxy(Proxy.NO_PROXY)
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

        fun url(path: String, host: String = "fuckingfast.co") = "http://$host:${server.address.port}$path"

        fun client() = OkHttpHttpDownloaderClient(
            okHttpClient = httpClient,
            customUserAgentProvider = object : UserAgentProvider {
                override fun getUserAgent(): String? = null
            },
            proxyStrategyProvider = object : ProxyStrategyProvider {
                override fun getProxyStrategyFor(url: String): ProxyStrategy = ProxyStrategy.Direct
            },
            systemProxySelectorProvider = object : SystemProxySelectorProvider {
                override fun getSystemProxySelector(): ProxySelector? = null
            },
            autoConfigurableProxyProvider = AutoConfigurableProxyProvider.NoOp(),
        )

        fun requestsTo(path: String) = requests.filter { it.path == path }

        private fun handle(exchange: HttpExchange) {
            exchange.use {
                val path = exchange.requestURI.path
                requests.add(RecordedRequest(path, exchange.requestMethod, exchange.requestHeaders.entries.associate { it.key.lowercase() to it.value.first() }))
                when (path) {
                    GO_PATH -> {
                        if (holdFirstResolution.compareAndSet(true, false)) {
                            firstResolutionStarted.countDown()
                            releaseFirstResolution.await(5, TimeUnit.SECONDS)
                        }
                        exchange.responseHeaders.set("HX-Redirect", url(ARCHIVE_PATH))
                        exchange.sendResponseHeaders(204, -1)
                    }
                    "/f/redir1234567/go" -> {
                        exchange.responseHeaders.set("Location", url(ARCHIVE_PATH))
                        exchange.sendResponseHeaders(302, -1)
                    }
                    "/getredir1234" -> {
                        exchange.responseHeaders.set("Location", ARCHIVE_PATH)
                        exchange.sendResponseHeaders(302, -1)
                    }
                    "/f/blocked12345/go" -> {
                        exchange.responseHeaders.set("cf-mitigated", "challenge")
                        exchange.sendResponseHeaders(403, -1)
                    }
                    "/f/htmlfile1234/go" -> {
                        exchange.responseHeaders.set("HX-Redirect", url("/dl/html/error.rar"))
                        exchange.sendResponseHeaders(204, -1)
                    }
                    "/f/oversize1234/go" -> sendHtml(exchange, "x".repeat(256 * 1024 + 1))
                    "/dl/html/error.rar" -> sendHtml(exchange, "<html>File unavailable</html>")
                    "/legacy123456" -> sendHtml(exchange, """<html><script>function download(){window.open("$ARCHIVE_PATH")}</script></html>""")
                    "/missing12345" -> sendHtml(exchange, "<html>Download unavailable</html>")
                    "/ordinary" -> sendHtml(exchange, "<html>fixture ordinary page</html>")
                    ARCHIVE_PATH -> sendArchive(exchange)
                    else -> {
                        exchange.sendResponseHeaders(404, -1)
                    }
                }
            }
        }

        private fun sendHtml(exchange: HttpExchange, html: String) {
            val bytes = html.toByteArray()
            exchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.write(bytes)
        }

        private fun sendArchive(exchange: HttpExchange) {
            if (rejectNextArchive.compareAndSet(true, false)) {
                exchange.sendResponseHeaders(403, -1)
                return
            }
            val range = exchange.requestHeaders.getFirst("Range")
                ?.removePrefix("bytes=")?.split('-')
            val start = range?.get(0)?.toIntOrNull() ?: 0
            val end = range?.get(1)?.toIntOrNull() ?: archive.lastIndex
            val bytes = archive.copyOfRange(start, end + 1)
            exchange.responseHeaders.set("Content-Type", "application/octet-stream")
            exchange.responseHeaders.set("Content-Disposition", "attachment; filename=\"archive.part01.rar\"")
            exchange.responseHeaders.set("Accept-Ranges", "bytes")
            if (range != null) exchange.responseHeaders.set("Content-Range", "bytes $start-$end/${archive.size}")
            exchange.sendResponseHeaders(if (range != null) 206 else 200, bytes.size.toLong())
            exchange.responseBody.write(bytes)
        }

        override fun close() {
            releaseFirstResolution.countDown()
            server.stop(0)
            executor.shutdownNow()
            httpClient.connectionPool.evictAll()
            httpClient.dispatcher.executorService.shutdownNow()
        }

        companion object {
            const val ARCHIVE_PATH = "/dl/token/archive.part01.rar"
            const val GO_PATH = "/f/abc123DEF456/go"
        }
    }

    private data class RecordedRequest(val path: String, val method: String, val headers: Map<String, String>) {
        fun header(name: String): String? = headers[name.lowercase()]
    }
}
