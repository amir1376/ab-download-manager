package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A configurable that lets the user pick a single network interface
 * (discovered and passed via [availableOptions]) to bind the queue's downloads
 * to. The selection is a single identifier, or `null` for the system default
 * route.
 */
class NetworkInterfacesConfigurable(
    title: StringSource,
    description: StringSource,
    /**
     * Discovered options the user can pick from, as pairs of
     * (identifier, displayLabel). The identifier is what gets stored; the
     * label is shown in the UI.
     */
    val availableOptions: List<Pair<String, String>>,
    backedBy: MutableStateFlow<String?>,
    describe: (String?) -> StringSource,
    validate: (String?) -> Boolean = { true },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<String?>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    validate = validate,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}
