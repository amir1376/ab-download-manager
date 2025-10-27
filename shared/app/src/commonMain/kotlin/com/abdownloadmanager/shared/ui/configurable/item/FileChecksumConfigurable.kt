package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.util.FileChecksum
import ir.amirab.util.compose.StringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileChecksumConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<FileChecksum?>,
    describe: (FileChecksum?) -> StringSource,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : Configurable<FileChecksum?>(
    title = title,
    description = description,
    backedBy = backedBy,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey() = Key
}

