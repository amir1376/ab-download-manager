package com.abdownloadmanager.android.pages.settings

import android.content.Context
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.requestIgnoreBatteryOptimizationPermission
import com.abdownloadmanager.android.util.pagemanager.PermissionsPageManager
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.NavigatableConfigurable
import ir.amirab.util.compose.asStringSource


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
    fun ignoreBatteryOptimizations(
        context: Context
    ): NavigatableConfigurable {
        return NavigatableConfigurable(
            title = Res.string.permissions_ignore_battery_optimization_title.asStringSource(),
            description = Res.string.permissions_ignore_battery_optimization_reason.asStringSource(),
            onRequestNavigate = {
                requestIgnoreBatteryOptimizationPermission(context, true)
            },
        )
    }
}
