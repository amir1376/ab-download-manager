plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
    google()
}
version = 1
group = "ir.amirab.plugin"
dependencies {
    implementation(libs.pluginAndroidGradle)
    implementation(libs.handlebarsJava)
    implementation(libs.okio.okio)
}
