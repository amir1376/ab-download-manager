package com.abdownloadmanager.shared.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import org.jetbrains.skiko.Cursor

actual fun Modifier.myPointerHoverIcon(
    pointerHoverIcon: MyPointerHoverIcon,
    overrideDescendants: Boolean
): Modifier {
    return pointerHoverIcon(
        pointerHoverIcon.toDesktopIcon(),
        overrideDescendants = overrideDescendants,
    )
}

private fun MyPointerHoverIcon.toDesktopIcon(): PointerIcon {
    return when (this) {
        MyPointerHoverIcon.Crosshair -> PointerIcon.Crosshair
        MyPointerHoverIcon.Default -> PointerIcon.Default
        MyPointerHoverIcon.Hand -> PointerIcon.Hand
        MyPointerHoverIcon.Text -> PointerIcon.Text
        MyPointerHoverIcon.HorizontalResize -> pointerIconFromCursorInt(Cursor.S_RESIZE_CURSOR)
        MyPointerHoverIcon.VerticalResize -> pointerIconFromCursorInt(Cursor.E_RESIZE_CURSOR)
    }
}

private fun pointerIconFromCursorInt(
    cursorInt: Int
): PointerIcon {
    return PointerIcon(Cursor(cursorInt))
}
