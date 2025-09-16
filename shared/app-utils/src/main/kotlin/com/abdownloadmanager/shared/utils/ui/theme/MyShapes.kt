package com.abdownloadmanager.shared.utils.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape


val LocalMyShapes = staticCompositionLocalOf<MyShapes> {
    error("LocalMyShapes not provided")
}

val myShapes
    @Composable
    get() = LocalMyShapes.current

@Stable
data class MyShapes(
    val defaultRounded: RoundedCornerShape,
)
