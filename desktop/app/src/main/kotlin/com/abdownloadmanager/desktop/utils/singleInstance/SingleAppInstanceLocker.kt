package com.abdownloadmanager.desktop.utils.singleInstance

import okio.Path
import java.io.RandomAccessFile
import kotlin.concurrent.thread
import kotlin.io.path.createParentDirectories

class SingleAppInstanceLocker(
    private val lockPath: Path
) {
    private fun getLockPath(): Path {
        return lockPath
    }

    /**
     * @throws AnotherInstanceIsRunning
     */
    fun tryLockInstance() {
        val file = getLockPath()
            .also {
                it.toNioPath().createParentDirectories()
            }
            .toFile()

        val raf = RandomAccessFile(file, "rw")
        val lock = kotlin
            .runCatching { raf.channel.tryLock() }
            .getOrElse { null }
        if (lock != null) {
            //we get a lock
            Runtime
                .getRuntime()
                .addShutdownHook(thread(start = false) {
                    lock.release()
                    raf.close()
                    file.delete()
                })
            return
        } else {
            throw AnotherInstanceIsRunning()
        }

    }
}

class AnotherInstanceIsRunning(
) : RuntimeException("Another Instance Is Running")
