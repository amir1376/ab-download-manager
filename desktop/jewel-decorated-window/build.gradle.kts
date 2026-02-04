plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}

val jewelVersion = libs.versions.jewel.get()

val jewelExclusions = Action<ExternalModuleDependency> {
    exclude(group = "org.jetbrains.compose.foundation", module = "foundation-desktop")
    exclude(group = "org.jetbrains.skiko", module = "skiko-awt")
    exclude(group = "org.jetbrains.skiko", module = "skiko-awt-runtime-all")
}

dependencies {
    implementation(libs.compose.foundation.desktop)
    implementation(libs.jna.core)
    implementation(libs.jbrApi)

    api("org.jetbrains.jewel:jewel-int-ui-decorated-window:$jewelVersion", jewelExclusions)
    api("com.jetbrains.intellij.platform:icons:253.28294.334")
}
