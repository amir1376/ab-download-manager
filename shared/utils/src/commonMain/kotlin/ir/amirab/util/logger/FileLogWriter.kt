@file:OptIn(ExperimentalTime::class)

package ir.amirab.util.logger

import co.touchlab.kermit.*
import ir.amirab.util.createParentDirectories
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import okhttp3.internal.closeQuietly
import okio.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal open class FileLogWriter(
    private val config: FileLogWriterConfig,
    private val clock: Clock = Clock.System,
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : LogWriter() {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val coroutineScope = CoroutineScope(
        newSingleThreadContext("FileLogWriter") +
                SupervisorJob() +
                CoroutineName("FileLogWriter") +
                CoroutineExceptionHandler { _, throwable ->
                    // can't log it, we're the logger -- print to standard error
                    println("FileLogWriter: Uncaught exception in writer coroutine")
                    throwable.printStackTrace()
                },
    )

    private val loggingChannel: Channel<Buffer> = Channel()

    init {
        coroutineScope.launch {
            writer()
        }
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        bufferLog(
            formatMessage(
                severity = severity,
                tag = Tag(tag),
                message = Message(message),
            ),
            throwable,
        )
    }

    private fun bufferLog(message: String, throwable: Throwable?) {
        val log = buildString {
            if (config.prependTimestamp) {
                append(clock.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET))
                append(" ")
            }
            appendLine(message)
            if (throwable != null) {
                appendLine(throwable.stackTraceToString())
            }
        }
        loggingChannel.trySendBlocking(Buffer().apply { writeUtf8(log) })
    }

    private fun formatMessage(severity: Severity, tag: Tag?, message: Message): String =
        messageStringFormatter.formatMessage(severity, if (config.logTag) tag else null, message)

    val logFilePath = config.logDirectory / config.logFileName

    fun createNewLogSink(): BufferedSink {
        logFilePath.createParentDirectories()
        return fileSystem
            .sink(logFilePath, mustCreate = false)
            .buffer()
    }

    private suspend fun writer() {
        createNewLogSink().use {
            while (currentCoroutineContext().isActive) {
                val buffer = loggingChannel.receiveCatching().getOrNull() ?: break
                it.write(buffer, buffer.size)
                it.flush()
            }
        }
    }
}

data class FileLogWriterConfig(
    val logFileName: String,
    val logDirectory: Path,
    val logTag: Boolean = true,
    val prependTimestamp: Boolean = true,
)
