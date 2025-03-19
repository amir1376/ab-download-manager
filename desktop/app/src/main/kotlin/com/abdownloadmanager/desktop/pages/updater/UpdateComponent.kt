package com.abdownloadmanager.desktop.pages.updater

import com.abdownloadmanager.desktop.utils.AppVersion
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.desktop.NotificationSender
import com.abdownloadmanager.desktop.ui.widget.MessageDialogType
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ir.amirab.util.compose.asStringSource

class UpdateComponent(
    ctx: ComponentContext,
    private val notificationSender: NotificationSender,
) : BaseComponent(ctx),
    KoinComponent {
    private val updateManager: UpdateManager by inject()

    val currentVersion = AppVersion.get()
    val showNewUpdate = MutableStateFlow(false)
    val newVersionData = updateManager.newVersionData
    private var updateApplierJob: Job? = null

    var updateCheckStatus = updateManager.updateCheckStatus

    fun performUpdate() {
        updateApplierJob?.cancel()
        updateApplierJob = scope.launch {
            try {
                updateManager.update()
            } catch (e: Exception) {
                showMessage(e)
            }
        }
    }

    private fun showMessage(e: Exception) {
        e.printStackTrace()
        notificationSender.sendDialogNotification(
            "Update Error".asStringSource(),
            e.localizedMessage.orEmpty().asStringSource(),
            type = MessageDialogType.Error,
        )
    }

    fun showNewUpdate() {
        showNewUpdate.update { true }
    }

    fun requestCheckForUpdate() {
        scope.launch {
            updateManager
                .checkForUpdate()
                ?.let {
                    showNewUpdate()
                }
        }
    }

    fun requestClose() {
        showNewUpdate.update { false }
    }
}
