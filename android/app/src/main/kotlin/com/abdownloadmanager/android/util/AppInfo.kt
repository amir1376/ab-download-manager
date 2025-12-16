package com.abdownloadmanager.android.util

import android.content.Context
import com.abdownloadmanager.android.BuildConfig
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.DefinedPaths
import com.abdownloadmanager.shared.util.SharedConstants
import ir.amirab.util.platform.Platform
import okio.Path.Companion.toOkioPath

object AppInfo {
    val isInDebugMode: Boolean = BuildConfig.DEBUG
    lateinit var context: Context
    fun init(context: Context) {
        this.context = context
    }

    val platform = Platform.Android
    val version = AppVersion.get()

    val definedPaths by lazy {
        AndroidDefinedPaths(
            dataDir = context.dataDir.resolve(
                SharedConstants.dataDirName
            ).toOkioPath()
        )
    }
}
