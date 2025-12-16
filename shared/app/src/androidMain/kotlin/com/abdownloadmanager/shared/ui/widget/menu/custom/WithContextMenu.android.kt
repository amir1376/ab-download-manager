package com.abdownloadmanager.shared.ui.widget.menu.custom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Popup
import com.abdownloadmanager.shared.ui.widget.rememberMyPopupPositionProviderAtPosition
import ir.amirab.util.compose.action.MenuItem

@Composable
actual fun WithContextMenu(
    menuProvider: () -> List<MenuItem>,
    modifier: Modifier,
    content: @Composable (() -> Unit)
) {
    val menu = remember(menuProvider) {
        mutableStateOf(emptyList<MenuItem>())
    }
    val onDismissRequest = {
        menu.value = emptyList()
    }
    var lastClickPosition: Offset? by remember {
        mutableStateOf(null)
    }
    Box(
        modifier.onLongPress {
            menu.value = menuProvider()
            lastClickPosition = it
        }
    ) {
        content()
        if (menu.value.isNotEmpty()) {
            Popup(
                popupPositionProvider = rememberMyPopupPositionProviderAtPosition(
                    positionPx = lastClickPosition
                    // shouldn't happen! just to not use !!
                        ?: Offset.Zero,
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

private fun Modifier.onLongPress(
    onLongPress: (Offset) -> Unit
): Modifier = pointerInput(Unit) {
    detectTapGestures(
        onLongPress = { onLongPress(it) }
    )
}
