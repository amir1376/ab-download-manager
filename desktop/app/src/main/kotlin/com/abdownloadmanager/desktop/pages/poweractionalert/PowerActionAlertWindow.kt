package com.abdownloadmanager.desktop.pages.poweractionalert

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.LoadingIndicatorWithBrush
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun PowerActionAlert(appComponent: AppComponent) {
    appComponent.openedPowerAction.rememberChild()?.let {
        PowerActionAlertWindow(it)
    }
}

@Composable
private fun PowerActionAlertWindow(
    component: PowerActionComponent
) {
    val uiScale = LocalUiScale.current
    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(
            width = 450.dp,
            height = 200.dp,
        ).applyUiScale(uiScale),
    )
    CustomWindow(
        onCloseRequest = component::performCancel,
        state = windowState,
        alwaysOnTop = true,
        resizable = false,
        onRequestMinimize = null,
        onRequestToggleMaximize = null,
    ) {
        PowerActionAlertPage(component)
    }
}

@Composable
private fun PowerActionAlertPage(component: PowerActionComponent) {
    val totalTime = component.totalDelay
    val remainingTime by component.remainingDelay.collectAsState()
    val powerActionError = component.powerActionError.collectAsState().value
    val cancel = component::performCancel
    val performPowerActionNow = component::performPowerAction
    val remainingSeconds = remainingTime / 1000
    WindowTitle(myStringResource(Res.string.shutdown_alert))
    Column {
        Row(
            Modifier
                .weight(1f)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                val progress = animateFloatAsState(
                    (remainingTime.toFloat() / totalTime)
                ).value
                val strokeWidth = 4.dp
                Row(
                    Modifier
                        .align(Alignment.Center)
                        .padding(strokeWidth * 4),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    if (powerActionError == null) {
                        Text(
                            text = remainingSeconds.toString().padStart(2, '0'),
                            fontSize = myTextSizes.x3l,
                            modifier = Modifier,
                        )
                        Text(
                            "s",
                            fontSize = myTextSizes.xl,
                        )
                    } else {
                        Text(
                            text = myStringResource(Res.string.error),
                            fontSize = myTextSizes.x3l,
                            modifier = Modifier,
                        )
                    }
                }
                LoadingIndicatorWithBrush(
                    Modifier.matchParentSize()
                        .aspectRatio(1f),
                    brush = if (powerActionError == null) {
                        myColors.primaryGradient
                    } else {
                        myColors.errorGradient
                    },
                    progress = if (powerActionError == null) {
                        progress
                    } else {
                        1f
                    },
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (powerActionError == null) {
                        myStringResource(Res.string.system_shutdown_soon)
                    } else {
                        myStringResource(Res.string.system_shutdown_failed)
                    },
                    fontSize = myTextSizes.x3l,
                    fontWeight = FontWeight.Bold,
                    color = if (powerActionError == null) {
                        myColors.warning
                    } else {
                        myColors.error
                    },
                )
                Column(
                    Modifier.padding(end = 8.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    val description = if (powerActionError == null) {
                        myStringResource(Res.string.system_shutdown_soon_description)
                    } else {
                        (powerActionError
                            .localizedMessage
                            .takeIf { it.isNotBlank() }
                            ?.asStringSource()
                            ?: Res.string.unknown_error.asStringSource()
                                )
                            .rememberString()
                    }
                    Text(
                        description
                    )
                    component
                        .powerActionReason?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it.message.rememberString(),
                                color = when (it.type) {
                                    PowerActionComponent.PowerActionReason.Type.Success -> myColors.success
                                    PowerActionComponent.PowerActionReason.Type.Warning -> myColors.warning
                                    PowerActionComponent.PowerActionReason.Type.Error -> myColors.error
                                }
                            )
                        }
                }
            }
        }
        Actions(
            modifier = Modifier,
            isShuttingDown = component.isShuttingDown.collectAsState().value,
            cancel = cancel,
            performPowerActionNow = performPowerActionNow,
        )
    }
}

@Composable
private fun Actions(
    modifier: Modifier,
    isShuttingDown: Boolean,
    cancel: () -> Unit,
    performPowerActionNow: () -> Unit,
) {
    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ActionButton(
                Res.string.shutdown_now.asStringSource().rememberString(),
                enabled = !isShuttingDown,
                modifier = Modifier,
                onClick = performPowerActionNow,
            )
            Spacer(Modifier.width(8.dp))
            ActionButton(
                myStringResource(Res.string.cancel),
                modifier = Modifier,
                onClick = cancel,
            )
        }
    }
}
