plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
version = 1
group = "ir.amirab.plugin"
dependencies {
    implementation(libs.semver)
    implementation(libs.jgit)
}
gradlePlugin {
    plugins {
        create("git-version-plugin") {
            id = "ir.amirab.git-version-plugin"
            implementationClass = "ir.amirab.git_version.GitVersionPlugin"
        }
    }
}