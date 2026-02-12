package ir.amirab.downloader.destination

import ir.amirab.downloader.part.DownloadPart
import ir.amirab.downloader.utils.calcPercent
import okhttp3.internal.closeQuietly
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

class SegmentedDownloadDestination(
    // a directory unique for this item!
    val tempDirectory: File,
    val getFileName: (DownloadPart) -> String,
    val getAllParts: () -> List<DownloadPart>,
    val appendMode: Boolean,
    outputFile: File,
) : DownloadDestination(outputFile) {
    private fun getFileOfPart(downloadPart: DownloadPart): File {
        val id = downloadPart.getID()
        return tempDirectory.resolve("$id")
    }

    override fun getWriterFor(part: DownloadPart): DestWriter {
        tempDirectory.mkdirs()
        val tFile = getFileOfPart(part)
        val writer = FileSystem.SYSTEM.openReadWrite(tFile.toOkioPath(), mustCreate = false, mustExist = false)
        val newSize = if (appendMode) {
            writer.size()
        } else {
            // part starts from the beginning
            0
        }
        writer.resize(newSize)
        val destWriter = DestWriter(
            id = part.getID(),
            file = tFile,
            seekPos = newSize,
            writer = writer,
        )
        synchronized(this) {
            fileParts.add(destWriter)
        }
        return destWriter
    }

    override fun onPartCancelled(part: DownloadPart) {
        super.onPartCancelled(part)
        val id = part.getID()
        synchronized(this) {
            fileParts
                .find { it.id == id }
                ?.writer
                ?.closeQuietly()
        }
    }


    override fun canGetFileWriter(): Boolean {
        return true
    }

    override suspend fun prepareFile(onProgressUpdate: (Int?) -> Unit) {

    }

    override suspend fun isDownloadedPartsIsValid(): Boolean {
        return tempDirectory.exists()
    }

    fun isDownloadPartValid(part: DownloadPart): Boolean {
        return getFileOfPart(part).exists()
    }

    override fun cleanUpJunkFiles() {
        runCatching {
            FileSystem.SYSTEM.deleteRecursively(tempDirectory.toOkioPath())
        }
    }

    override fun onAllPartsCompleted(onProgressUpdate: (Int?) -> Unit) {
        assemble(
            getAllParts()
                .sortedBy { it.getID() }
                .map { tempDirectory.resolve(getFileName(it)) },
            outputFile,
            onProgressUpdate
        )
        super.onAllPartsCompleted(onProgressUpdate)
    }

    fun assemble(
        sources: List<File>,
        destination: File,
        onProgress: (Int) -> Unit
    ) {
        DownloadDestination.prepareDestinationFolder(outputFile)
        val totalLength = sources.sumOf { it.length() }
        var totalWritten = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var percent = 0
        destination.outputStream().use { dst ->
            sources.forEach { sourceFile ->
                sourceFile.inputStream().use { src ->
                    onProgress(percent)
                    while (true) {
                        val len = src.read(buffer)
                        if (len == -1) break
                        dst.write(buffer, 0, len)
                        totalWritten += len

                        val newPercent = calcPercent(totalWritten, totalLength)
                        if (newPercent != percent) {
                            onProgress(newPercent)
                            percent = newPercent
                        }
                    }
                }
            }
        }
        onProgress(100)
    }

    override fun flush() {
        synchronized(this) {
            fileParts.forEach {
                it.writer.flush()
            }
        }
    }
}
