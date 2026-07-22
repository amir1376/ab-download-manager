package buildlogic.versioning

import dev.nucleusframework.desktop.application.dsl.TargetFormat
import io.github.z4kn4fein.semver.Version
import org.gradle.api.Project

fun Project.getAppVersion(): Version {
    return rootProject.version as Version
}

fun Project.getAppVersionString(): String {
    return rootProject.version.toString()
}
fun Version.convertToVersionCode(): Int {
    require(major in 0..1023) { "Major must be 0..1023" }
    require(minor in 0..1023) { "Minor must be 0..1023" }
    require(patch in 0..511) { "Patch must be 0..511" }

    return (major shl 19) or
            (minor shl 9)  or
            patch
}

fun Project.getAppName(): String {
    return rootProject.name
}

fun Project.getPrettifiedAppName(): String {
    return "AB Download Manager"
}
fun Project.getAppDataDirName(): String {
    return ".abdm"
}

fun Project.getApplicationPackageName(): String {
    return "com.abdownloadmanager"
}

fun Project.getAppVersionStringForPackaging(targetFormat: TargetFormat? = null): String {
    val v = getAppVersion()
    val simple = { v.run { "$major.$minor.$patch" } }
//    val semantic = { v.toString() }
//    val forRpm = { semantic().replace("-", "_") }
    return simple()
}
