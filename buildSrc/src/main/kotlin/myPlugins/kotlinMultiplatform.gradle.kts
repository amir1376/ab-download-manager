package myPlugins

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}

fun getOptIns(): Set<String> = setOf(
    "androidx.compose.animation.ExperimentalAnimationApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "androidx.compose.ui.ExperimentalComposeUiApi",
)

fun getFeatures(): Set<String> = setOf(
    "context-parameters",
)

kotlin {
    compilerOptions {
        val optIns = getOptIns().map { "-Xopt-in=$it" }
        val features = getFeatures().map { "-X$it" }
        freeCompilerArgs.set(optIns + features)
    }
}
