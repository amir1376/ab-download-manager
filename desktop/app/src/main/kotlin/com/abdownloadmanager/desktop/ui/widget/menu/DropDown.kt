package com.abdownloadmanager.desktop.ui.widget.menu

import DropdownMenuPositionProvider
import SiblingMenuPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberComponentRectPositionProvider

@Composable
fun MyDropDown(
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable () -> Unit,
) {
    val positionProvider = rememberComponentRectPositionProvider(
        offset = offset,
        anchor = Alignment.BottomStart,
        alignment = Alignment.BottomEnd,
    )
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
    ) {
        content()
    }
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