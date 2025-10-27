package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.desktop.storage.DesktopDefinedPaths
import com.abdownloadmanager.shared.util.AppVersion
import ir.amirab.util.platform.Platform
import okio.Path.Companion.toOkioPath
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
                    Platform.Desktop.MacOS -> it.findAppFolder() // /Applications/ABDownloadManager.app
                    Platform.Desktop.Windows -> it // <installationFolder>/ABDownloadManager.exe
                    else -> null
                }?.path
            }
    }

    private fun getUserDataDir(): File {
        val dataDirName = SharedConstants.dataDirName
        return File(System.getProperty("user.home"), dataDirName)
    }

    val dataDir by lazy {
        PortableUtil.getPortableDataDir(installationFolder) ?: getUserDataDir()
    }
    val definedPaths = DesktopDefinedPaths(dataDir.toOkioPath())
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
