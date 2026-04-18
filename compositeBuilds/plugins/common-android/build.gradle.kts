plugins {
    `kotlin-dsl`
}

version = 1
group = "ir.amirab.plugin"
dependencies {
    implementation(libs.pluginAndroidGradle)
    implementation(libs.handlebarsJava)
    implementation(libs.okio.okio)
}
