package com.abdownloadmanager.desktop.pages.settings

import kotlinx.serialization.Serializable

@Serializable
data class SettingPageStateToPersist(
    val windowSize: Pair<Float, Float> = 800f to 400f
)
