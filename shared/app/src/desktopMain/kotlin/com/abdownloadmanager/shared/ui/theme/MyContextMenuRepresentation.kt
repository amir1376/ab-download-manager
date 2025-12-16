package com.abdownloadmanager.shared.ui.theme

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuRepresentation
import androidx.compose.foundation.ContextMenuState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.abdownloadmanager.shared.ui.widget.menu.custom.SubMenu
import com.abdownloadmanager.shared.ui.widget.rememberMyPopupPositionProviderAtPosition
import ir.amirab.util.compose.action.buildMenu
import ir.amirab.util.compose.asStringSource

private class MyContextMenuRepresentation : ContextMenuRepresentation {
    @Composable
    override fun Representation(state: ContextMenuState, items: () -> List<ContextMenuItem>) {
        val status = state.status
        if (status !is ContextMenuState.Status.Open) {
            return
        }
        val contextItems = items()
        val menuItems = remember(contextItems) {
            buildMenu {
                contextItems.map {
                    item(title = it.label.asStringSource(), onClick = {
                        it.onClick()
                    })
                }
            }
        }
        val onCloseRequest = { state.status = ContextMenuState.Status.Closed }
        Popup(
            properties = PopupProperties(
                focusable = true,
            ),
            onDismissRequest = onCloseRequest,
            popupPositionProvider = rememberMyPopupPositionProviderAtPosition(
                positionPx = status.rect.center
            ),
        ) {
            SubMenu(menuItems, onCloseRequest)
        }
    }
}

@Composable
internal fun myContextMenuRepresentation(): ContextMenuRepresentation {
    return remember {
        MyContextMenuRepresentation()
    }
}
