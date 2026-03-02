package myPlugins

plugins {
    kotlin("jvm")
}
repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}

fun getOptIns(): Set<String> = setOf(
    // No Compose opt-ins here — this plugin is used by non-Compose modules
)

fun getFeatures(): Set<String> = setOf(
    "context-parameters",
)

kotlin {
    compilerOptions {
        val optIns = getOptIns().map { "-opt-in=$it" }
        val features = getFeatures().map { "-X$it" }
        freeCompilerArgs.set(optIns + features)
    }
}
