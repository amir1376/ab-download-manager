package com.abdownloadmanager.android.ui.configurable.android.item

import com.abdownloadmanager.android.pages.onboarding.permissions.AppPermission
import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<AppPermission>,
    describe: () -> StringSource = { "".asStringSource() },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<AppPermission>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = {
        describe()
    },
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}
