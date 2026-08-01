package ir.amirab.downloader.destination

import ir.amirab.downloader.downloaditem.http.HttpBundlePart
import ir.amirab.downloader.part.DownloadPart
import ir.amirab.downloader.part.PartDownloadStatus
import ir.amirab.util.tryAtomicMove
import okhttp3.internal.closeQuietly
import okio.FileHandle
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

class HttpBundleDownloadDestination(
    outputFile: File,
    private val tempDirectory: File,
    private val downloadId: Long,
    private val appendExtensionToIncompleteDownloads: Boolean,
    private val getAllParts: () -> List<HttpBundlePart>,
    private val assembler: ZipBundleAssembler = ZipBundleAssembler(),
) : DownloadDestination(outputFile) {
    val incompleteFile: File by lazy {
        IncompleteFileUtil.addIncompleteIndicator(outputFile, downloadId)
    }
    private val archiveFile: File
        get() = if (appendExtensionToIncompleteDownloads) incompleteFile else outputFile

    private val handles = mutableMapOf<Long, FileHandle>()

    fun getFileOfPart(part: HttpBundlePart): File = tempDirectory.resolve(part.index.toString())

    fun syncPartsWithFiles(parts: List<HttpBundlePart>) {
        tempDirectory.mkdirs()
        parts.forEach { part ->
            val file = getFileOfPart(part)
            if (part.length == 0L && !file.exists()) {
                file.createNewFile()
            }
            val validLength = file.length().takeIf { file.exists() && it <= part.length } ?: 0L
            if (file.exists() && file.length() > part.length) {
                file.delete()
            }
            part.current = validLength
            part.statusFlow.value = if (part.isCompleted) {
                PartDownloadStatus.Completed
            } else {
                PartDownloadStatus.IDLE
            }
        }
    }

    override fun getWriterFor(part: DownloadPart): DestWriter {
        require(part is HttpBundlePart)
        tempDirectory.mkdirs()
        val file = getFileOfPart(part)
        val handle = FileSystem.SYSTEM.openReadWrite(file.toOkioPath(), mustCreate = false, mustExist = false)
        handle.resize(part.current)
        synchronized(this) {
            handles.remove(part.index)?.closeQuietly()
            handles[part.index] = handle
        }
        return DestWriter(
            id = part.index,
            file = file,
            seekPos = part.current,
            writer = handle,
        ).also {
            synchronized(this) { fileParts.add(it) }
        }
    }

    override fun onPartCancelled(part: DownloadPart) {
        synchronized(this) {
            handles.remove(part.getID())?.closeQuietly()
        }
        super.onPartCancelled(part)
    }

    override fun canGetFileWriter(): Boolean = true

    override suspend fun prepareFile(onProgressUpdate: (Int?) -> Unit) {
        DownloadDestination.prepareDestinationFolder(outputFile)
        tempDirectory.mkdirs()
    }

    override suspend fun isDownloadedPartsIsValid(): Boolean = tempDirectory.exists()

    override fun flush() {
        synchronized(this) {
            handles.values.forEach { runCatching { it.flush() } }
        }
    }

    override fun onAllPartsCompleted(onProgressUpdate: (Int?) -> Unit) {
        synchronized(this) {
            if (allPartsDownloaded) {
                return
            }
            closeHandles()
            assembler.assemble(
                parts = getAllParts(),
                sourceFile = ::getFileOfPart,
                destination = archiveFile,
                onProgress = { onProgressUpdate(it) },
            )
            if (appendExtensionToIncompleteDownloads) {
                outputFile.delete()
                incompleteFile.tryAtomicMove(outputFile)
            }
            super.onAllPartsCompleted(onProgressUpdate)
        }
    }

    override fun moveOutput(to: File) {
        closeHandles()
        if (appendExtensionToIncompleteDownloads && incompleteFile.exists()) {
            incompleteFile.tryAtomicMove(IncompleteFileUtil.addIncompleteIndicator(to, downloadId))
        }
        super.moveOutput(to)
    }

    override fun deleteOutputFile() {
        closeHandles()
        incompleteFile.delete()
        super.deleteOutputFile()
    }

    override fun cleanUpJunkFiles() {
        closeHandles()
        runCatching { FileSystem.SYSTEM.deleteRecursively(tempDirectory.toOkioPath()) }
        incompleteFile.takeIf { it != outputFile }?.delete()
    }

    private fun closeHandles() {
        synchronized(this) {
            handles.values.forEach { it.closeQuietly() }
            handles.clear()
            fileParts.clear()
        }
    }
}
