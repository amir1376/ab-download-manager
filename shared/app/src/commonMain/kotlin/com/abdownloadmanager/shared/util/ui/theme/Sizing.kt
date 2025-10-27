package com.abdownloadmanager.shared.util.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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

val LocalSpacing = compositionLocalOf<MySpacings> {
    error("LocalSpacing not provided")
}
val mySpacings
    @Composable
    get() = LocalSpacing.current


@Stable
data class MySpacings(
    val thumbSize: Dp,
    val iconSize: Dp,
    val smallSpace: Dp,
    val mediumSpace: Dp,
    val largeSpace: Dp,
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
