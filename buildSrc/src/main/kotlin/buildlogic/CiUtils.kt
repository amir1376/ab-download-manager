package buildlogic

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Platform
import org.jetbrains.compose.desktop.application.dsl.JvmApplicationDistributions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.File


object CiUtils {
    fun getTargetFileName(
        packageName: String,
        appVersion: Version,
        target: TargetFormat,
    ): String {
        val fileExtension = when (target) {
            // we use archived for app image distribution
            TargetFormat.AppImage -> {
                when (Platform.getCurrentPlatform()) {
                    Platform.Desktop.Linux -> "tar.gz"
                    Platform.Desktop.MacOS -> "tar.gz"
                    Platform.Desktop.Windows -> "zip"
                    Platform.Android -> error("Android not available for now")
                }
            }

            else -> target.fileExtensionWithoutDot()
        }

        val platformName = when (target) {
            TargetFormat.AppImage -> Platform.getCurrentPlatform()
            else -> {
                val packageFileExt = target.fileExtensionWithoutDot()
                requireNotNull(Platform.fromExecutableFileExtension(packageFileExt)) {
                    "can't find platform name with this file extension: ${packageFileExt}"
                }
            }
        }.name.lowercase()
        return "${packageName}_${appVersion}_${platformName}.${fileExtension}"
    }

    fun getFileOfPackagedTarget(
        baseOutputDir: File,
        target: TargetFormat,
    ): File {
        val folder = baseOutputDir.resolve(target.outputDirName)
        val exeFile = kotlin.runCatching {
            folder.walk().first {
                it.name.endsWith(target.fileExt)
            }
        }.onFailure {
            println("error when finding packaged app for $target in: $baseOutputDir")
        }
        return exeFile.getOrThrow()
    }

    fun getFileOfDistributedArchivedTarget(
        baseOutputDir: File,
    ): File {
        val folder = baseOutputDir
        val extension = when (Platform.getCurrentPlatform()) {
            Platform.Desktop.Linux,
            Platform.Desktop.MacOS -> "tar.gz"

            Platform.Android,
            Platform.Desktop.Windows -> "zip"
        }
        val archiveFile = kotlin.runCatching {
            folder.walk().first {
                it.name.endsWith(extension)
            }
        }.onFailure {
            println("error when finding archive of unpackaged app in: $baseOutputDir")
        }
        return archiveFile.getOrThrow()
    }

    fun copyAndHashToDestination(
        src: File,
        destinationFolder: File,
        name: String,
    ) {
        val destinationExeFile = destinationFolder.resolve(name)
        src.copyTo(destinationExeFile)
        val md5File = destinationFolder.resolve("$name.md5")
        md5File.writeText(HashUtils.md5(src))
    }

    fun movePackagedAndCreateSignature(
        appVersion: Version,
        packageName: String,
        target: TargetFormat,
        basePackagedAppsDir: File,
        outputDir: File,
    ) {
        require(!outputDir.isFile) {
            "$outputDir is a file"
        }
        outputDir.mkdirs()
        require(outputDir.isDirectory) {
            "$outputDir is not directory"
        }

        val exeFile = getFileOfPackagedTarget(
            baseOutputDir = basePackagedAppsDir,
            target = target
        )

        val newName = getTargetFileName(packageName, appVersion, target)
        copyAndHashToDestination(
            src = exeFile,
            destinationFolder = outputDir,
            name = newName,
        )
    }
    /*
        fun moveAndCreateSignature(
            appVersion: Version,
            nativeDistributions: JvmApplicationDistributions,
            target: TargetFormat,
            path: File,
            output: File,
        ) {
            require(!output.isFile) {
                "$output is a file"
            }
            output.mkdirs()
            require(output.isDirectory) {
                "$output is not directory"
            }
            val folder = path.resolve(target.outputDirName)
            val exeFile = folder.walk().first {
                it.name.endsWith(target.fileExt)
            }
            val appName = requireNotNull(nativeDistributions.packageName){
                "package name must not null"
            }
            val fileExtension = exeFile.extension
            val platformName = requireNotNull(Platform.fromExecutableFileExtension(fileExtension)){
                "can't find platform name with this file extension :${fileExtension}"
            }.name.lowercase()
            val newName = "${appName}_${appVersion}_${platformName}.${fileExtension}"
            val destinationExeFile = output.resolve(newName)
            val md5File = output.resolve("$newName.md5")
            exeFile.copyTo(destinationExeFile, true)
            md5File.writeText(HashUtils.md5(exeFile))
        }
    */
}

private fun TargetFormat.fileExtensionWithoutDot() = fileExt.substring(".".length)