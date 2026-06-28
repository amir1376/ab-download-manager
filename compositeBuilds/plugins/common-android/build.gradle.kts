plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
    google()
}
version = 1
group = "com.xeton.plugin"
dependencies {
    implementation(libs.pluginAndroidGradle)
    implementation(libs.handlebarsJava)
    implementation(libs.okio.okio)
}
