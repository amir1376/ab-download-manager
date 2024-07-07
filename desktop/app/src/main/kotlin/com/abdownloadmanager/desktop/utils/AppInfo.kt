package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.desktop.SharedConstants
import ir.amirab.util.platform.Platform
import java.io.File

object AppInfo {
    val name = SharedConstants.appName
    val packageName = SharedConstants.packageName
    val website = SharedConstants.projectWebsite
    val sourceCode = SharedConstants.projectSourceCode


    val version = AppVersion.get()
    val platform = Platform.getCurrentPlatform()
    val exeFile: String? = run {
//        if (!AppProperties.isAppInstalled()){
//            return@run null
//        }
        System.getProperty("jpackage.app-path")
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

val AppInfo.configDir: File get() = File(AppProperties.getConfigDirectory())
val AppInfo.downloadDbDir:File get() =  AppInfo.configDir.resolve("download_db")
