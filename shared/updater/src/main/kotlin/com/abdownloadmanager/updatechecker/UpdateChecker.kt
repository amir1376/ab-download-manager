package com.abdownloadmanager.updatechecker

import GithubApi
import com.abdownloadmanager.ArtifactUtil
import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Platform
import kotlinx.coroutines.delay


private class GithubUpdateChecker(
    currentVersion: Version,
    val githubApi: GithubApi,
) : UpdateChecker(currentVersion) {
    override suspend fun getMyPlatformLatestVersion(): VersionData {
        val all=getLatestVersions()
        val versionData = all.find { it.platform == Platform.getCurrentPlatform() }
        return requireNotNull(versionData){
            "could not find latest version for current platform"
        }
    }

    suspend fun getLatestVersions(): List<VersionData> {
        val release = githubApi.getLatestReleases()
        return release.assets.mapNotNull {
            val v = ArtifactUtil.getArtifactInfo(it.name) ?: return@mapNotNull null
            VersionData(
                name = it.name,
                version = v.version,
                link = it.downloadLink,
                platform = v.platform,
                changeLog = release.body?:""
            )
        }
    }
}

class DummyUpdateChecker(currentVersion :Version): UpdateChecker(currentVersion ){
    override suspend fun getMyPlatformLatestVersion(): VersionData {
        val newVersion=currentVersion.copy(major = currentVersion.major+1)
        delay(5000)
        error("Something wrong")
        return VersionData(
            version = newVersion,
            platform = Platform.getCurrentPlatform(),
            link = "http://localhost:3000/app_1.0.1_windows.msi",
            name = "app_1.0.1_windows.msi",
            changeLog = """
                1. there is an improve on download engine.
                2. fix known bugs.        
            """.trimIndent()
        )
    }
}

abstract class UpdateChecker(
    protected val currentVersion: Version,
) {
    abstract suspend fun getMyPlatformLatestVersion(): VersionData
    suspend fun check(): VersionData? {
        val latest=getMyPlatformLatestVersion()
        requireNotNull(latest){ "There is no release for this platform" }
        return latest.takeIf {
            it.version>currentVersion
        }
    }
}