package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.ActionContainer
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale
import java.awt.Dimension

@Suppress("unused")
sealed class ConfirmDialogType {
    data object Success : ConfirmDialogType()
    data object Info : ConfirmDialogType()
    data object Error : ConfirmDialogType()
    data object Warning : ConfirmDialogType()
}

@Composable
fun ConfirmDialog(
    title: StringSource,
    message: StringSource,
    type: ConfirmDialogType,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val uiScale = LocalUiScale.current
    val h = 180.applyUiScale(uiScale)
    val w = 400.applyUiScale(uiScale)
    val state = rememberWindowState(
        size = DpSize(w.dp, h.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )
    CustomWindow(
        state,
        onRequestMinimize = null,
        onRequestToggleMaximize = null,
        onCloseRequest = onCancel,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(w, h)
        }
        val typeName = type.toString()
        WindowTitle(typeName)
        Column {
            Row(
                Modifier
                    .weight(1f)
                    .padding(8.dp),
            ) {
                val color = when (type) {
                    ConfirmDialogType.Error -> myColors.info
                    ConfirmDialogType.Info -> myColors.warning
                    ConfirmDialogType.Success -> myColors.success
                    ConfirmDialogType.Warning -> myColors.warning
                }
                MyIcon(
                    icon = MyIcons.info,
                    tint = color,
                    modifier = Modifier
                        .padding(16.dp)
                        .requiredSize(36.dp),
                    contentDescription = null,
                )
                Column {
                    Text(
                        title.rememberString(),
                        fontSize = myTextSizes.xl,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        message.rememberString(),
                        fontSize = myTextSizes.base,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            ActionContainer(
                Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    val confirmFocusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        confirmFocusRequester.requestFocus()
                    }
                    ActionButton(
                        myStringResource(Res.string.ok),
                        onClick = onConfirm,
                        modifier = Modifier.focusRequester(confirmFocusRequester)
                    )
                    Spacer(Modifier.width(8.dp))
                    ActionButton(
                        myStringResource(Res.string.cancel),
                        onClick = onCancel
                    )
                }
            }
        }

    }
}
