package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A configurable that lets the user pick an ordered list of network interface
 * identifiers (discovered and passed via [availableOptions]) to bind downloads
 * to. The selection order matters: concurrent downloads are spread across the
 * chosen interfaces in that order (round-robin).
 *
 * The [backedBy] value is the list of selected identifiers; an empty list means
 * "use the system default route".
 */
class NetworkInterfacesConfigurable(
    title: StringSource,
    description: StringSource,
    /**
     * Discovered options the user can pick from, as pairs of
     * (identifier, displayLabel). The identifier is what gets stored in the
     * selection; the label is shown in the UI.
     */
    val availableOptions: List<Pair<String, String>>,
    backedBy: MutableStateFlow<List<String>>,
    describe: (List<String>) -> StringSource,
    validate: (List<String>) -> Boolean = { true },
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<List<String>>(
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
