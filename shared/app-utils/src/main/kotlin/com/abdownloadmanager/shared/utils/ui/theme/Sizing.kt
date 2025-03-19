package com.abdownloadmanager.shared.utils.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

val LocalSystemDensity = staticCompositionLocalOf<Density?> { null }
val LocalUiScale = staticCompositionLocalOf<Float?> { null }

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

/**
 * put this in every window because [Window] composable override [LocalDensity]
 */
@Composable
fun UiScaledContent(
    defaultDensity: Density = LocalDensity.current,
    uiScale: Float? = LocalUiScale.current,
    content: @Composable () -> Unit,
) {
    val density = remember(uiScale) {
        if (uiScale == null) {
            defaultDensity
        } else {
            Density(uiScale)
        }
    }
    CompositionLocalProvider(
        LocalDensity provides density,
        content,
    )
}