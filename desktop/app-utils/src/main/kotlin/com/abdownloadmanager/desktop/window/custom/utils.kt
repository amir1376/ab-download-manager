package com.abdownloadmanager.desktop.window.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.Toolkit
import java.awt.Window
import kotlin.math.max

@Composable
fun Window.subtractInset() {
    LaunchedEffect(Unit) {
        val inset = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
        val size = Toolkit.getDefaultToolkit().screenSize.apply {
            // Only one side will have an inset at any point in time, all others remain at 0
            // We need to find the one side that has it and apply it to the appropriate dimension
            width -= max(inset.left, inset.right)
            height -= max(inset.top, inset.bottom)
        }
        val rangeX = 0..size.width
        val rangeY = 0..size.height
        // Works when taskbar is on top or left of screen
        if (x !in rangeX || y !in rangeY) {
            setLocation(
                x.coerceIn(rangeX),
                y.coerceIn(rangeY)
            )
        }

        // Works for when taskbar is on right or bottom of screen
        if (x + width !in rangeX || y + height !in rangeY) {
            setLocation(
                (x + width).coerceIn(rangeX) - width,
                (y + height).coerceIn(rangeY) - height
            )
        }
    }
}