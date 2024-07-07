package com.abdownloadmanager.desktop.ui.customwindow

import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.util.ScreenSurface
import com.abdownloadmanager.desktop.ui.WithContentColor
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.event.FocusListener
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener


@Composable
fun BaseOptionDialog(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    resizeable:Boolean=true,
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
        val focusListener=remember {
            object : WindowFocusListener {
                override fun windowGainedFocus(e: WindowEvent?) {
                    //do nothing
                }

                override fun windowLostFocus(e: WindowEvent) {
                    onCloseRequest()
                }
            }
        }
        DisposableEffect(window){
            window.addWindowFocusListener(focusListener);
            window.isAlwaysOnTop=true
            //we need this to allow click outside
            window.modalityType=java.awt.Dialog.ModalityType.MODELESS
            onDispose{
                window.removeWindowFocusListener(focusListener)
            }
        }
//        window.subtractInset()
        content()
    }
}