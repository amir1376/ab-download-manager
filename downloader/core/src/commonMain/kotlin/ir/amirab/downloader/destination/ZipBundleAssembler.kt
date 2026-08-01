package ir.amirab.downloader.destination

import ir.amirab.downloader.downloaditem.http.HttpBundlePart
import ir.amirab.downloader.utils.calcPercent
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.archivers.zip.Zip64Mode
import java.io.File
import java.util.zip.ZipEntry

class ZipBundleAssembler {
    fun assemble(
        parts: List<HttpBundlePart>,
        sourceFile: (HttpBundlePart) -> File,
        destination: File,
        onProgress: (Int) -> Unit,
    ) {
        destination.parentFile?.mkdirs()
        destination.delete()
        val totalLength = parts.sumOf { it.length }.coerceAtLeast(1)
        var processedLength = 0L
        var lastProgress = -1

        fun reportProgress() {
            val progress = calcPercent(processedLength, totalLength)
            if (progress != lastProgress) {
                lastProgress = progress
                onProgress(progress)
            }
        }

        ZipArchiveOutputStream(destination).use { output ->
            output.setUseZip64(Zip64Mode.AsNeeded)
            parts.sortedBy { it.index }.forEach { part ->
                val file = sourceFile(part)
                require(file.isFile && file.length() == part.length) {
                    "bundle source is missing or incomplete: ${part.source.suggestedName}"
                }
                if (part.source.suggestedName.endsWith(".zip", ignoreCase = true)) {
                    copyZipEntries(file, output)
                } else {
                    val entry = output.createArchiveEntry(file, part.source.suggestedName) as ZipArchiveEntry
                    entry.method = ZipEntry.STORED
                    output.putArchiveEntry(entry)
                    file.inputStream().buffered().use { input ->
                        input.copyTo(output)
                    }
                    output.closeArchiveEntry()
                }
                processedLength += part.length
                reportProgress()
            }
            output.finish()
        }
        onProgress(100)
    }

    private fun copyZipEntries(source: File, output: ZipArchiveOutputStream) {
        ZipFile.builder().setFile(source).get().use { input ->
            val entries = input.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val rawInput = input.getRawInputStream(entry)
                    ?: error("can't read ZIP entry ${entry.name}")
                rawInput.use {
                    output.addRawArchiveEntry(ZipArchiveEntry(entry), it)
                }
            }
        }
    }
}
