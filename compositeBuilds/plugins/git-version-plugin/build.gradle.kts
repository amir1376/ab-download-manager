plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
version = 1
group = "com.xeton.plugin"
dependencies {
    implementation(libs.semver)
    implementation(libs.jgit)
}
gradlePlugin {
    plugins {
        create("git-version-plugin") {
            id = "com.xeton.git-version-plugin"
            implementationClass = "com.xeton.git_version.GitVersionPlugin"
        }
    }
}