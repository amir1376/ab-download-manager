package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Info
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.util.compose.StringSource
import java.awt.Dimension
import java.util.UUID

@Suppress("unused")
sealed class MessageDialogType {
    data object Success : MessageDialogType()
    data object Info : MessageDialogType()
    data object Error : MessageDialogType()
    data object Warning : MessageDialogType()
}

data class MessageDialogModel(
    val id: String = UUID.randomUUID().toString(),
    val title: StringSource,
    val description: StringSource,
    val type: MessageDialogType = MessageDialogType.Info,
)

@Composable
fun ShowMessageDialogs(
    appComponent: AppComponent,
) {
    val list by appComponent.dialogMessages.collectAsState()

    for (msg in list) {
        MessageDialog(
            msgContent = msg,
            onConfirm = {
                appComponent.onDismissDialogMessage(msg)
            }
        )
    }
}

@Composable
fun MessageDialog(
    msgContent: MessageDialogModel,
    onConfirm: () -> Unit,
) {
    val h = 200
    val w = 400
    val state = rememberWindowState(
        size = DpSize(w.dp, h.dp)
    )
    CustomWindow(
        state,
        onRequestMinimize = null,
        onRequestToggleMaximize = null,
        onCloseRequest = onConfirm,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(Unit){
            window.minimumSize = Dimension(w,h)
        }
        val typeName = msgContent.type.toString()
        WindowTitle(typeName)
        Row(
            Modifier.padding(8.dp),
        ) {
            val color = when (msgContent.type) {
                MessageDialogType.Error -> myColors.info
                MessageDialogType.Info -> myColors.warning
                MessageDialogType.Success -> myColors.success
                MessageDialogType.Warning -> myColors.warning
            }
            Icon(
                imageVector = AbIcons.Default.Info,
                tint = color,
                modifier = Modifier
                    .padding(16.dp)
                    .requiredSize(36.dp),
                contentDescription = null,
            )
            Column {
                Text(
                    msgContent.title.rememberString(),
                    fontSize = myTextSizes.xl,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    msgContent.description.rememberString(),
                    fontSize = myTextSizes.base,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    ActionButton("Ok", onClick = onConfirm)
                }
            }
        }
    }
}