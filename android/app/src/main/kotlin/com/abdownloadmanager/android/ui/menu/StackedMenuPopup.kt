package com.abdownloadmanager.android.ui.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.asStringSource

@Composable
fun StackedMenuPopup(
    menu: List<MenuItem>,
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stack = remember(menu) {
        mutableStateListOf(
            MenuItem.SubMenu(
                title = "".asStringSource(),
                items = menu,
            )
        )
    }
    val onDismiss: () -> Unit = {
        if (stack.size == 1) {
            onDismissRequest()
        } else {
            stack.removeAt(stack.lastIndex)
        }
    }
    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
        )
    ) {
        RenderMenuInSinglePage(
            menuStack = stack,
            modifier = modifier,
            onDismissRequest = onDismiss,
        )
    }
}