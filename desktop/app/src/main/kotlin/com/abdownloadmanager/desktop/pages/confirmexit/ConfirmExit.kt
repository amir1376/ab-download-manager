package com.abdownloadmanager.desktop.pages.confirmexit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.widget.ConfirmDialog
import com.abdownloadmanager.desktop.ui.widget.ConfirmDialogType
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.asStringSource

@Composable
fun ConfirmExit(appComponent: AppComponent) {
    val showExitDialog by appComponent.showConfirmExitDialog.collectAsState()
    if (showExitDialog) {
        ConfirmDialog(
            Res.string.confirm_exit.asStringSource(),
            Res.string.confirm_exit_description.asStringSource(),
            onCancel = appComponent::closeConfirmExit,
            onConfirm = appComponent::exitAppAsync,
            type = ConfirmDialogType.Warning,
        )
    }
}