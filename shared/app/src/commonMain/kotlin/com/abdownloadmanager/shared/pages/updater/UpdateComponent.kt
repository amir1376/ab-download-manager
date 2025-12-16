package com.abdownloadmanager.shared.pages.updater

import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.ui.widget.MessageDialogType
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
    private val updateManager: UpdateManager,
) : BaseComponent(ctx),
    KoinComponent {

    fun isUpdateSupported(): Boolean {
        return updateManager.isUpdateSupported()
    }

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
            Res.string.update_error.asStringSource(),
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
