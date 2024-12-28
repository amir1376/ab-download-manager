plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
version = 1
group = "ir.amirab.plugin"
dependencies {
    implementation("ir.amirab.util:platform:1")
    implementation(libs.handlebarsJava)
}
gradlePlugin {
    plugins {
        create("installer-plugin") {
            id = "ir.amirab.installer-plugin"
            implementationClass = "ir.amirab.installer.InstallerPlugin"
        }
    }
}