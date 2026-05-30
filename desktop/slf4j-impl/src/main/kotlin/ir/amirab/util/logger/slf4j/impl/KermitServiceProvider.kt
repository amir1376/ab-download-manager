@file:Suppress("unused")

package ir.amirab.util.logger.slf4j.impl

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import com.google.auto.service.AutoService
import ir.amirab.util.logger.AppLogger
import org.slf4j.ILoggerFactory
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.NOPMDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

@AutoService(SLF4JServiceProvider::class)
class KermitServiceProvider : SLF4JServiceProvider {
    private val markerFactory = BasicMarkerFactory()
    private val mdcAdapter = NOPMDCAdapter()
    override fun getLoggerFactory() = ILoggerFactory {
        Slf4jKermitLogger(it, AppLogger::get)
    }

    override fun getMarkerFactory() = markerFactory

    override fun getMDCAdapter() = mdcAdapter

    override fun getRequestedApiVersion() = "2.0.99"

    override fun initialize() = Unit

    companion object {
        private val writers = mutableListOf<LogWriter>().apply {
            add(CommonWriter())
        }
        private var config: StaticConfig = StaticConfig(logWriterList = writers)

        var minSeverity: Severity
            get() = config.minSeverity
            set(value) {
                config = config.copy(minSeverity = value)
            }

        fun addWriter(writer: LogWriter) {
            writers.add(writer)
            config = config.copy(logWriterList = writers)
        }

        fun setWriters(vararg writer: LogWriter) {
            writers.clear()
            writers.addAll(writer)
            config = config.copy(logWriterList = writers)
        }
    }
}
