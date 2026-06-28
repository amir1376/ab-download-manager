plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
version = 1
group = "com.xeton.plugin"
dependencies {
    implementation("com.xeton.util:platform:1")
    implementation(libs.handlebarsJava)
}
gradlePlugin {
    plugins {
        create("installer-plugin") {
            id = "com.xeton.installer-plugin"
            implementationClass = "com.xeton.installer.InstallerPlugin"
        }
    }
}
