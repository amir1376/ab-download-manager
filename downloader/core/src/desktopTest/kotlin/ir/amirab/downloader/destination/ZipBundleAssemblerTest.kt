package ir.amirab.downloader.destination

import ir.amirab.downloader.downloaditem.http.HttpBundlePart
import ir.amirab.downloader.downloaditem.http.HttpDownloadBundleSource
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZipBundleAssemblerTest {
    @Test
    fun mergesZipEntriesAndPlainFilesIntoOneArchive() {
        val directory = createTempDirectory("abdm-zip-bundle-test").toFile()
        try {
            val firstZip = directory.resolve("first.zip")
            val secondZip = directory.resolve("second.zip")
            val video = directory.resolve("video.mp4").apply {
                writeBytes(byteArrayOf(1, 2, 3, 4, 5))
            }
            createZip(firstZip, "folder/first.txt", "first".encodeToByteArray())
            createZip(secondZip, "folder/second.txt", "second".encodeToByteArray())

            val inputs = listOf(firstZip, secondZip, video)
            val parts = inputs.mapIndexed { index, file ->
                HttpBundlePart(
                    index = index.toLong(),
                    source = HttpDownloadBundleSource(
                        link = "https://example.test/$index",
                        suggestedName = file.name,
                        contentLength = file.length(),
                    ),
                    current = file.length(),
                )
            }
            val output = directory.resolve("merged.zip")
            var progress = 0

            ZipBundleAssembler().assemble(
                parts = parts,
                sourceFile = { inputs[it.index.toInt()] },
                destination = output,
                onProgress = { progress = it },
            )

            assertEquals(100, progress)
            assertTrue(output.isFile)
            ZipFile(output).use { zip ->
                assertContentEquals(
                    "first".encodeToByteArray(),
                    zip.getInputStream(zip.getEntry("folder/first.txt")).readBytes(),
                )
                assertContentEquals(
                    "second".encodeToByteArray(),
                    zip.getInputStream(zip.getEntry("folder/second.txt")).readBytes(),
                )
                assertContentEquals(
                    video.readBytes(),
                    zip.getInputStream(zip.getEntry("video.mp4")).readBytes(),
                )
            }
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun createZip(file: File, name: String, contents: ByteArray) {
        ZipOutputStream(file.outputStream()).use { output ->
            output.putNextEntry(ZipEntry(name))
            output.write(contents)
            output.closeEntry()
        }
    }
}
