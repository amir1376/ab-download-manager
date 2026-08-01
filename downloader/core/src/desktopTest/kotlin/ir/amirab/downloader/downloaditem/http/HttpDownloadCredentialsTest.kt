package ir.amirab.downloader.downloaditem.http

import kotlin.test.Test
import kotlin.test.assertEquals

class HttpDownloadCredentialsTest {
    @Test
    fun preservesBundleWhenCreatedFromDownloadItem() {
        val bundle = HttpDownloadBundle(
            sources = listOf(
                HttpDownloadBundleSource(
                    link = "https://example.test/archive-001.zip",
                    suggestedName = "archive-001.zip",
                    contentLength = 42,
                ),
            ),
            suggestedName = "archive.zip",
        )
        val item = HttpDownloadItem.createWithCredentials(
            credentials = HttpDownloadCredentials(
                link = bundle.sources.first().link,
                bundle = bundle,
            ),
            id = 1,
            folder = ".",
            name = bundle.suggestedName,
        )

        assertEquals(bundle, HttpDownloadCredentials.from(item).bundle)
    }
}
