import buildlogic.versioning.getAppVersionString

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id(Plugins.buildConfig)
}

dependencies {
    // Project modules — DTOs + shared utilities (datasize)
    implementation(project(":integration:server"))
    implementation(project(":shared:utils"))

    // Kotlin
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)

    // CLI framework
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    implementation("com.github.ajalt.mordant:mordant:2.7.2")

    // HTTP client (same http4k + OkHttp stack as desktop app)
    implementation(libs.http4k.core)
    implementation(libs.http4k.client.okhttp)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass = "com.abdownloadmanager.cli.CliMainKt"
    applicationName = "abdm-cli"
    applicationDefaultJvmArgs = listOf("-Djava.awt.headless=true")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

// Generate build config with version from the project's shared versioning
buildConfig {
    packageName = "com.abdownloadmanager.cli"
    buildConfigField(
        "APP_VERSION",
        provider { getAppVersionString() }
    )
}