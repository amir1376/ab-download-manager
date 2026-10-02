package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Stable
import com.abdownloadmanager.shared.util.ui.MyColors

@Stable
data class ThemePack(
    val id: String,
    val name: String,
    val dark: MyColors,
    val light: MyColors,
)
