package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import ir.amirab.downloader.connection.proxy.ProxyStrategy
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.SystemProxySelectorProvider
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.Dns
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.Closeable
import java.net.InetAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.UnknownHostException
import java.util.Collections
import java.util.concurrent.TimeUnit

/** Exercise the HTTP/1-only client injected by the desktop app, over real TLS and ALPN. */
class FuckingFastProtocolTest {
    @Test
    fun landingPageAndResolvedDownloadNegotiateHttp2WithDesktopClientSettings() = runBlocking {
        Fixture().use { fixture ->
            fixture.server.enqueue(MockResponse.Builder()
                .code(204)
                .addHeader("HX-Redirect", fixture.url(ARCHIVE_PATH, "dl.fuckingfast.co"))
                .build())
            fixture.enqueueArchive()

            val credentials = HttpDownloadCredentials(
                fixture.url("/abc123DEF456"),
                headers = mapOf("X-Fixture" to "preserved", "Cookie" to "session=fixture"),
                username = "fixture-user",
                password = "fixture-password",
                userAgent = "fixture-agent",
            )
            fixture.client.connect(credentials, 0L, 14L).use { connection ->
                assertEquals("fixture archive", connection.source.buffer().readUtf8())
            }

            val button = fixture.request()
            val archive = fixture.request()
            assertEquals("POST", button.method)
            assertEquals("/f/abc123DEF456/go", button.url.encodedPath)
            assertNull(button.headers["Range"])
            assertEquals("true", button.headers["HX-Request"])
            assertEquals(ARCHIVE_PATH, archive.url.encodedPath)
            assertEquals("bytes=0-14", archive.headers["Range"])
            for (request in listOf(button, archive)) {
                assertEquals("HTTP/2", request.version)
                assertEquals("preserved", request.headers["X-Fixture"])
                assertEquals("session=fixture", request.headers["Cookie"])
                assertEquals("fixture-agent", request.headers["User-Agent"])
                assertEquals(Credentials.basic("fixture-user", "fixture-password"), request.headers["Authorization"])
            }
            assertEquals(listOf(Protocol.HTTP_2, Protocol.HTTP_2), fixture.negotiatedProtocols)
            assertEquals(listOf(Protocol.HTTP_1_1), fixture.injectedClient.protocols)
        }
    }

    @Test
    fun directlyImportedDownloadNegotiatesHttp2WithoutLandingPage() = runBlocking {
        Fixture().use { fixture ->
            fixture.enqueueArchive()
            fixture.client.connect(HttpDownloadCredentials(fixture.url(ARCHIVE_PATH)), 0L, 14L).use {
                assertEquals("fixture archive", it.source.buffer().readUtf8())
            }
            assertEquals(ARCHIVE_PATH, fixture.request().url.encodedPath)
            assertEquals(1, fixture.server.requestCount)
            assertEquals(listOf(Protocol.HTTP_2), fixture.negotiatedProtocols)
        }
    }

    @Test
    fun ordinaryHostsKeepInjectedHttp1Protocol() = runBlocking {
        Fixture().use { fixture ->
            fixture.enqueueArchive()
            fixture.client.connect(HttpDownloadCredentials(fixture.url(ARCHIVE_PATH, "example.test")), 0L, 14L).use {
                assertEquals("fixture archive", it.source.buffer().readUtf8())
            }
            assertEquals("HTTP/1.1", fixture.request().version)
            assertEquals(listOf(Protocol.HTTP_1_1), fixture.negotiatedProtocols)
        }
    }

    private class Fixture : Closeable {
        val negotiatedProtocols: MutableList<Protocol> = Collections.synchronizedList(mutableListOf())
        private val certificate = HeldCertificate.Builder()
            .commonName("download fixture")
            .addSubjectAlternativeName("fuckingfast.co")
            .addSubjectAlternativeName("dl.fuckingfast.co")
            .addSubjectAlternativeName("example.test")
            .build()
        private val serverCertificates = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        private val clientCertificates = HandshakeCertificates.Builder().addTrustedCertificate(certificate.certificate).build()
        private val loopback = InetAddress.getByName("127.0.0.1")
        val server = MockWebServer().apply {
            useHttps(serverCertificates.sslSocketFactory())
            start(loopback, 0)
        }
        val injectedClient = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    if (hostname !in setOf("fuckingfast.co", "dl.fuckingfast.co", "example.test")) {
                        throw UnknownHostException(hostname)
                    }
                    return listOf(loopback)
                }
            })
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .proxy(Proxy.NO_PROXY)
            .callTimeout(5, TimeUnit.SECONDS)
            .eventListener(object : EventListener() {
                override fun connectionAcquired(call: Call, connection: okhttp3.Connection) {
                    negotiatedProtocols.add(connection.protocol())
                }
            })
            .build()
        val client = OkHttpHttpDownloaderClient(
            injectedClient,
            object : UserAgentProvider { override fun getUserAgent(): String? = null },
            object : ProxyStrategyProvider { override fun getProxyStrategyFor(url: String) = ProxyStrategy.Direct },
            object : SystemProxySelectorProvider { override fun getSystemProxySelector(): ProxySelector? = null },
            AutoConfigurableProxyProvider.NoOp(),
        )

        fun url(path: String, host: String = "fuckingfast.co") = "https://$host:${server.port}$path"

        fun enqueueArchive() = server.enqueue(MockResponse.Builder()
            .code(206)
            .addHeader("Content-Type", "application/octet-stream")
            .addHeader("Content-Range", "bytes 0-14/1024")
            .body("fixture archive")
            .build())

        fun request(): RecordedRequest = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))

        override fun close() {
            injectedClient.connectionPool.evictAll()
            injectedClient.dispatcher.executorService.shutdownNow()
            server.close()
        }
    }

    private companion object {
        const val ARCHIVE_PATH = "/dl/token/archive.part01.rar"
    }
}
