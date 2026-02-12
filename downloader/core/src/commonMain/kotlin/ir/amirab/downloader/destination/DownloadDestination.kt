package ir.amirab.downloader.destination

import ir.amirab.downloader.part.DownloadPart
import ir.amirab.util.tryAtomicMove
import java.io.File

abstract class DownloadDestination(
    outputFile: File,
) {
    val outputFile = outputFile.canonicalFile.absoluteFile

    protected val fileParts = mutableListOf<DestWriter>()
    protected var allPartsDownloaded = false
    protected var requestedToChangeLastModified: Long? = null

    protected open fun onAllFilePartsRemoved() {
        updateLastModified()
    }

    open fun onAllPartsCompleted(
        onProgressUpdate: (Int?) -> Unit = {}
    ) {
        allPartsDownloaded = true
        cleanUpJunkFiles()
        updateLastModified()
    }

    open fun cleanUpJunkFiles() {}

    abstract fun getWriterFor(part: DownloadPart): DestWriter
    abstract fun canGetFileWriter(): Boolean

    fun returnIfAlreadyHaveWriter(partId: Long): DestWriter? {
        synchronized(this) {
            return fileParts.find {
                val condition = it.id == partId
//      if (condition) {
//        logger.info("part id$partId already have an associated file")
//      }
                condition
            }
        }
    }

    open fun deleteOutputFile() {
        outputFile.delete()
    }

    abstract suspend fun prepareFile(onProgressUpdate: (Int?) -> Unit)
    abstract suspend fun isDownloadedPartsIsValid(): Boolean
    abstract fun flush()

    open fun onPartCancelled(part: DownloadPart) {
        synchronized(this) {
            val cleanAny = fileParts.removeAll {
                it.id == part.getID()
            }
            if (cleanAny) {
                if (fileParts.isEmpty()) {
                    onAllFilePartsRemoved()
                }
            }
        }
    }

    /**
     * specify last modified time to be used when this destination finish its work
     * for example file paused / finished
     */
    fun setLastModified(timestamp: Long?) {
        requestedToChangeLastModified = timestamp
    }

    protected open fun updateLastModified() {
        kotlin.runCatching {
            requestedToChangeLastModified?.let {
                outputFile.setLastModified(it)
            }
        }
    }

    /**
     * after you use this method this class must be recreated
     */
    open fun moveOutput(to: File) {
        if (outputFile.exists()) {
            try {
                outputFile.tryAtomicMove(to)
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Failed to move output file to the new destination: ${e.localizedMessage}",
                    e,
                )
            }
        }
    }

    companion object {
        fun prepareDestinationFolder(
            outputFile: File,
        ) {
            outputFile.parentFile.let {
                it.canonicalFile.mkdirs()
                if (!it.exists()) {
                    error("can't create folder for destination file $it")
                }

                if (!it.isDirectory) {
                    error("${outputFile.parentFile} is not a directory")
                }
            }
        }
    }
}
