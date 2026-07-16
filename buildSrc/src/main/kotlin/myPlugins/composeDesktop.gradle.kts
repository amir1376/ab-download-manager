package myPlugins

plugins {
    id("myPlugins.kotlin")
    id("myPlugins.composeBase")
    id("dev.nucleusframework")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Manually add these dependencies to prevent material dependencies from sneaking in!
    val skikoVersion = libs.findVersion("skiko").get().requiredVersion
    implementation(SkikoRuntimeUtil.dependencyForCurrentOS(skikoVersion))
    implementation(libs.findLibrary("compose.runtime").get())
    implementation(libs.findLibrary("compose.foundation").get())
    implementation(libs.findLibrary("compose.ui").get())
}

object SkikoRuntimeUtil {
    /**
     * Don't use Platform.asDesktop() as I (or them) may change these names
     * https://github.com/JetBrains/skiko
     */
    private fun getCurrentOS(): String {
        val osName = System.getProperty("os.name")
        val targetOs = when {
            osName == "Mac OS X" -> "macos"
            osName.startsWith("Win") -> "windows"
            osName.startsWith("Linux") -> "linux"
            else -> error("Unsupported OS: $osName")
        }

        val osArch = System.getProperty("os.arch")
        val targetArch = when (osArch) {
            "x86_64", "amd64" -> "x64"
            "aarch64" -> "arm64"
            else -> error("Unsupported arch: $osArch")
        }
        return "$targetOs-$targetArch"
    }
    fun dependencyForCurrentOS(version: String): String {
        return "org.jetbrains.skiko:skiko-awt-runtime-${getCurrentOS()}:$version"
    }
}
