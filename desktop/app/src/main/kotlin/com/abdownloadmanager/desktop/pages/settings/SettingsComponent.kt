package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.pages.settings.SettingSections.*
import com.abdownloadmanager.desktop.pages.settings.configurable.*
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.BaseComponent
import com.abdownloadmanager.desktop.utils.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import androidx.compose.runtime.*
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.proxy.ProxyManager
import com.abdownloadmanager.utils.proxy.ProxyMode
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.compose.localizationmanager.LanguageInfo
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.ConvertSizeConfig
import ir.amirab.util.osfileutil.FileUtils
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class SettingSections(
    val icon: IconSource,
    val name: StringSource,
) {
    data object Appearance : SettingSections(MyIcons.appearance, Res.string.appearance.asStringSource())

    //    TODO ADD Network section (proxy , etc..)
    //    data object Network : SettingSections(MyIcons.network, "Network")
    data object DownloadEngine : SettingSections(MyIcons.downloadEngine, Res.string.download_engine.asStringSource())
    data object BrowserIntegration : SettingSections(MyIcons.network, Res.string.browser_integration.asStringSource())
}

interface SettingSectionGetter {
    operator fun get(key: SettingSections): List<Configurable<*>>
}

fun threadCountConfig(appRepository: AppRepository): IntConfigurable {
    return IntConfigurable(
        title = Res.string.settings_download_thread_count.asStringSource(),
        description = Res.string.settings_download_thread_count_description.asStringSource(),
        backedBy = appRepository.threadCount,
        range = 1..32,
        renderMode = IntConfigurable.RenderMode.TextField,
        describe = {
            Res.string.settings_download_thread_count_describe
                .asStringSourceWithARgs(
                    Res.string.settings_download_thread_count_describe_createArgs(
                        count = it.toString()
                    )
                )
        },
    )
}

fun dynamicPartDownloadConfig(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_dynamic_part_creation.asStringSource(),
        description = Res.string.settings_dynamic_part_creation_description.asStringSource(),
        backedBy = appRepository.dynamicPartCreation,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        },
    )
}

fun useServerLastModified(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_use_server_last_modified_time.asStringSource(),
        description = Res.string.settings_use_server_last_modified_time_description.asStringSource(),
        backedBy = appRepository.useServerLastModifiedTime,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        },
    )
}

fun useSparseFileAllocation(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_use_sparse_file_allocation.asStringSource(),
        description = Res.string.settings_use_sparse_file_allocation_description.asStringSource(),
        backedBy = appRepository.useSparseFileAllocation,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        },
    )
}

fun trackDeletedFilesOnDisk(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_track_deleted_files_on_disk.asStringSource(),
        description = Res.string.settings_track_deleted_files_on_disk_description.asStringSource(),
        backedBy = appRepository.trackDeletedFilesOnDisk,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        },
    )
}

fun speedUnit(appRepository: AppRepository, scope: CoroutineScope): EnumConfigurable<ConvertSizeConfig> {
    return EnumConfigurable(
        title = Res.string.settings_download_speed_unit.asStringSource(),
        description = Res.string.settings_download_speed_unit_description.asStringSource(),
        backedBy = createMutableStateFlowFromStateFlow(
            appRepository.speedUnit,
            updater = { appRepository.setSpeedUnit(it) },
            scope = scope
        ),
        possibleValues = listOf(
            CommonSizeConvertConfigs.BinaryBytes,
            CommonSizeConvertConfigs.BinaryBits,
        ),
        describe = {
            val u = it.baseSize.longString()
            "$u/s".asStringSource()
        },
    )
}

fun showDownloadFinishWindow(settingsStorage: AppSettingsStorage): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_show_completion_dialog.asStringSource(),
        description = Res.string.settings_show_completion_dialog_description.asStringSource(),
        backedBy = settingsStorage.showDownloadCompletionDialog,
        describe = {
            (if (it) Res.string.enabled else Res.string.disabled).asStringSource()
        },
    )
}

fun autoShowDownloadProgressWindow(settingsStorage: AppSettingsStorage): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_show_download_progress_dialog.asStringSource(),
        description = Res.string.settings_show_download_progress_dialog_description.asStringSource(),
        backedBy = settingsStorage.showDownloadProgressDialog,
        describe = {
            (if (it) Res.string.enabled else Res.string.disabled).asStringSource()
        },
    )
}

