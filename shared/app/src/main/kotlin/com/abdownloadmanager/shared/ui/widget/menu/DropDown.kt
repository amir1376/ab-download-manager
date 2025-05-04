package com.abdownloadmanager.shared.ui.widget.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberComponentRectPositionProvider

@Composable
fun MyDropDown(
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset.Zero,
    anchor: Alignment = Alignment.BottomStart,
    alignment: Alignment = Alignment.BottomEnd,
    content: @Composable () -> Unit,
) {
    val positionProvider = rememberComponentRectPositionProvider(
        offset = offset,
        anchor = anchor,
        alignment = alignment,
    )
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
        content = {
            content()
        })
}

@Composable
fun SiblingDropDown(
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable () -> Unit,
) {
    val positionProvider = rememberComponentRectPositionProvider(
        anchor = Alignment.TopEnd,
        alignment = Alignment.BottomEnd,
        offset = offset,
    )
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
    ) {
        content()
    }
}
