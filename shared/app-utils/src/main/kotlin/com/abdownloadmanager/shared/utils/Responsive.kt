package com.abdownloadmanager.shared.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface ResponsiveSize {
    val maxHeight: Dp
    val maxWidth: Dp
}

@Immutable
data class WindowSize(
    override val maxHeight: Dp,
    override val maxWidth: Dp,
) : ResponsiveSize

@Immutable
data class ContainerSize(
    override val maxHeight: Dp,
    override val maxWidth: Dp,
    private val containerName: String
) : ResponsiveSize

@Composable
fun provideContainerSize(
    maxHeight: Dp,
    maxWidth: Dp,
    name: String? = null
): ProvidedValue<ContainerSize> {
    return LocalContainerSize provides ContainerSize(
        maxHeight,
        maxWidth,
        name ?: "not specified container"
    )
}

@Composable
fun provideWindowSize(
    maxHeight: Dp,
    maxWidth: Dp,
): ProvidedValue<WindowSize> {
    return LocalWindowSize provides WindowSize(maxHeight, maxWidth)
}

@Composable
fun ResponsiveBox(content: @Composable BoxWithConstraintsScope.() -> Unit) {
    BoxWithConstraints {
        CompositionLocalProvider(
            provideContainerSize(maxHeight, maxWidth),
        ) {
            content()
        }
    }
}

enum class ResponsiveTarget {
    Phone,
    Tablet,
    Desktop,
}

@Composable
fun rememberResponsiveWidth(): ResponsiveTarget {
    return when (LocalContainerSize.current.maxWidth) {
        in 0.dp..599.dp -> ResponsiveTarget.Phone
        in 600.dp..1199.dp -> ResponsiveTarget.Tablet
        else -> ResponsiveTarget.Desktop
    }
}


val LocalWindowSize = compositionLocalOf<WindowSize> { error("not initialized") }
val LocalContainerSize = compositionLocalOf<ContainerSize> { error("not initialized") }