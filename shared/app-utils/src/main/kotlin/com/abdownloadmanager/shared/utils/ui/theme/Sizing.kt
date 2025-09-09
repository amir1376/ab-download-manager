package com.abdownloadmanager.shared.utils.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

val LocalSystemDensity = staticCompositionLocalOf<Density> {
    error("LocalSystemDensity not provided")
}

const val DEFAULT_UI_SCALE = 1f

val LocalUiScale = staticCompositionLocalOf<Float> { DEFAULT_UI_SCALE }

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
    val x2l: TextUnit,
    val x3l: TextUnit,
    val x4l: TextUnit,
    val x5l: TextUnit,
)

/**
 * put this in every window because [Window] composable override [LocalDensity]
 */
@Composable
fun UiScaledContent(
    defaultDensity: Density = LocalDensity.current,
    uiScale: Float = LocalUiScale.current,
    content: @Composable () -> Unit,
) {
    val density = remember(defaultDensity, uiScale) {
        if (uiScale == DEFAULT_UI_SCALE) {
            defaultDensity
        } else {
            Density(uiScale * defaultDensity.density)
        }
    }
    CompositionLocalProvider(
        LocalDensity provides density,
        content,
    )
}
