package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.ui.configurable.platform.item.FontConfigurable
import com.abdownloadmanager.desktop.utils.renderapi.CustomRenderApi
import com.abdownloadmanager.desktop.utils.renderapi.RenderApi
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.util.asToggledStringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.createMutableStateFlowFromFlow
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine

enum class AppIconMode {
    SystemTrayOnly,
    DockIconOnly,
    Both,
}

object DesktopSettings {
    fun mergeTopBarWithTitleBarConfig(appSettings: AppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_compact_top_bar.asStringSource(),
            description = Res.string.settings_compact_top_bar_description.asStringSource(),
            backedBy = appSettings.mergeTopBarWithTitleBar,
            describe = {
                it.asToggledStringSource()
            },
        )
    }

    fun useNativeMenuBarConfig(appSettings: AppSettingsStorage): BooleanConfigurable? {
        if (Platform.isMac().not()) return null
        return BooleanConfigurable(
            title = Res.string.settings_use_native_menu_bar.asStringSource(),
            description = Res.string.settings_use_native_menu_bar_description.asStringSource(),
            backedBy = appSettings.useNativeMenuBar,
            describe = {
                it.asToggledStringSource()
            },
        )
    }

    fun useSystemTray(appSettings: AppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_use_system_tray.asStringSource(),
            description = Res.string.settings_use_system_tray_description.asStringSource(),
            backedBy = appSettings.useSystemTray,
            describe = {
                it.asToggledStringSource()
            },
        )
    }

    fun appIcon(appSettings: AppSettingsStorage, scope: CoroutineScope): EnumConfigurable<AppIconMode> {
        fun modeOf(useSystemTray: Boolean, showDockIcon: Boolean?) = when {
            !useSystemTray -> AppIconMode.DockIconOnly
            showDockIcon == true -> AppIconMode.Both
            else -> AppIconMode.SystemTrayOnly
        }

        return EnumConfigurable(
            title = Res.string.settings_app_icon.asStringSource(),
            description = Res.string.settings_app_icon_description.asStringSource(),
            backedBy = createMutableStateFlowFromFlow(
                flow = combine(appSettings.useSystemTray, appSettings.showDockIcon) { useSystemTray, showDockIcon ->
                    modeOf(useSystemTray, showDockIcon)
                },
                initialValue = modeOf(appSettings.useSystemTray.value, appSettings.showDockIcon.value),
                updater = { mode ->
                    appSettings.useSystemTray.value = mode != AppIconMode.DockIconOnly
                    appSettings.showDockIcon.value = mode != AppIconMode.SystemTrayOnly
                },
                scope = scope,
            ),
            possibleValues = AppIconMode.entries,
            itemTitle = {
                when (it) {
                    AppIconMode.SystemTrayOnly -> Res.string.settings_app_icon_system_tray_only
                    AppIconMode.DockIconOnly -> Res.string.settings_app_icon_dock_icon_only
                    AppIconMode.Both -> Res.string.settings_app_icon_both
                }.asStringSource()
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
            itemTitle = {
                it?.prettyName?.asStringSource()?: Res.string.default.asStringSource()
            }
        )
    }
}
