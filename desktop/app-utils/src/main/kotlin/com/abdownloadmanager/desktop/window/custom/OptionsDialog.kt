package com.abdownloadmanager.desktop.window.custom

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import com.abdownloadmanager.shared.util.ui.theme.UiScaledContent
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener


@Composable
fun BaseOptionDialog(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    resizeable: Boolean = true,
    content: @Composable WindowScope.() -> Unit,
) {
    DialogWindow(
        visible = true,
        state = state,
        undecorated = true,
        transparent = true,
        resizable = resizeable,
        onCloseRequest = onCloseRequest,
    ) {
        val focusListener = remember {
            object : WindowFocusListener {
                override fun windowGainedFocus(e: WindowEvent?) {
                    //do nothing
                }

                override fun windowLostFocus(e: WindowEvent) {
                    onCloseRequest()
                }
            }
        }
        DisposableEffect(window) {
            window.addWindowFocusListener(focusListener);
            window.isAlwaysOnTop = true
            //we need this to allow click outside
            window.modalityType = java.awt.Dialog.ModalityType.MODELESS
            onDispose {
                window.removeWindowFocusListener(focusListener)
            }
        }
//        window.subtractInset()
        UiScaledContent {
            content()
        }
    }
}
