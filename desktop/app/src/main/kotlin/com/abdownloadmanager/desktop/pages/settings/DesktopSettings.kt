package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.ui.configurable.platform.item.FontConfigurable
import com.abdownloadmanager.desktop.utils.NotificationSoundEvent
import com.abdownloadmanager.desktop.utils.NotificationSoundPlayer
import com.abdownloadmanager.desktop.utils.renderapi.CustomRenderApi
import com.abdownloadmanager.desktop.utils.renderapi.RenderApi
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.FileConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import com.abdownloadmanager.shared.util.proxy.ProxyMode
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
import kotlinx.coroutines.CoroutineScope
import java.io.File

object DesktopSettings {
    private fun isAllowedSoundPath(path: String): Boolean {
        return path.isBlank() || path.endsWith(".wav", ignoreCase = true)
    }

    private fun describeSoundPath(path: String) =
        if (path.isBlank()) {
            Res.string.settings_notification_sound_default.asStringSource()
        } else {
            File(path).name.ifBlank { path }.asStringSource()
        }

    fun mergeTopBarWithTitleBarConfig(appSettings: AppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_compact_top_bar.asStringSource(),
            description = Res.string.settings_compact_top_bar_description.asStringSource(),
            backedBy = appSettings.mergeTopBarWithTitleBar,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun useNativeMenuBarConfig(appSettings: AppSettingsStorage): BooleanConfigurable? {
        if (Platform.Companion.isMac().not()) return null
        return BooleanConfigurable(
            title = Res.string.settings_use_native_menu_bar.asStringSource(),
            description = Res.string.settings_use_native_menu_bar_description.asStringSource(),
            backedBy = appSettings.useNativeMenuBar,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun useSystemTray(appSettings: AppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_use_system_tray.asStringSource(),
            description = Res.string.settings_use_system_tray_description.asStringSource(),
            backedBy = appSettings.useSystemTray,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun fontConfig(
        fontManager: FontManager,
        scope: CoroutineScope,
    ): FontConfigurable {
        return FontConfigurable(
            title = Res.string.settings_font.asStringSource(),
            description = Res.string.settings_font_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = fontManager.currentFontInfo,
                updater = { font ->
                    fontManager.setFont(font.id)
                },
                scope = scope,
            ),
            possibleValues = fontManager.selectableFonts.value,
            describe = {
                it.name
            }
        )
    }

    fun renderApi(
        customRenderApi: CustomRenderApi,
    ): EnumConfigurable<RenderApi?> {
        return EnumConfigurable(
            title = "Render API".asStringSource(),
            description = "Configures the Render API backend used by the application. A restart is required for the change to take effect.".asStringSource(),
            backedBy = customRenderApi.data,
            possibleValues = buildList {
                add(null)
                addAll(customRenderApi.getSupportedRenderApiForThisPlatform())
            },
            describe = {
                it?.prettyName?.asStringSource()?: Res.string.default.asStringSource()
            }
        )
    }

    fun downloadCompletedSound(appSettings: AppSettingsStorage): FileConfigurable {
        return FileConfigurable(
            title = Res.string.settings_download_completed_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.downloadCompletedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
            onPreview = { NotificationSoundPlayer().preview(NotificationSoundEvent.DownloadCompleted) },
        )
    }

    fun downloadErrorSound(appSettings: AppSettingsStorage): FileConfigurable {
        return FileConfigurable(
            title = Res.string.settings_download_error_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.downloadErrorSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
            onPreview = { NotificationSoundPlayer().preview(NotificationSoundEvent.DownloadError) },
        )
    }

    fun queueStartedSound(appSettings: AppSettingsStorage): FileConfigurable {
        return FileConfigurable(
            title = Res.string.settings_queue_started_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.queueStartedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
            onPreview = { NotificationSoundPlayer().preview(NotificationSoundEvent.QueueStarted) },
        )
    }

    fun queueEndedSound(appSettings: AppSettingsStorage): FileConfigurable {
        return FileConfigurable(
            title = Res.string.settings_queue_ended_sound.asStringSource(),
            description = Res.string.settings_notification_sound_file_description.asStringSource(),
            backedBy = appSettings.queueEndedSoundPath,
            validate = ::isAllowedSoundPath,
            describe = ::describeSoundPath,
            onPreview = { NotificationSoundPlayer().preview(NotificationSoundEvent.QueueEnded) },
        )
    }
}