fun speedLimitConfig(appRepository: AppRepository): SpeedLimitConfigurable {
    return SpeedLimitConfigurable(
        title = Res.string.settings_global_speed_limiter.asStringSource(),
        description = Res.string.settings_global_speed_limiter_description.asStringSource(),
        backedBy = appRepository.speedLimiter,
        describe = {
            if (it == 0L) {
                Res.string.unlimited.asStringSource()
            } else {
                convertPositiveSpeedToHumanReadable(it, appRepository.speedUnit.value).asStringSource()
            }
        }
    )
}

fun useAverageSpeedConfig(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_show_average_speed.asStringSource(),
        description = Res.string.settings_show_average_speed_description.asStringSource(),
        backedBy = appRepository.useAverageSpeed,
        describe = {
            if (it) Res.string.average_speed.asStringSource()
            else Res.string.exact_speed.asStringSource()
        }
    )
}

fun defaultDownloadFolderConfig(appSettings: AppSettingsStorage): FolderConfigurable {
    return FolderConfigurable(
        title = Res.string.settings_default_download_folder.asStringSource(),
        description = Res.string.settings_default_download_folder_description.asStringSource(),
        backedBy = appSettings.defaultDownloadFolder,
        validate = {
            FileUtils.canWriteInThisFolder(it)
        },
        describe = {
            Res.string
                .settings_default_download_folder_describe
                .asStringSourceWithARgs(
                    Res.string.settings_default_download_folder_describe_createArgs(
                        folder = it
                    )
                )
        }
    )
}

fun proxyConfig(proxyManager: ProxyManager, scope: CoroutineScope): ProxyConfigurable {
    return ProxyConfigurable(
        title = Res.string.settings_use_proxy.asStringSource(),
        description = Res.string.settings_use_proxy_description.asStringSource(),
        backedBy = proxyManager.proxyData,

        validate = {
            true
        },
        describe = {
            when (it.proxyMode) {
                ProxyMode.Direct -> Res.string.settings_use_proxy_describe_no_proxy.asStringSource()
                ProxyMode.UseSystem -> Res.string.settings_use_proxy_describe_system_proxy.asStringSource()
                ProxyMode.Manual -> Res.string.settings_use_proxy_describe_manual_proxy
                    .asStringSourceWithARgs(
                        Res.string.settings_use_proxy_describe_manual_proxy_createArgs(
                            value = it.proxyWithRules.proxy.run { "$type $host:$port" }
                        )
                    )
            }
        }
    )
}

fun uiScaleConfig(appSettings: AppSettingsStorage): EnumConfigurable<Float?> {
    return EnumConfigurable(
        title = Res.string.settings_ui_scale.asStringSource(),
        description = Res.string.settings_ui_scale_description.asStringSource(),
        backedBy = appSettings.uiScale,
        possibleValues = listOf(
            null,
            0.8f,
            0.9f,
            1f,
            1.1f,
            1.25f,
            1.5f,
            1.75f,
            2f,
            2.25f,
            2.5f,
            2.75f,
            3f,
        ),
        renderMode = EnumConfigurable.RenderMode.Spinner,
        describe = {
            if (it == null) {
                Res.string.system.asStringSource()
            } else {
                "$it x".asStringSource()
            }
        }
    )
}

fun themeConfig(
    themeManager: ThemeManager,
    scope: CoroutineScope,
): ThemeConfigurable {
    val currentThemeName = themeManager.currentThemeInfo
    val themes = themeManager.possibleThemesToSelect
    return ThemeConfigurable(
        title = Res.string.settings_theme.asStringSource(),
        description = Res.string.settings_theme_description.asStringSource(),
        backedBy = createMutableStateFlowFromStateFlow(
            flow = currentThemeName,
            updater = {
                themeManager.setTheme(it.id)
            },
            scope = scope,
        ),
        possibleValues = themes.value,
        describe = {
            it.name
        },
    )
}

