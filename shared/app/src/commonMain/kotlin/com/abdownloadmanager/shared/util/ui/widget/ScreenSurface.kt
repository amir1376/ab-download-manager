package com.abdownloadmanager.shared.util.ui.widget

import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.WithContentColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ScreenSurface(
    modifier: Modifier,
    background: Brush,
    contentColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .background(background)
    ) {
        WithContentColor(contentColor) {
            content()
        }
    }
}

@Composable
fun ScreenSurface(
    modifier: Modifier,
    background: Color,
    contentColor: Color = myColors.getContentColorFor(background),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .background(background)
    ) {
        WithContentColor(contentColor) {
            content()
        }
    }
}
