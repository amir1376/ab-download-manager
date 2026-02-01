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
        decoration = WindowDecoration.Undecorated(),
        transparent = true,
        resizable = resizeable,
        //we need this to allow click outside
        modalityType = DialogModalityType.Modeless,
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
            window.addWindowFocusListener(focusListener)
            window.isAlwaysOnTop = true
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
