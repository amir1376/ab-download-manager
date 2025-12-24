package com.abdownloadmanager.android.pages.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.isBatteryOptimizationDisabled
import com.abdownloadmanager.android.pages.onboarding.permissions.requestIgnoreBatteryOptimizationPermission
import com.abdownloadmanager.android.ui.configurable.android.item.PermissionConfigurable
import com.abdownloadmanager.android.util.pagemanager.PermissionsPageManager
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.NavigatableConfigurable
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.flow.MutableStateFlow


object AndroidSettings {
    fun permissionSettings(
        permissionsPageManager: PermissionsPageManager
    ): NavigatableConfigurable {
        return NavigatableConfigurable(
            title = Res.string.permissions.asStringSource(),
            description = "".asStringSource(),
            onRequestNavigate = {
                permissionsPageManager.openPermissionsPage(false)
            },
        )
    }
    fun ignoreBatteryOptimizations(): PermissionConfigurable {
        val permission = ABDMPermissions.BatteryOptimizationPermission
        return PermissionConfigurable(
            title = permission.title,
            description = permission.description,
            backedBy = MutableStateFlow(permission),
        )
    }
}
