package ir.amirab.downloader.destination

import ir.amirab.downloader.anntation.HeavyCall
import okio.Buffer
import okio.FileHandle
import okio.FileSystem
import okio.Sink
import java.io.File

/**
 * this class provide an interface for independent write buffer by Part Manager Crew
 * */
class DestWriter(
    val id: Long,
    val file: File,
    var seekPos: Long,
    val writer: FileHandle,
) {

    private var status: Status = Status.NotPrepared


    @Transient
    private var sink: Sink? = null

    @HeavyCall
    @Synchronized
    fun prepare() {
        if (status != Status.NotPrepared) {
            error("already prepared : status=$status")
        }
        if (!file.exists()) {
            error("file not exists, can't prepare file")
        }

        status = Status.Preparing
        sink = writer.sink(seekPos)
        status = Status.Prepared
//        println("part #$id started to write from $seekPos")
    }

    @Synchronized
    fun release() {
        sink?.close()
        status = Status.NotPrepared
//        println("part #$id stopped to write to $seekPos")
    }

    fun write(buffer: Buffer, length: Long = buffer.size) {
        val currentStatus = status
        if (currentStatus == Status.NotPrepared) {
            throw Exception("first prepare")
        }
        if (currentStatus == Status.Finished) {
            throw Exception("finished still writing?")
        }
        if (currentStatus == Status.Prepared) {
            status = Status.Writing
        }
        sink!!.write(buffer, length)
        seekPos += length
//    println("seek :$seekPos")
    }

    enum class Status { NotPrepared, Preparing, Prepared, Writing, Finished }

    fun use(block: (DestWriter) -> Unit) {
//        println("using dest")
        prepare()
        try {
            block(this)
        } catch (e: Exception) {
            throw e
        } finally {
            try {
//                println("release dest")
                release()
            } catch (_: Exception) {
            }
        }
    }

}
