package ir.amirab.downloader.db

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class TransactionalFileSaver(
    val json: Json,
) {
    fun getBakFile(file: File) = File("$file.tmp")
    inline fun <reified T> writeObject(file: File, t: T) {
        val bakFile = getBakFile(file)
        val text = json.encodeToString(t)
        kotlin.runCatching {
            bakFile.writeText(text)
        }.onSuccess {
            file.delete()
            bakFile.renameTo(file)
        }.getOrThrow()
    }

    inline fun <reified T> readObject(file: File): T? {
        return kotlin.runCatching {
            val text = file.readText()
            json.decodeFromString<T>(text)
        }.getOrNull()
    }
}