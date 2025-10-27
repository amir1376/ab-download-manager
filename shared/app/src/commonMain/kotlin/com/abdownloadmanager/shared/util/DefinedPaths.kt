package com.abdownloadmanager.shared.util

import okio.Path
import java.io.File
import kotlin.io.resolve

abstract class DefinedPaths(
    val dataDir: Path,
) {
    val configDir: Path = dataDir.resolve("config")
    val systemDir: Path = dataDir.resolve("system")
    val updateDir: Path = systemDir.resolve("update")
    val logDir: Path = systemDir.resolve("log")
    val pagesStateDir: Path = configDir.resolve("pages")
    val optionsDir: Path = configDir.resolve("options")
    val downloadDbDir: Path = configDir.resolve("download_db")
    val downloadListDir = downloadDbDir.resolve("downloadlist")
    val extraDownloadSettings: Path = downloadDbDir.resolve("extra_download_settings")
    val extraQueueSettings: Path = downloadDbDir.resolve("extra_queue_settings")
    val categoriesDir: Path = downloadDbDir.resolve("categories")
    val categoriesFile: Path = categoriesDir.resolve("categories.json")

    val partsDir: Path = downloadDbDir.resolve("parts")
    val updateDownloadLocation: Path = updateDir.resolve("downloads")

    val downloadDataDir: Path = systemDir.resolve("downloadData")
    val queuesDir: Path = downloadDbDir.resolve("queues")

    val proxySettingsFile: Path = optionsDir.resolve("proxySettings.json")
    val appSettingsFile: Path = configDir.resolve("appSettings.json")
    val perHostSettingsFile: Path = optionsDir.resolve("perHostSettings.json")
}
