package com.abdownloadmanager.shared.ui.configurable.item

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.Configurable
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private fun defaultSoundValidate(path: String): Boolean {
    return path.isBlank()
        || path.endsWith(".wav", ignoreCase = true)
        || path.startsWith("content://", ignoreCase = true)
        || path.startsWith("android.resource://", ignoreCase = true)
}

private fun defaultSoundDescribe(path: String): StringSource {
    return if (path.isBlank()) {
        Res.string.settings_notification_sound_default.asStringSource()
    } else {
        val name = path.replace('\\', '/').substringAfterLast('/')
        (name.ifBlank { path }).asStringSource()
    }
}

class SoundFileConfigurable(
    title: StringSource,
    description: StringSource,
    backedBy: MutableStateFlow<String>,
    describe: ((String) -> StringSource) = ::defaultSoundDescribe,
    validate: (String) -> Boolean = ::defaultSoundValidate,
    val onPreview: (() -> Unit)? = null,
    enabled: StateFlow<Boolean> = DefaultEnabledValue,
    visible: StateFlow<Boolean> = DefaultVisibleValue,
) : StringConfigurable(
    title = title,
    description = description,
    backedBy = backedBy,
    validate = validate,
    describe = describe,
    enabled = enabled,
    visible = visible,
) {
    object Key : Configurable.Key

    override fun getKey(): Configurable.Key = Key
}
