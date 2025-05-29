package ir.amirab.downloader.destination

import ir.amirab.downloader.anntation.HeavyCall
import ir.amirab.downloader.part.Part
import ir.amirab.downloader.utils.EmptyFileCreator
import okio.FileHandle
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

class SimpleDownloadDestination(
    file: File,
    val appendExtensionToIncompleteDownloads: Boolean,
    val downloadId: Long,
    private val emptyFileCreator: EmptyFileCreator,
) : DownloadDestination(
    outputFile = file,
) {
    // this is only used when appendExtensionToIncompleteDownloads is true
    val incompleteFile by lazy {
        IncompleteFileUtil.addIncompleteIndicator(outputFile, downloadId)
    }

    private val fileToWrite: File = if (appendExtensionToIncompleteDownloads) {
        incompleteFile
    } else {
        outputFile
    }

    private var _fileHandle: FileHandle? = null
    private val fileHandle: FileHandle
        get() {
            synchronized(this) {
                if (_fileHandle == null) {
                    return initFileHandle()
                }
            }
            return _fileHandle!!
        }

    private fun initFileHandle(): FileHandle {
        // lets open a file for writing to it
        // it will be removed when all parts are cancelled so this method
        // maybe called multiple times
        val handle = FileSystem.SYSTEM.openReadWrite(fileToWrite.toOkioPath())
        _fileHandle = handle
        return handle
    }

    private fun removeFileHandle() {
        //close and release handle to unlock the file
        synchronized(this) {
            _fileHandle?.close()
            _fileHandle = null
        }
    }

    override fun onAllFilePartsRemoved() {
        super.onAllFilePartsRemoved()
//        println("release handle")
        removeFileHandle()
    }

    override fun onAllPartsCompleted() {
        if (appendExtensionToIncompleteDownloads) {
            // this function maybe called at some point that we may not even start download yet.
            // for example when the download has already completed, the DownloadJob will call this function! so we should do nothing.
            val incompleteFile = incompleteFile
            if (!incompleteFile.exists()) {
                return
            }
            val completeFile = outputFile
            // delete old file if exists to override with new one!
            if (completeFile.exists()) {
                completeFile.delete()
            }
            incompleteFile.renameTo(completeFile)
        }
        // clean up junk files called in the super class
        super.onAllPartsCompleted()
    }

    var outputSize: Long = -1
    override fun getWriterFor(
        part: Part,
    ): DestWriter {
        if (!canGetFileWriter()) {
            throw IllegalStateException("First check then ask for...")
        }
        val outFile = fileToWrite
        val returned = returnIfAlreadyHaveWriter(part.from)
        returned?.let { return it }
        val writer = DestWriter(
            part.from,
            outFile,
            part.from,
            part.current,
            fileHandle,
        )
        synchronized(this) {
            fileParts.add(writer)
        }
        return writer
    }

    override fun flush() {
        runCatching {
            _fileHandle?.flush()
        }
    }

    @HeavyCall
    override suspend fun prepareFile(onProgressUpdate: (Int?) -> Unit) {
//        println("preparing file ")
//        println("file info path=$outputFile size=${outputFile.runCatching { length() }.getOrNull()}")
        val incompleteFile = fileToWrite
        incompleteFile.parentFile.let {
            it.canonicalFile.mkdirs()
            if (!it.exists()) {
                error("can't create folder for destination file $it")
            }

            if (!it.isDirectory) {
                error("${incompleteFile.parentFile} is not a directory")
            }
        }
        emptyFileCreator
            .prepareFile(incompleteFile, outputSize, onProgressUpdate)
    }

    /**
     * restart download if file was deleted by user!
     * this function will be called when the download is resumed, and it's not completed yet.
     */
    override suspend fun isDownloadedPartsIsValid(): Boolean {
        val targetFile = fileToWrite
        val fileExists = targetFile.exists()
        val fileEqualToContentSize = targetFile.length() == outputSize
        return fileExists && fileEqualToContentSize
    }

    override fun canGetFileWriter(): Boolean {
        return true
    }

    override fun updateLastModified() {
        runCatching {
            requestedToChangeLastModified?.let {
                fileToWrite.setLastModified(it)
            }
        }
    }

    override fun moveOutput(to: File) {
        if (appendExtensionToIncompleteDownloads) {
            val incompleteFile = incompleteFile
            if (incompleteFile.exists()) {
                incompleteFile.renameTo(IncompleteFileUtil.addIncompleteIndicator(to, downloadId))
            }
        }
        super.moveOutput(to)
    }

    override fun cleanUpJunkFiles() {
        // remove incomplete file if exists
        val incompleteFile = incompleteFile
        if (incompleteFile.exists()) {
            incompleteFile.delete()
        }
    }
}
