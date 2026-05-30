@file:Suppress("unused")

package ir.amirab.util.logger.slf4j.impl

import co.touchlab.kermit.BaseLogger
import co.touchlab.kermit.Severity
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.event.Level.*
import org.slf4j.helpers.AbstractLogger

class Slf4jKermitLogger(
    private val defaultTag: String,
    private val loggerProvider: () -> BaseLogger,
) : AbstractLogger() {
    override fun getName(): String = "slf4j-over-kermit"

    val logger get() = loggerProvider()

    //region Is Logging enabled at various levels
    override fun isTraceEnabled() = logger.config.minSeverity <= Severity.Verbose
    override fun isTraceEnabled(marker: Marker?) = logger.config.minSeverity <= Severity.Verbose
    override fun isDebugEnabled() = logger.config.minSeverity <= Severity.Debug
    override fun isDebugEnabled(marker: Marker?) = logger.config.minSeverity <= Severity.Debug
    override fun isInfoEnabled() = logger.config.minSeverity <= Severity.Info
    override fun isInfoEnabled(marker: Marker?) = logger.config.minSeverity <= Severity.Info
    override fun isWarnEnabled() = logger.config.minSeverity <= Severity.Warn
    override fun isWarnEnabled(marker: Marker?) = logger.config.minSeverity <= Severity.Warn
    override fun isErrorEnabled() = logger.config.minSeverity <= Severity.Error
    override fun isErrorEnabled(marker: Marker?) = logger.config.minSeverity <= Severity.Error
    //endregion

    override fun getFullyQualifiedCallerName(): String? = null

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any>?,
        throwable: Throwable?
    ) {
        val severity = when (level) {
            ERROR -> Severity.Error
            WARN -> Severity.Warn
            INFO -> Severity.Info
            DEBUG -> Severity.Debug
            else -> Severity.Verbose
        }

        val formatted = if (messagePattern != null && arguments != null) {
            org.slf4j.helpers.MessageFormatter
                .arrayFormat(messagePattern, arguments)
                .message
        } else {
            null
        }

        messagePattern.let {
            logger.log(
                severity,
                marker?.toString() ?: defaultTag,
                throwable,
                formatted ?: (messagePattern ?: "")
            )
        }
    }
}
