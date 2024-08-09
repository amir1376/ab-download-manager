package myPlugins

plugins {
    kotlin("jvm")
}
repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}

fun getOptIns() = setOf(
    "com.russhwolf.settings.ExperimentalSettingsApi",
    "com.arkivanov.decompose.ExperimentalDecomposeApi",
    "androidx.compose.animation.ExperimentalAnimationApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.ui.ExperimentalComposeUiApi",
)
fun getFeatures() = setOf(
    "context-receivers"
)

kotlin {
    compilerOptions {
        val optIns = getOptIns().map { "-Xopt-in=$it" }
        val features = getFeatures().map { "-X$it" }
        freeCompilerArgs.set(optIns + features)
    }
}