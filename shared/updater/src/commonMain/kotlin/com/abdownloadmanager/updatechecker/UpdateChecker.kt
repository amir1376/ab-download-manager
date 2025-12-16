package com.abdownloadmanager.updatechecker

import io.github.z4kn4fein.semver.Version


abstract class UpdateChecker(
    protected val currentVersion: Version,
) {
    abstract suspend fun getMyPlatformLatestVersion(): UpdateInfo
    suspend fun check(): UpdateInfo? {
        val latest = getMyPlatformLatestVersion()
        require(latest.updateSource.isNotEmpty()) { "There is no release for this platform" }
        return latest.takeIf {
            it.version > currentVersion
        }
    }
}