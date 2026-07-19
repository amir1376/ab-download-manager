package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.storage.DesktopDefinedPaths
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.SharedConstants
import ir.amirab.util.platform.Platform
import okio.Path.Companion.toOkioPath
import java.io.File

object FileNameConstants {
    const val mainApp = "ABDownloadManager"
    const val cliApp = "ABDownloadManagerCli"
    const val nativeMessagingHost = "ABDownloadManagerNativeMessagingHost"
}

object AppInfo {
    val name = SharedConstants.appName
    val displayName = SharedConstants.appDisplayName
    val packageName = SharedConstants.packageName
    val website = SharedConstants.projectWebsite
    val sourceCode = SharedConstants.projectSourceCode
    val translationsUrl = SharedConstants.projectTranslations


    val version = AppVersion.get()
    val platform = Platform.getCurrentPlatform()

    val currentExecutable: String? = run {
        System.getProperty("jpackage.app-path")
    }

    fun calcExecutable(nameWithoutExtension: String): String? {
        return currentExecutable?.let {
            val file = File(it)
            val extension = file.extension
            val targetFileName = buildString(file.name.length) {
                append(nameWithoutExtension)
                if (extension.isNotEmpty()) {
                    append(".")
                    append(extension)
                }
            }
            file.parentFile.resolve(targetFileName).path
        }
    }

    val mainExeFile: String? = run {
        calcExecutable(FileNameConstants.mainApp)
    }
    val cliExeFile: String? = run {
        calcExecutable(FileNameConstants.cliApp)
    }
    val nativeMessagingExeFile: String? = run {
        calcExecutable(FileNameConstants.nativeMessagingHost)
    }

    private fun File.findAppFolder() = generateSequence(this) { it.parentFile }
        .firstOrNull { it.name.endsWith(".app") }

    val installationFolder: String? = run {
        mainExeFile?.let(::File)
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
    return AppInfo.currentExecutable != null
}

fun AppInfo.isInIDE(): Boolean {
    return !isAppInstalled()
}

fun AppInfo.isInDebugMode(): Boolean {
    return AppArguments.get().debug || AppProperties.isDebugMode() || isInIDE()
}
