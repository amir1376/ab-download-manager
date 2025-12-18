package com.abdownloadmanager.android.util

import android.app.Application
import com.abdownloadmanager.android.BuildConfig
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.SharedConstants
import ir.amirab.util.platform.Platform
import okio.Path.Companion.toOkioPath

object AppInfo {
    val isInDebugMode: Boolean = BuildConfig.DEBUG
    lateinit var context: Application
    fun init(context: Application) {
        this.context = context
    }

    val platform = Platform.Android
    val version = AppVersion.get()

    val definedPaths by lazy {
        AndroidDefinedPaths(
            dataDir = context.filesDir.resolve(
                SharedConstants.dataDirName
            ).toOkioPath()
        )
    }
}
