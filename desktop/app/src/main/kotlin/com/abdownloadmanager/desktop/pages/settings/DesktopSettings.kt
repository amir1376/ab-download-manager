package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.ui.configurable.platform.item.FontConfigurable
import com.abdownloadmanager.desktop.utils.net.NetworkInterfaceProvider
import com.abdownloadmanager.desktop.utils.renderapi.CustomRenderApi
import com.abdownloadmanager.desktop.utils.renderapi.RenderApi
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.NetworkInterfacesConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import com.abdownloadmanager.shared.util.proxy.ProxyMode
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
import kotlinx.coroutines.CoroutineScope

object DesktopSettings {
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

    fun defaultNetworkInterfacesConfig(
        appSettings: AppSettingsStorage,
        networkInterfaceProvider: NetworkInterfaceProvider,
    ): NetworkInterfacesConfigurable {
        val parse: (String) -> List<String> = { raw ->
            raw.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }
        }
        val flow = appSettings.defaultNetworkInterfaces.mapTwoWayStateFlow(
            map = { parse(it) },
            unMap = { it.joinToString(", ") },
        )
        return NetworkInterfacesConfigurable(
            title = Res.string.settings_network_default_interfaces.asStringSource(),
            description = Res.string.settings_network_default_interfaces_description.asStringSource(),
            availableOptions = networkInterfaceProvider.discoverInterfaces(),
            backedBy = flow,
            describe = {
                if (it.isEmpty()) {
                    Res.string.queue_network_interface_default.asStringSource()
                } else {
                    Res.string.queue_network_interface_selected.asStringSourceWithARgs(
                        Res.string.queue_network_interface_selected_createArgs(count = it.size.toString())
                    )
                }
            },
        )
    }
}
