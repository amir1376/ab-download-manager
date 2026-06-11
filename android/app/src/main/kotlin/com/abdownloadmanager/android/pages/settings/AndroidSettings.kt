package com.abdownloadmanager.android.pages.settings

import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.android.ui.configurable.android.item.PermissionConfigurable
import com.abdownloadmanager.android.util.pagemanager.PermissionsPageManager
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.NavigatableConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.SoundFileConfigurable
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File


object AndroidSettings {
    private fun isAllowedSoundPath(path: String): Boolean {
        return path.isBlank() || path.endsWith(".wav", ignoreCase = true)
    }

    private fun describeSoundPath(path: String) =
        if (path.isBlank()) {
            Res.string.settings_notification_sound_default.asStringSource()
        } else {
            File(path).name.ifBlank { path }.asStringSource()
        }

    fun downloadCompletedSound(appSettings: AppSettingsStorage): SoundFileConfigurable {
        return SoundFileConfigurable(
            title = Res.string.settings_download_completed_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.downloadCompletedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
        )
    }

    fun downloadErrorSound(appSettings: AppSettingsStorage): SoundFileConfigurable {
        return SoundFileConfigurable(
            title = Res.string.settings_download_error_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.downloadErrorSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
        )
    }

    fun queueStartedSound(appSettings: AppSettingsStorage): SoundFileConfigurable {
        return SoundFileConfigurable(
            title = Res.string.settings_queue_started_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.queueStartedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
        )
    }

    fun queueEndedSound(appSettings: AppSettingsStorage): SoundFileConfigurable {
        return SoundFileConfigurable(
            title = Res.string.settings_queue_ended_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.queueEndedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
        )
    }

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

    fun browserIconInLauncher(
        appSettingsStorage: AppSettingsStorage
    ): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_browser_in_launcher.asStringSource(),
            description = Res.string.settings_browser_in_launcher_description.asStringSource(),
            backedBy = appSettingsStorage.browserIconInLauncher,
            describe = {
                if (it) {
                    Res.string.enabled
                } else {
                    Res.string.disabled
                }.asStringSource()
            }
        )
    }
}
