package ir.amirab.downloader.utils

import java.util.logging.Logger

inline fun <reified T> T.thisLogger(): Logger {
    return Logger.getLogger(T::class.qualifiedName)
}