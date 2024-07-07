import ir.amirab.util.platform.Platform

object MyPlugins {
    private const val namespace = "myPlugins"
    const val kotlin = "$namespace.kotlin"
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

    const val ksp = "com.google.devtools.ksp"
    const val compose = "org.jetbrains.compose"
    const val composeCompiler = "org.jetbrains.kotlin.plugin.compose"
    const val changeLog = "org.jetbrains.changelog"
    const val buildConfig = "com.github.gmazzo.buildconfig"
    const val aboutLibraries = "com.mikepenz.aboutlibraries.plugin"
}