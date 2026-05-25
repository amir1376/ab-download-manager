package ir.amirab.util.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import ir.amirab.util.ifThen
import okio.Path

object AppLogger {
    private lateinit var config: StaticConfig
    private var logger: Logger = Logger

    fun init(
        writeToConsole: Boolean,
        logFilePath: Path?,
        minSeverity: Severity = Severity.Verbose,
    ) {
        config = StaticConfig(
            minSeverity = minSeverity,
            logWriterList = buildList {
                if (writeToConsole) {
                    add(platformLogWriter())
                }
                logFilePath?.let {
                    add(
                        FileLogWriter(
                            config = FileLogWriterConfig(
                                logFileName = "AppLog.log",
                                logDirectory = logFilePath,
                            ),
                        )
                    )
                }
            }
        )
        logger = Logger(config)
    }

    fun get() = logger
}

val appLogger get() = AppLogger.get()

inline fun <reified T> T.thisLogger(): Logger {
    val name = T::class.qualifiedName
    return appLogger.ifThen(name != null) {
        withTag(name)
    }
}
