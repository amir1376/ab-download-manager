package buildlogic

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Platform
import org.jetbrains.compose.desktop.application.dsl.JvmApplicationDistributions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.File

object CiUtils {
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
}