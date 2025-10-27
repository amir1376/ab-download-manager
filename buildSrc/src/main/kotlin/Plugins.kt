import ir.amirab.util.platform.Platform

object MyPlugins {
    private const val namespace = "myPlugins"
    const val kotlin = "$namespace.kotlin"
    const val kotlinAndroid = "$namespace.kotlinAndroid"
    const val kotlinMultiplatform = "$namespace.kotlinMultiplatform"
    const val composeAndroid = "$namespace.composeAndroid"
    const val composeDesktop = "$namespace.composeDesktop"
    const val composeBase = "$namespace.composeBase"
    const val proguardDesktop = "$namespace.proguardDesktop"
}
object MyPlatform{
    fun getPlatform() = Platform
}
object Plugins {
    object Kotlin {
        private const val baseName = "org.jetbrains.kotlin"
        const val serialization = "$baseName.plugin.serialization"
    }

    object Android {
        private const val baseName = "com.android"
        const val application = "$baseName.application"
        const val library = "$baseName.library"
    }

    const val ksp = "com.google.devtools.ksp"
    const val compose = "org.jetbrains.compose"
    const val composeCompiler = "org.jetbrains.kotlin.plugin.compose"
    const val changeLog = "org.jetbrains.changelog"
    const val buildConfig = "com.github.gmazzo.buildconfig"
    const val aboutLibraries = "com.mikepenz.aboutlibraries.plugin"
    const val aboutLibrariesAndroid = "com.mikepenz.aboutlibraries.plugin.android"

    const val multiplatformResources = "dev.icerock.mobile.multiplatform-resources"
}
