package com.abdownloadmanager.android.ui.configurable

import com.abdownloadmanager.android.ui.configurable.comon.item.PermissionConfigurable
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ContainsConfigurableRenderers

data class AndroidConfigurableRenderers(
    val permissionConfigurableRenderers: ConfigurableRenderer<PermissionConfigurable>,
) : ContainsConfigurableRenderers {
    override fun getAllRenderers(): Map<Configurable.Key, ConfigurableRenderer<*>> {
        return mapOf(
            PermissionConfigurable.Key to permissionConfigurableRenderers,
        )
    }
}
