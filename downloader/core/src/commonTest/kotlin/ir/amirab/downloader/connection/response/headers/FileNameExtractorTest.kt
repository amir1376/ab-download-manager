package ir.amirab.downloader.connection.response.headers

import kotlin.test.Test
import kotlin.test.assertEquals

class FileNameExtractorTest {
    @Test
    fun `trims whitespace before delimiter in quoted filename`() {
        assertEquals(
            "sample.torrent",
            extractFileNameFromContentDisposition(
                "attachment; filename=\"sample.torrent\" \t; charset=utf-8",
            ),
        )
    }

    @Test
    fun `trims whitespace before delimiter in unquoted filename`() {
        assertEquals(
            "sample.torrent",
            extractFileNameFromContentDisposition(
                "attachment; filename=sample.torrent ; charset=utf-8",
            ),
        )
    }

    @Test
    fun `trims whitespace before delimiter in extended filename`() {
        assertEquals(
            "sample file.torrent",
            extractFileNameFromContentDisposition(
                "attachment; filename*=UTF-8''sample%20file.torrent ; charset=utf-8",
            ),
        )
    }

    @Test
    fun `keeps filename extraction without whitespace before delimiter`() {
        assertEquals(
            "sample.torrent",
            extractFileNameFromContentDisposition(
                "attachment; filename=\"sample.torrent\"; charset=utf-8",
            ),
        )
        assertEquals(
            "sample file.torrent",
            extractFileNameFromContentDisposition(
                "attachment; filename*=UTF-8''sample%20file.torrent; charset=utf-8",
            ),
        )
    }
}
