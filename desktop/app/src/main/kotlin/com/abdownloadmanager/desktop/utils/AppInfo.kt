package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.SharedConstants
import ir.amirab.util.platform.Platform
import java.io.File

object AppInfo {
    val name = SharedConstants.appName
    val displayName = SharedConstants.appDisplayName
    val packageName = SharedConstants.packageName
    val website = SharedConstants.projectWebsite
    val sourceCode = SharedConstants.projectSourceCode
    val translationsUrl = SharedConstants.projectTranslations


    val version = AppVersion.get()
    val platform = Platform.getCurrentPlatform()
    val exeFile: String? = run {
//        if (!AppProperties.isAppInstalled()){
//            return@run null
//        }
        System.getProperty("jpackage.app-path")
    }

    private fun File.findAppFolder() = generateSequence(this) { it.parentFile }
        .firstOrNull { it.name.endsWith(".app") }

    val installationFolder: String? = run {
        exeFile?.let(::File)
            ?.parentFile // executable path
            ?.let {
                when (Platform.getCurrentPlatform()) {
                    Platform.Desktop.Linux -> it.parentFile // <installationFolder>/bin/ABDownloadManager
                    Platform.Desktop.MacOS -> it.findAppFolder()
                    Platform.Desktop.Windows -> it // <installationFolder>/ABDownloadManager.exe
                    else -> null
                }?.path
            }
    }

    private fun getPortableDataDir(): File? {
        val dataDirName = SharedConstants.dataDirName
        if (installationFolder != null) {
            val portableDataDir = File(installationFolder, dataDirName)
            if (portableDataDir.exists() && portableDataDir.canWrite()) {
                return portableDataDir
            }
        }
        return null
    }

    private fun getUserDataDir(): File {
        val dataDirName = SharedConstants.dataDirName
        return File(System.getProperty("user.home"), dataDirName)
    }

    val dataDir by lazy {
        getPortableDataDir() ?: getUserDataDir()
    }
}

fun AppInfo.isAppInstalled(): Boolean {
    return AppInfo.exeFile != null
}

fun AppInfo.isInIDE(): Boolean {
    return !isAppInstalled()
}

fun AppInfo.isInDebugMode(): Boolean {
    return AppArguments.get().debug || AppProperties.isDebugMode() || isInIDE()
}

val AppInfo.configDir: File get() = dataDir.resolve("config")
val AppInfo.systemDir: File get() = dataDir.resolve("system")
val AppInfo.updateDir: File get() = AppInfo.systemDir.resolve("update")
val AppInfo.logDir: File get() = AppInfo.systemDir.resolve("log")
val AppInfo.optionsDir: File get() = AppInfo.configDir.resolve("options")
val AppInfo.downloadDbDir: File get() = AppInfo.configDir.resolve("download_db")