fun languageConfig(
    languageManager: LanguageManager,
    scope: CoroutineScope,
): EnumConfigurable<LanguageInfo> {
    val currentLanguageName = languageManager.selectedLanguage
    val allLanguages = languageManager.languageList
    return EnumConfigurable(
        title = Res.string.settings_language.asStringSource(),
        description = "".asStringSource(),
        backedBy = createMutableStateFlowFromStateFlow(
            flow = currentLanguageName.mapStateFlow { l ->
                allLanguages.value.find {
                    it.toLocaleString() == l
                } ?: LanguageManager.DefaultLanguageInfo
            },
            updater = { languageInfo ->
                languageManager.selectLanguage(languageInfo)
            },
            scope = scope,
        ),
        possibleValues = allLanguages.value,
        describe = {
            it.nativeName.asStringSource()
        },
    )
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

fun autoStartConfig(appSettings: AppSettingsStorage): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_start_on_boot.asStringSource(),
        description = Res.string.settings_start_on_boot_description.asStringSource(),
        backedBy = appSettings.autoStartOnBoot,
        renderMode = BooleanConfigurable.RenderMode.Switch,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        }
    )
}

fun playSoundNotification(appSettings: AppSettingsStorage): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_notification_sound.asStringSource(),
        description = Res.string.settings_notification_sound_description.asStringSource(),
        backedBy = appSettings.notificationSound,
        renderMode = BooleanConfigurable.RenderMode.Switch,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        }
    )
}

fun browserIntegrationEnabled(appRepository: AppRepository): BooleanConfigurable {
    return BooleanConfigurable(
        title = Res.string.settings_browser_integration.asStringSource(),
        description = Res.string.settings_browser_integration_description.asStringSource(),
        backedBy = appRepository.integrationEnabled,
        renderMode = BooleanConfigurable.RenderMode.Switch,
        describe = {
            if (it) {
                Res.string.enabled.asStringSource()
            } else {
                Res.string.disabled.asStringSource()
            }
        }
    )
}

fun browserIntegrationPort(appRepository: AppRepository): IntConfigurable {
    return IntConfigurable(
        title = Res.string.settings_browser_integration_server_port.asStringSource(),
        description = Res.string.settings_browser_integration_server_port_description.asStringSource(),
        backedBy = appRepository.integrationPort,
        describe = {
            Res.string.settings_browser_integration_server_port_describe
                .asStringSourceWithARgs(
                    Res.string.settings_browser_integration_server_port_describe_createArgs(
                        port = it.toString()
                    )
                )
        },
        range = 0..65000,
    )
}

sealed class SettingPageEffects {
    data object BringToFront : SettingPageEffects()
}

class SettingsComponent(
    ctx: ComponentContext,
) : BaseComponent(ctx),
    KoinComponent,
    ContainsEffects<SettingPageEffects> by supportEffects() {
    val appSettings by inject<AppSettingsStorage>()
    val appRepository by inject<AppRepository>()
    val proxyManager by inject<ProxyManager>()
    val themeManager by inject<ThemeManager>()
    val languageManager by inject<LanguageManager>()
    val allConfigs = object : SettingSectionGetter {
        override operator fun get(key: SettingSections): List<Configurable<*>> {
            return when (key) {
                Appearance -> listOf(
                    themeConfig(themeManager, scope),
                    languageConfig(languageManager, scope),
                    uiScaleConfig(appSettings),
                    autoStartConfig(appSettings),
                    mergeTopBarWithTitleBarConfig(appSettings),
                    speedUnit(appRepository, scope),
                    playSoundNotification(appSettings),
                )

//                Network -> listOf()
                BrowserIntegration -> listOf(
                    browserIntegrationEnabled(appRepository),
                    browserIntegrationPort(appRepository)
                )

                DownloadEngine -> listOf(
                    defaultDownloadFolderConfig(appSettings),
                    proxyConfig(proxyManager, scope),
                    useAverageSpeedConfig(appRepository),
                    speedLimitConfig(appRepository),
                    threadCountConfig(appRepository),
                    dynamicPartDownloadConfig(appRepository),
                    autoShowDownloadProgressWindow(appSettings),
                    showDownloadFinishWindow(appSettings),
                    useServerLastModified(appRepository),
                    useSparseFileAllocation(appRepository),
                    trackDeletedFilesOnDisk(appRepository),
                )
            }
        }
    }

    fun toFront() {
        sendEffect(SettingPageEffects.BringToFront)
    }

    var pages = listOf(
        Appearance,
//        Network,
        DownloadEngine,
        BrowserIntegration,
    )
    var currentPage: SettingSections by mutableStateOf(Appearance)
    val configurables by derivedStateOf {
        allConfigs[currentPage]
    }
}