package ir.amirab.downloader.db

import ir.amirab.util.tryAtomicMove
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

class TransactionalFileSaver(
    val json: Json,
) {
    fun getBakFile(file: File) = File("$file.tmp")
    inline fun <reified T> writeObject(file: File, t: T) {
        val text = json.encodeToString(t)
        writeText(file, text)
    }

    fun <T> writeObject(file: File, t: T, kSerializer: KSerializer<T>) {
        val text = json.encodeToString(kSerializer, t)
        writeText(file, text)
    }

    fun writeText(file: File, text: String) {
        val bakFile = getBakFile(file)
        kotlin.runCatching {
            FileSystem.SYSTEM.write(
                file = bakFile.toOkioPath()
            ) {
                writeUtf8(text)
            }
        }.onSuccess {
            bakFile.tryAtomicMove(file)
        }.getOrThrow()
    }

    fun readText(file: File): String? {
        return runCatching {
            FileSystem.SYSTEM.read(file.toOkioPath()) {
                readUtf8()
            }
        }.getOrNull()
    }

    inline fun <reified T> readObject(file: File): T? {
        return kotlin.runCatching {
            val text = readText(file)!!
            json.decodeFromString<T>(text)
        }.getOrNull()
    }

    fun <T> readObject(file: File, serializer: KSerializer<T>): T? {
        return kotlin.runCatching {
            val text = readText(file)!!
            json.decodeFromString(serializer, text)
        }.getOrNull()
    }
}
