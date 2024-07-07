package myPlugins

plugins {
    id("myPlugins.composeBase")
}

dependencies {
    api(compose.desktop.currentOs){
        exclude("org.jetbrains.compose.material")
    }
}