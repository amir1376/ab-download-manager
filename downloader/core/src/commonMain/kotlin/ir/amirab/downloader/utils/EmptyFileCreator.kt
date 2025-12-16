package ir.amirab.downloader.utils

import ir.amirab.downloader.exception.NoSpaceInStorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class EmptyFileCreator(
    private val diskStat: IDiskStat,
    private val useSparseFile: () -> Boolean
) {
    private fun canWeUseSparse(file: File): Boolean {
        return useSparseFile() && SparseFile.canWeCreateSparseFile(file)
    }

    /**
     * @param length must be -1 , or positive
     */
    suspend fun prepareFile(
        file: File,
        length: Long,
        onProgressUpdate: (percent: Int?) -> Unit,
    ) {
        require(length >= -1) {
            "length must be -1 , or positive value but we got ${length}"
        }
        withContext(Dispatchers.IO) {

            val canWeUseSparse = canWeUseSparse(file)
            onProgressUpdate(0)
            if (length == -1L) {
                RandomAccessFile(file, "rw").use {
                    it.setLength(0)
                }
                onProgressUpdate(100)
                return@withContext
            }
            val remainingSpace = diskStat.getRemainingSpace(file.parentFile)
            if (file.exists()) {
                val currentLength = file.length()
                val requiredLength = length - currentLength
                if (remainingSpace < requiredLength) {
                    throw NoSpaceInStorageException(remainingSpace, requiredLength)
                }

                when {
                    currentLength > length -> {
                        RandomAccessFile(file, "rw").use {
                            it.setLength(length)
                        }
                        onProgressUpdate(100)
                        return@withContext
                    }

                    currentLength < length -> {
                        if (canWeUseSparse) {
                            if (!file.delete()) {
                                throw IOException("can't delete file")
                            }
                            if (SparseFile.createSparseFile(file)) {
                                onProgressUpdate(null)
                                writeAtLast(file, length)
                            } else {
                                file.createNewFile()
                                fillOutput(file, length, onProgressUpdate)
                            }
                        } else {
                            fillOutput(file, length, onProgressUpdate)
                        }
                        onProgressUpdate(100)
                        return@withContext
                    }

                    else -> {
                        onProgressUpdate(100)
                        return@withContext
                    }
                }
            } else {
                if (remainingSpace < length) {
                    throw NoSpaceInStorageException(remainingSpace, length)
                }
                if (canWeUseSparse && SparseFile.createSparseFile(file)) {
                    onProgressUpdate(null)
                    writeAtLast(file, length)
                } else {
                    file.createNewFile()
                    fillOutput(file, length, onProgressUpdate)
                }
                onProgressUpdate(100)
            }
        }
    }

    /**
     * manually write a single byte to the last of file!
     * if the sparse is not supported for the file, at least
     * waits for OS to create empty file for us
     */
    private fun writeAtLast(file: File, length: Long) {
        RandomAccessFile(file, "rw").use {
            it.seek(length - 1)
            it.write(0)
        }
    }

    private suspend fun fillOutput(outputFile: File, length: Long, onProgressUpdate: (percent: Int) -> Unit) {
        val much = length - outputFile.length()
        val remainingSpace = diskStat.getRemainingSpace(outputFile.parentFile)
        if (remainingSpace < much) {
            throw NoSpaceInStorageException(remainingSpace, much)
        }
//        println("how much to be appended $much")
        withContext(Dispatchers.IO) {
            FileOutputStream(outputFile, true).use {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var written = 0L
                while (isActive) {
                    val writeInThisLoop = if (much - written > buffer.size) {
                        buffer.size
                    } else {
                        much - written
                    }.toInt()
                    if (writeInThisLoop == 0) break
//                println(writeInThisLoop)
                    it.write(buffer, 0, writeInThisLoop)
                    written += writeInThisLoop
                    onProgressUpdate(calcPercent(written, much))
                }
            }
        }
    }
}
