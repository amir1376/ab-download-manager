package com.abdownloadmanager.shared.ui.modifier

import androidx.compose.ui.Modifier

sealed interface MyPointerHoverIcon {
    data object Default : MyPointerHoverIcon
    data object Crosshair : MyPointerHoverIcon
    data object Text : MyPointerHoverIcon
    data object Hand : MyPointerHoverIcon
    data object VerticalResize : MyPointerHoverIcon
    data object HorizontalResize : MyPointerHoverIcon
}

expect fun Modifier.myPointerHoverIcon(
    pointerHoverIcon: MyPointerHoverIcon,
    overrideDescendants: Boolean,
): Modifier
