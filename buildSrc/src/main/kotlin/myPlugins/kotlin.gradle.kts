package myPlugins

plugins {
    kotlin("jvm")
}
repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    maven("https://www.jetbrains.com/intellij-repository/releases")
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
