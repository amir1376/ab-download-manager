@file:Suppress("unused")

package com.abdownloadmanager.desktop.utils

import io.github.z4kn4fein.semver.Version
import com.abdownloadmanager.desktop.BuildConfig

object AppVersion {
    private val currentVersion= Version.parse(BuildConfig.APP_VERSION)
    fun get() = currentVersion
}