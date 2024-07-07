package com.abdownloadmanager.desktop.utils.windowUtil

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.PopupPositionProviderAtPosition
import ir.amirab.util.desktop.GlobalLayoutDirection
import java.awt.Component
import java.awt.Insets
import java.awt.Toolkit
import java.awt.Window

fun Window.moveSafe(
    position: DpOffset,
    alignment: Alignment = Alignment.BottomEnd,
) {
    val window = this
    val p = PopupPositionProviderAtPosition(
        positionPx = Offset.Zero,
        isRelativeToAnchor = true,
        offsetPx = Offset.Zero,
        alignment = alignment,
        windowMarginPx = 0,
    )

    val screenSize = getScreenSize()
    val insets = getScreenInsets(window)
    val offset = p.calculatePosition(
        popupContentSize = IntSize(
            window.width, window.height
        ),
        layoutDirection = GlobalLayoutDirection,
        windowSize = screenSize - insets,
        anchorBounds = IntRect(
            position.x.value.toInt(),
            position.y.value.toInt(),
            position.x.value.toInt(),
            position.y.value.toInt(),
        ),
    ) + insets
    window.setLocation(
        offset.x,
        offset.y,
    )
}

fun getScreenInsets(component: Component): Insets {
    return runCatching {
        Toolkit.getDefaultToolkit().getScreenInsets(component.graphicsConfiguration)
    }.getOrElse {
        Insets(0, 0, 0, 0)
    }
}

private operator fun IntSize.minus(insets: Insets): IntSize {
    return IntSize(
        width - (insets.left + insets.right),
        height - (insets.top + insets.bottom)
    )
}

private operator fun IntOffset.plus(insets: Insets): IntOffset {
    return copy(
        x = x + insets.left,
        y = y + insets.top,
    )
}

//it is dp size!
private fun getScreenSize(): IntSize {
    Toolkit.getDefaultToolkit().screenSize.run {
        return IntSize(
            width, height
        )
    }
}
