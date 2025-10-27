package com.abdownloadmanager.android.pages.settings

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
}
