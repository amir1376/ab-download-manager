package com.abdownloadmanager.updatechecker

import com.abdownloadmanager.InstallableArch
import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform

data class UpdateInfo(
    val version: Version,
    val platform: Platform,
    val arch: Arch,
    val updateSource: List<UpdateSource>,
    val changeLog: String,
)

sealed interface UpdateSource {
    data class DirectDownloadLink(
        val link: String,
        val name: String,
        val hash: String?,
        val installableArch: InstallableArch?,
    ) : UpdateSource
}
