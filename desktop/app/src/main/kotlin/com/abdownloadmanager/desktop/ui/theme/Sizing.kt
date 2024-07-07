package com.abdownloadmanager.desktop.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit

//val LocalUiScale = staticCompositionLocalOf { null as Float? }

val LocalTextSizes = compositionLocalOf<TextSizes> {
    error("LocalTextSizes not provided")
}

val myTextSizes
    @Composable
    get() = LocalTextSizes.current

@Stable
data class TextSizes(
    val xs: TextUnit,
    val sm: TextUnit,
    val base: TextUnit,
    val lg: TextUnit,
    val xl: TextUnit,
)