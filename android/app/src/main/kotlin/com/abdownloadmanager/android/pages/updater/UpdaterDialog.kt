package com.abdownloadmanager.android.pages.updater

import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.ui.widget.ShowNotification
import androidx.compose.runtime.*
import com.abdownloadmanager.UpdateCheckStatus
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.updater.UpdateComponent
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdaterSheet(
    updaterComponent: UpdateComponent,
) {
    ShowUpdaterDialog(updaterComponent)
}

@Composable
private fun ShowUpdaterDialog(updaterComponent: UpdateComponent) {
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
    val isOpened = showUpdate && newVersion != null
    val uiScale = LocalUiScale.current
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(isOpened) {
        if (isOpened) {
            state.show()
        } else {
            state.hide()
        }
    }
    state.OnFullyDismissed(closeUpdatePage)
    ResponsiveDialog(
        state, state::hide
    ) {
        newVersion?.let {
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
