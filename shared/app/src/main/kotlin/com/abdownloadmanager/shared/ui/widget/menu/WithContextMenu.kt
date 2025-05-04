package com.abdownloadmanager.shared.ui.widget.menu

import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider
import ir.amirab.util.compose.action.MenuItem

@Composable
fun WithContextMenu(
    menuProvider: () -> List<MenuItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val menu = remember(menuProvider) {
        mutableStateOf(emptyList<MenuItem>())
    }
    val onDismissRequest = {
        menu.value = emptyList()
    }
    Box(
        modifier.onClick(
            matcher = PointerMatcher.mouse(
                PointerButton.Secondary
            )
        ) {
            menu.value = menuProvider()
        }
    ) {
        content()
        if (menu.value.isNotEmpty()) {
            Popup(
                popupPositionProvider = rememberCursorPositionProvider(
                    alignment = Alignment.BottomEnd
                ),
                onDismissRequest = onDismissRequest
            ) {
                SubMenu(
                    subMenu = menu.value,
                    onRequestClose = onDismissRequest,
                )
            }
        }
    }
}
