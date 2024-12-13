package ir.amirab.util

import io.github.z4kn4fein.semver.Version

class AppVersionTracker(
    val previousVersion: () -> Version?,
    val currentVersion: Version,
) {
    fun isNewInstall(): Boolean {
        return previousVersion() == null
    }

    fun isUpgraded(): Boolean {
        val previousVersion = previousVersion() ?: return false
        return previousVersion < currentVersion
    }

    fun isDowngraded(): Boolean {
        val previousVersion = previousVersion() ?: return false
        return previousVersion > currentVersion
    }

    fun isNewOrUpdated() = isNewInstall() || isUpgraded()
}