package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import ir.amirab.util.guardedEntry
import ir.amirab.util.logger.AppLogger

enum class EntryType {
    GUI,
    CLI,
    NativeMessaging,
}

object EntrypointInitializer {
    private val booted = guardedEntry()
    fun boot(
        debug: Boolean = false,
        entryType: EntryType
    ) {
        booted.action {
            AppArguments.update {
                it.copy(
                    debug = debug,
                )
            }
            AppProperties.boot(AppInfo.definedPaths.appPropertiesFile)
            AppLogger.init(
                writeToConsole = false,
                logFilePath = AppInfo.definedPaths.logDir
                    .let {
                        it / when (entryType) {
                            EntryType.GUI -> "AppLog.log"
                            EntryType.CLI -> "CliLog.log"
                            EntryType.NativeMessaging -> "NativeMessaging.log"
                        }
                    }.takeIf {
                        AppInfo.isInDebugMode()
                    },
            )
        }
    }
}
