package ir.amirab.downloader.destination

import ir.amirab.downloader.anntation.HeavyCall
import ir.amirab.downloader.exception.NoSpaceInStorageException
import ir.amirab.downloader.part.Part
import ir.amirab.downloader.utils.IDiskStat
import ir.amirab.downloader.utils.calcPercent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okio.FileHandle
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class SimpleDownloadDestination(
    file: File,
    private val diskStat: IDiskStat,
) : DownloadDestination(file) {

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
        val handle = FileSystem.SYSTEM.openReadWrite(outputFile.toOkioPath())
        _fileHandle = handle
        return handle
    }
    private fun removeFileHandle() {
        //close and release handle to unlock the file
        synchronized(this){
            _fileHandle?.close()
            _fileHandle=null
        }
    }

    override fun onAllFilePartsRemoved() {
//        println("release handle")
        removeFileHandle()
    }

    var outputSize: Long = -1
    override fun getWriterFor(
        part: Part,
    ): DestWriter {
        if (!canGetFileWriter()) {
            throw IllegalStateException("First check then ask for...")
        }
        val outFile = outputFile
        val returned = returnIfAlreadyHaveWriter(part.from)
        returned?.let { return it }
        val writer = DestWriter(
            part.from,
            outFile,
            part.from,
            part.current,
            fileHandle,
        )
        synchronized(this){
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
    override suspend fun prepareFile(onProgressUpdate: (Int) -> Unit) {
//        println("preparing file ")
//        println("file info path=$outputFile size=${outputFile.runCatching { length() }.getOrNull()}")
        outputFile.parentFile.let {
            it.canonicalFile.mkdirs()
            if (!it.exists()) {
                error("can't create folder for destination file $it")
            }

            if (!it.isDirectory) {
                error("${outputFile.parentFile} is not a directory")
            }
        }
        onProgressUpdate(0)
        if (!outputFile.exists()) {
//            println("file not exist creating...")
            outputFile.createNewFile()
        }
//        println("file size before modification ${outputFile.length()}")
//        println("currentFileSize: " + outputFile.length())
        if (outputSize==-1L){
            withContext(Dispatchers.IO){
                RandomAccessFile(outputFile, "rw").use {
                    it.channel.use {
                        //clear
                        it.truncate(0)
                    }
                }
            }
        }else if (outputFile.length() > outputSize) {
//            println("current file size bigger that output size truncating fSize:${outputFile.length()} oSize:${outputSize}")
            withContext(Dispatchers.IO) {
                RandomAccessFile(outputFile, "rw").use {
                    it.channel.use {
                        it.truncate(outputSize)
                    }
                }
            }
            onProgressUpdate(100)
        } else if (outputFile.length() < outputSize) {
//            println("current file size smaller than output size filling ${outputFile.length()} to reach ${outputSize}")
//            println("filling output")
            fillOutput(onProgressUpdate)
        }
//        println("length of prepared file ${outputFile.length()}")
    }

    /**
     * restart download if file was deleted by user!
     */
    override suspend fun isDownloadedPartsIsValid(): Boolean {
        val fileExists = outputFile.exists()
        val fileEqualToContentSize = outputFile.length() == outputSize
        return fileExists && fileEqualToContentSize
    }

    private suspend fun fillOutput(onProgressUpdate: (Int) -> Unit) {
        val much = outputSize - outputFile.length()
        val remainingSpace = diskStat.getRemainingSpace(outputFile.parentFile)
        if (remainingSpace < much) {
            throw NoSpaceInStorageException(remainingSpace, much)
        }
//        println("how much to be appended $much")
        withContext(Dispatchers.IO) {
            FileOutputStream(outputFile, true).use {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var writen = 0L
                while (isActive) {
                    val writeInThisLoop = if (much - writen > buffer.size) {
                        buffer.size
                    } else {
                        much - writen
                    }.toInt()
                    if (writeInThisLoop == 0) break
//                println(writeInThisLoop)
                    it.write(buffer, 0, writeInThisLoop)
                    writen += writeInThisLoop
                    onProgressUpdate(calcPercent(writen, much))
                }
            }
        }
    }


    override fun canGetFileWriter(): Boolean {
        return true
    }
}