package com.abdownloadmanager.shared.util.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import io.github.oikvpqya.compose.fastscroller.*

val LocalMultiplatformScrollbarStyle = staticCompositionLocalOf<ScrollbarStyle> {
    error("Scrollbar style not provided")
}

fun ScrollbarAdapter.needScroll(): Boolean {
    return contentSize > viewportSize
}

fun multiplatformDefaultScrollbarStyle(): ScrollbarStyle {
    return defaultScrollbarStyle()
}

@Composable
fun MultiplatformHorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    style: ScrollbarStyle = LocalMultiplatformScrollbarStyle.current,
    reverseLayout: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enablePressToScroll: Boolean = true,
    indicator: @Composable (position: Float, isVisible: Boolean) -> Unit = { _, _ -> },
) {
    // I intentionally wrapped it seems there is a bug in the library that consume more than thickness and breaks the UI
    Box(modifier) {
        HorizontalScrollbar(
            adapter = adapter,
            style = style,
            modifier = Modifier,
            reverseLayout = reverseLayout,
            interactionSource = interactionSource,
            enablePressToScroll = enablePressToScroll,
            indicator = indicator,
        )
    }
}

@Composable
fun MultiplatformVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    style: ScrollbarStyle = LocalMultiplatformScrollbarStyle.current,
    reverseLayout: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enablePressToScroll: Boolean = true,
    indicator: @Composable (position: Float, isVisible: Boolean) -> Unit = { _, _ -> },
) {
    Box(modifier) {
        VerticalScrollbar(
            adapter = adapter,
            style = style,
            modifier = Modifier,
            reverseLayout = reverseLayout,
            interactionSource = interactionSource,
            enablePressToScroll = enablePressToScroll,
            indicator = indicator,
        )
    }
}
