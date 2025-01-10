package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.ui.widget.ShowNotification
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.UpdateCheckStatus
import com.abdownloadmanager.shared.utils.ui.theme.LocalUiScale
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.desktop.screen.applyUiScale
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
    val status = updaterComponent.updateCheckStatus.collectAsState().value

    var message by remember { mutableStateOf(null as StringSource?) }
    var notificationType by remember { mutableStateOf(null as NotificationType?) }
    LaunchedEffect(status) {
        fun CoroutineScope.clearMessageAfter(delay: Long) {
            launch {
                delay(delay)
                message = null
            }
        }
        when (status) {
            UpdateCheckStatus.Checking -> {
                message = Res.string.update_checking_for_update.asStringSource()
                notificationType = NotificationType.Loading(null)
            }

            is UpdateCheckStatus.Error -> {
                clearMessageAfter(3000)
                message = StringSource.CombinedStringSource(
                    listOf(
                        Res.string.update_check_error.asStringSource(),
                        status.e.localizedMessage.orEmpty().asStringSource(),
                    ),
                    "\n",
                )
                status.e.printStackTrace()
                notificationType = NotificationType.Error
            }

            UpdateCheckStatus.NoUpdate -> {
                clearMessageAfter(3000)
                message = Res.string.update_no_update.asStringSource()
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
            title = Res.string.update_updater.asStringSource(),
            description = message,
            type = notificationType ?: NotificationType.Info,
            tag = "Updater"
        )
    }
    if (showUpdate && newVersion != null) {
        val uiScale = LocalUiScale.current
        CustomWindow(
            state = rememberWindowState(
                size = DpSize(500.dp, 400.dp).applyUiScale(uiScale),
                position = WindowPosition.Aligned(Alignment.Center)
            ),
            onCloseRequest = closeUpdatePage,
        ) {
            NewUpdatePage(
                newVersionInfo = newVersion,
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
