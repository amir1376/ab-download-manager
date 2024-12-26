package com.abdownloadmanager.updatechecker

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform
import kotlinx.coroutines.delay

class DummyUpdateChecker(currentVersion: Version) : UpdateChecker(currentVersion) {
    override suspend fun getMyPlatformLatestVersion(): UpdateInfo {
        val newVersion = currentVersion.copy(
            major = currentVersion.minor + 1,
            preRelease = null,
            buildMetadata = null,
        )
        delay(1000)
//        error("Something wrong")
        return UpdateInfo(
            version = newVersion,
            platform = Platform.getCurrentPlatform(),
            arch = Arch.getCurrentArch(),
            updateSource = listOf(
                UpdateSource.DirectDownloadLink(
                    link = "http://127.0.0.1:8080/ABDownloadManager_1.4.4_windows_x64.zip",
                    name = "ABDownloadManager_1.4.4_windows_x64.zip",
                    hash = "md5:0123456789abcdef",
                )
            ),
            changeLog = """
                1. there is an improve on download engine.
                2. fix known bugs.
            """.trimIndent()
        )
    }
}