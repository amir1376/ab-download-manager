package buildlogic

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

class CiDirs(baseDir: Provider<Directory>) {
    val releaseDir = baseDir.map { it.dir("ci-release") }
    val binariesDir = releaseDir.map { it.dir("binaries") }
    val changeNotesFile = releaseDir.map { it.file("release-notes.md") }
}
