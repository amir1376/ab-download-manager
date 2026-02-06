package com.abdownloadmanager.android.util

import com.abdownloadmanager.shared.util.DefinedPaths
import okio.Path

class AndroidDefinedPaths(
    dataDir: Path,
) : DefinedPaths(
    dataDir = dataDir
) {
    val lastSavedLocationFile = pagesStateDir.resolve("lastSavedLocation.json")
    val onboardingFile = pagesStateDir.resolve("onboarding.json")
    val homePageFile = pagesStateDir.resolve("home.json")
    val browserBookmarksFile = pagesStateDir.resolve("browser_bookmarks.json")
}
