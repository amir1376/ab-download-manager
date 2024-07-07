package com.abdownloadmanager.desktop.utils

import java.io.File
import kotlin.concurrent.thread

object IntegrationPortBroadcaster {
    const val INTEGRATION_DISABLED=-1
    const val INTEGRATION_UNKNOWN=-2

    private var requestedToCleanOnClose = false
    /*fun cleanOnClose() {
        if (requestedToCleanOnClose) return
        Runtime.getRuntime().addShutdownHook(
            thread(
                start = false
            ) {
                setIntegrationPortInFile(null)
            }
        )
    }*/
//    private val portFIle get() = File(AppProperties.getConfigDirectory(), "integration.port")
    private var port:Int=INTEGRATION_UNKNOWN
    fun isInitialized(): Boolean {
        return port!=INTEGRATION_UNKNOWN
    }

    fun setIntegrationPortInFile(portNumber: Int?) {
        port = portNumber ?: INTEGRATION_DISABLED
    }

    fun getIntegrationPort(): Int {
        return port
    }
}
