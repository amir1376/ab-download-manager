package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.widget.NotificationModel
import com.abdownloadmanager.desktop.ui.widget.NotificationType
import com.abdownloadmanager.desktop.ui.widget.ShowNotification
import com.abdownloadmanager.desktop.ui.widget.useNotification
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShowUpdaterDialog(updaterComponent: UpdateComponent) {
    val showUpdate = updaterComponent.showNewUpdate.collectAsState().value
    val newVersion = updaterComponent.newVersionData.collectAsState().value
    val closeUpdatePage = {
        updaterComponent.requestClose()
    }
    val status = updaterComponent.updateCheckStatus

    var message by remember { mutableStateOf(null as String?) }
    var notificationType by remember { mutableStateOf(null as NotificationType?) }
    LaunchedEffect(status) {
        fun CoroutineScope.clearMessageAfter(delay: Long) {
            launch {
                delay(delay)
                message = null
            }
        }
        when (status) {
            UpdateStatus.Checking -> {
                message = "Checking for update"
                notificationType = NotificationType.Loading(null)
            }

            is UpdateStatus.Error -> {
                clearMessageAfter(3000)
                message = """
                    Error while checking for update
                    ${status.e.localizedMessage}
                    """.trimIndent()
                notificationType = NotificationType.Error
            }

            UpdateStatus.NoUpdate -> {
                clearMessageAfter(3000)
                message = "No update"
                notificationType = NotificationType.Info
            }

            else -> {
                message = null
                notificationType = null
            }
        }
    }

    message?.let { message ->
        ShowNotification(
            title = "Updater".asStringSource(),
            description = message.asStringSource(),
            type = notificationType ?: NotificationType.Info,
            tag = "Updater"
        )
    }
    if (showUpdate && newVersion != null) {
        CustomWindow(
            state = rememberWindowState(
                size = DpSize(400.dp, 400.dp)
            ),
            onCloseRequest = closeUpdatePage,
        ) {
            NewUpdatePage(
                versionVersionData = newVersion,
                currentVersion = updaterComponent.currentVersion,
                cancel = closeUpdatePage,
                update = {
                    updaterComponent.performUpdate()
                    closeUpdatePage()
                }
            )
        }
    }
}
