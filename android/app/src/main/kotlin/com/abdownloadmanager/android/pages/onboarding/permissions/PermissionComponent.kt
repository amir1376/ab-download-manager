package com.abdownloadmanager.android.pages.onboarding.permissions

import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow

class PermissionComponent(
    componentContext: ComponentContext,
    private val permissionManager: PermissionManager,
    private val onReady: () -> Unit,
    private val onDismiss: () -> Unit,
) : BaseComponent(componentContext) {
    val currentPermission: MutableStateFlow<PermissionsPageSteps> = MutableStateFlow(PermissionsPageSteps.Initial)
    val permissionsToAsk = permissionManager.permissions.sortedBy {
        !it.isOptional
    }

    fun goToNextPermissionPage() {
        when (val currentPermission = currentPermission.value) {
            is PermissionsPageSteps.AtPermission -> {
                val index = permissionsToAsk.indexOf(currentPermission.appPermission)
                val nextIndex = index + 1
                if (nextIndex > permissionsToAsk.lastIndex) {
                    this.currentPermission.value = PermissionsPageSteps.Done
                } else {
                    if (permissionManager.isReady(currentPermission.appPermission)) {
                        val appPermission = permissionsToAsk[nextIndex]
                        this.currentPermission.value = PermissionsPageSteps.AtPermission(appPermission)
                    }
                }
            }

            PermissionsPageSteps.Done -> {
                onReady()
            }

            PermissionsPageSteps.Initial -> {
                this.currentPermission.value = permissionsToAsk.firstOrNull()?.let {
                    PermissionsPageSteps.AtPermission(it)
                } ?: PermissionsPageSteps.Done
            }
        }
    }

    fun goToPreviousPermissionPage() {
        when (val currentPermission = currentPermission.value) {
            is PermissionsPageSteps.AtPermission -> {
                val index = permissionsToAsk.indexOf(currentPermission.appPermission)
                val previousIndex = index - 1
                this.currentPermission.value = if (previousIndex < 0) {
                    PermissionsPageSteps.Initial
                } else {
                    PermissionsPageSteps.AtPermission(permissionsToAsk[previousIndex])
                }
            }

            PermissionsPageSteps.Done -> {
                // we don't reach this!
//                onReady()
            }

            PermissionsPageSteps.Initial -> {
                onDismiss()
            }
        }
    }
}

sealed interface PermissionsPageSteps {
    data object Initial : PermissionsPageSteps
    data class AtPermission(
        val appPermission: AppPermission,
    ) : PermissionsPageSteps

    data object Done : PermissionsPageSteps
}
