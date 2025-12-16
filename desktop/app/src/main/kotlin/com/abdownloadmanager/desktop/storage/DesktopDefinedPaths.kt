package com.abdownloadmanager.desktop.storage

import com.abdownloadmanager.shared.util.DefinedPaths
import okio.Path
import java.io.File

class DesktopDefinedPaths(
    dataDir: Path
) : DefinedPaths(
    dataDir
) {
    val pageStatesStorageFile: Path = configDir.resolve("pageStatesStorage.json")
}
