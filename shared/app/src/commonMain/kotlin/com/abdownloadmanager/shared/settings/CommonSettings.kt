package com.abdownloadmanager.shared.settings

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.EnumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.FolderConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.IntConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.NavigatableConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ProxyConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.ThemeConfigurable
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.shared.util.MaximumDownloadRetriesLimitation
import com.abdownloadmanager.shared.util.ThreadCountLimitation
import com.abdownloadmanager.shared.util.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import com.abdownloadmanager.shared.util.proxy.ProxyMode
import com.abdownloadmanager.shared.util.ui.theme.DEFAULT_UI_SCALE
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.compose.combineStringSources
import ir.amirab.util.compose.localizationmanager.LanguageInfo
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.datasize.CommonSizeConvertConfigs
import ir.amirab.util.datasize.ConvertSizeConfig
import ir.amirab.util.datasize.SizeFactors
import ir.amirab.util.datasize.SizeUnit
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

object CommonSettings {

    fun threadCountConfig(appRepository: BaseAppRepository): IntConfigurable {
        return IntConfigurable(
            title = Res.string.settings_download_thread_count.asStringSource(),
            description = Res.string.settings_download_thread_count_description.asStringSource(),
            backedBy = appRepository.threadCount,
            range = 1..ThreadCountLimitation.MAX_ALLOWED_THREAD_COUNT,
            renderMode = IntConfigurable.RenderMode.TextField,
            describe = {
                buildList {
                    add(
                        Res.string.settings_download_thread_count_describe
                            .asStringSourceWithARgs(
                                Res.string.settings_download_thread_count_describe_createArgs(
                                    count = it.toString()
                                )
                            )
                    )
                    if (it > ThreadCountLimitation.MAX_NORMAL_VALUE) {
                        add(
                            Res.string.settings_download_thread_count_with_large_value_describe.asStringSource()
                        )
                    }
                }.combineStringSources("\n")
            },
        )
    }
    fun maxConcurrentDownloads(appRepository: BaseAppRepository): IntConfigurable {
        return IntConfigurable(
            title = Res.string.settings_download_max_concurrent_downloads.asStringSource(),
            description = Res.string.settings_download_max_concurrent_downloads_description.asStringSource(),
            backedBy = appRepository.maxConcurrentDownloads,
            range = 0..Int.MAX_VALUE,
            renderMode = IntConfigurable.RenderMode.TextField,
            describe = {
                if (it == 0) {
                    Res.string.unlimited.asStringSource()
                } else {
                    "$it".asStringSource()
                }
            },
        )
    }

    fun maxDownloadRetryCount(appRepository: BaseAppRepository): IntConfigurable {
        return IntConfigurable(
            title = Res.string.settings_download_max_retries_count.asStringSource(),
            description = Res.string.settings_download_max_retries_count_description.asStringSource(),
            backedBy = appRepository.maxDownloadRetryCount,
            range = 0..MaximumDownloadRetriesLimitation.MAX_ALLOWED_RETRIES,
            renderMode = IntConfigurable.RenderMode.TextField,
            describe = {
                if (it == 0) {
                    Res.string.settings_download_max_retries_count_describe_no_retries.asStringSource()
                } else {
                    Res.string.settings_download_max_retries_count_describe_n_retries
                        .asStringSourceWithARgs(
                            Res.string.settings_download_max_retries_count_describe_n_retries_createArgs(
                                count = "$it"
                            )
                        )
                }
            },
        )
    }

    fun dynamicPartDownloadConfig(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun useServerLastModified(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun appendExtensionToIncompleteDownloads(appRepository: BaseAppRepository): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_append_extension_to_incomplete_downloads.asStringSource(),
            description = Res.string.settings_append_extension_to_incomplete_downloads_description.asStringSource(),
            backedBy = appRepository.appendExtensionToIncompleteDownloads,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun useSparseFileAllocation(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun trackDeletedFilesOnDisk(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun deletePartialFileOnDownloadCancellation(appSettingsStorage: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_delete_partial_file_on_download_cancellation.asStringSource(),
            description = Res.string.settings_delete_partial_file_on_download_cancellation_description.asStringSource(),
            backedBy = appSettingsStorage.deletePartialFileOnDownloadCancellation,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun ignoreSSLCertificates(appSettingsStorage: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_ignore_ssl_certificates.asStringSource(),
            description = Res.string.settings_ignore_ssl_certificates_description.asStringSource(),
            backedBy = appSettingsStorage.ignoreSSLCertificates,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun userAgent(appSettingsStorage: BaseAppSettingsStorage): StringConfigurable {
        return StringConfigurable(
            title = Res.string.settings_default_user_agent.asStringSource(),
            description = Res.string.settings_default_user_agent_description.asStringSource(),
            backedBy = appSettingsStorage.userAgent,
            describe = {
                if (it.isBlank()) {
                    Res.string.disabled.asStringSource()
                } else {
                    "".asStringSource()
                }
            },
        )
    }

    fun useCategoryByDefault(appSettingsStorage: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_use_category_by_default.asStringSource(),
            description = Res.string.settings_use_category_by_default_description.asStringSource(),
            backedBy = appSettingsStorage.useCategoryByDefault,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun sizeUnit(
        appRepository: BaseAppRepository,
        scope: CoroutineScope
    ): EnumConfigurable<ConvertSizeConfig> {
        return EnumConfigurable(
            title = Res.string.settings_download_size_unit.asStringSource(),
            description = Res.string.settings_download_size_unit_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                appRepository.sizeUnit,
                updater = { appRepository.setSizeUnit(it) },
                scope = scope
            ),
            possibleValues = listOf(
                CommonSizeConvertConfigs.BinaryBytes,
                CommonSizeConvertConfigs.DecimalBytes,
            ),
            describe = {
                val sizeUnit = SizeUnit(
                    SizeFactors.FactorValue.Kilo,
                    it.baseSize,
                    it.factors,
                )
                "$sizeUnit".asStringSource()
            },
        )
    }

    fun speedUnit(
        appRepository: BaseAppRepository,
        scope: CoroutineScope
    ): EnumConfigurable<ConvertSizeConfig> {
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
                CommonSizeConvertConfigs.DecimalBytes,
                CommonSizeConvertConfigs.BinaryBits,
                CommonSizeConvertConfigs.DecimalBits,
            ),
            describe = {
                val sizeUnit = SizeUnit(
                    SizeFactors.FactorValue.Kilo,
                    it.baseSize,
                    it.factors,
                )
                val extraInfo = "${it.factors.baseValue} ${it.baseSize.longString()}/s"
                "${sizeUnit}/s ($extraInfo)".asStringSource()
            },
        )
    }

    fun showDownloadFinishWindow(settingsStorage: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_show_completion_dialog.asStringSource(),
            description = Res.string.settings_show_completion_dialog_description.asStringSource(),
            backedBy = settingsStorage.showDownloadCompletionDialog,
            describe = {
                (if (it) Res.string.enabled else Res.string.disabled).asStringSource()
            },
        )
    }

    fun autoShowDownloadProgressWindow(settingsStorage: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_show_download_progress_dialog.asStringSource(),
            description = Res.string.settings_show_download_progress_dialog_description.asStringSource(),
            backedBy = settingsStorage.showDownloadProgressDialog,
            describe = {
                (if (it) Res.string.enabled else Res.string.disabled).asStringSource()
            },
        )
    }

    fun perHostSettings(perHostSettingsPageManager: PerHostSettingsPageManager): NavigatableConfigurable {
        return NavigatableConfigurable(
            title = Res.string.settings_per_host_settings.asStringSource(),
            description = Res.string.settings_per_host_settings_descriptions.asStringSource(),
            onRequestNavigate = {
                perHostSettingsPageManager.openPerHostSettings(null)
            },
        )
    }

    fun speedLimitConfig(appRepository: BaseAppRepository): SpeedLimitConfigurable {
        return SpeedLimitConfigurable(
            title = Res.string.settings_global_speed_limiter.asStringSource(),
            description = Res.string.settings_global_speed_limiter_description.asStringSource(),
            backedBy = appRepository.speedLimiter,
            describe = {
                if (it == 0L) {
                    Res.string.unlimited.asStringSource()
                } else {
                    convertPositiveSpeedToHumanReadable(
                        it,
                        appRepository.speedUnit.value
                    ).asStringSource()
                }
            }
        )
    }

    fun useAverageSpeedConfig(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun defaultDownloadFolderConfig(appSettings: BaseAppSettingsStorage): FolderConfigurable {
        return FolderConfigurable(
            title = Res.string.settings_default_download_folder.asStringSource(),
            description = Res.string.settings_default_download_folder_description.asStringSource(),
            backedBy = appSettings.defaultDownloadFolder,
            validate = {
                FileUtils.Companion.canWriteInThisFolder(it)
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

    fun uiScaleConfig(appSettings: BaseAppSettingsStorage): EnumConfigurable<Float> {
        return EnumConfigurable(
            title = Res.string.settings_ui_scale.asStringSource(),
            description = Res.string.settings_ui_scale_description.asStringSource(),
            backedBy = appSettings.uiScale,
            possibleValues = listOf(
                0.8f,
                0.9f,
                1f,
                1.1f,
                1.25f,
                1.5f,
                1.75f,
                2f,
            ),
            renderMode = EnumConfigurable.RenderMode.Spinner,
            describe = {
                val percent = (it * 100).roundToInt()
                if (it == DEFAULT_UI_SCALE) {
                    StringSource.CombinedStringSource(
                        listOf(
                            Res.string.system.asStringSource(),
                            "($percent%)".asStringSource()
                        ),
                        " "
                    )
                } else {
                    "$percent%".asStringSource()
                }
            }
        )
    }

    fun themeConfig(
        themeManager: ThemeManager,
        scope: CoroutineScope,
    ): ThemeConfigurable {
        val currentThemeInfo = themeManager.currentThemeInfo
        val themes = themeManager.selectableThemes
        return ThemeConfigurable(
            title = Res.string.settings_theme.asStringSource(),
            description = Res.string.settings_theme_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = currentThemeInfo,
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

    fun defaultDarkThemeConfig(
        themeManager: ThemeManager,
        scope: CoroutineScope,
    ): ThemeConfigurable {
        val currentDefaultDarkThemeInfo = themeManager.selectedDarkThemeInfo
        val darkThemes = themeManager.selectableDarkThemes
        return ThemeConfigurable(
            title = Res.string.settings_default_dark_theme.asStringSource(),
            description = Res.string.settings_default_dark_theme_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = currentDefaultDarkThemeInfo,
                updater = {
                    themeManager.setDarkTheme(it.id)
                },
                scope = scope,
            ),
            possibleValues = darkThemes.value,
            describe = {
                it.name
            },
        )
    }

    fun defaultLightThemeConfig(
        themeManager: ThemeManager,
        scope: CoroutineScope,
    ): ThemeConfigurable {
        val currentDefaultLightThemeInfo = themeManager.selectedLightThemeInfo
        val lightThemes = themeManager.selectableLightThemes
        return ThemeConfigurable(
            title = Res.string.settings_default_light_theme.asStringSource(),
            description = Res.string.settings_default_light_theme_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = currentDefaultLightThemeInfo,
                updater = {
                    themeManager.setLightTheme(it.id)
                },
                scope = scope,
            ),
            possibleValues = lightThemes.value,
            describe = {
                it.name
            },
        )
    }

    fun languageConfig(
        languageManager: LanguageManager,
        scope: CoroutineScope,
    ): EnumConfigurable<LanguageInfo?> {
        val currentLanguageName = languageManager.selectedLanguageInStorage
        val allLanguages = languageManager.languageList.value
        return EnumConfigurable(
            title = Res.string.settings_language.asStringSource(),
            description = "".asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = currentLanguageName.mapStateFlow { language ->
                    language?.let {
                        allLanguages.find {
                            it.toLocaleString() == language
                        }
                    }
                },
                updater = { languageInfo ->
                    languageManager.selectLanguage(languageInfo)
                },
                scope = scope,
            ),
            valueToString = {
                if (it == null) {
                    emptyList()
                } else {
                    listOfNotNull(
                        it.nativeName,
                        it.locale.languageCode,
                        it.locale.countryCode,
                    )
                }
            },
            possibleValues = listOf(null).plus(allLanguages),
            describe = {
                val isAuto = it == null
                val language = it ?: languageManager.systemLanguageOrDefault
                val languageName = language.nativeName
                if (isAuto) {
                    // always use english here!
                    "System ($languageName)".asStringSource()
                } else {
                    languageName.asStringSource()
                }
            },
        )
    }

    fun showIconLabels(appSettings: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_show_icon_labels.asStringSource(),
            description = Res.string.settings_show_icon_labels_description.asStringSource(),
            backedBy = appSettings.showIconLabels,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }

    fun useRelativeDateTime(appSettings: BaseAppSettingsStorage): BooleanConfigurable {
        return BooleanConfigurable(
            title = Res.string.settings_use_relative_date_time.asStringSource(),
            description = Res.string.settings_use_relative_date_time_description.asStringSource(),
            backedBy = appSettings.useRelativeDateTime,
            describe = {
                if (it) {
                    Res.string.enabled.asStringSource()
                } else {
                    Res.string.disabled.asStringSource()
                }
            },
        )
    }


    fun autoStartConfig(appSettings: BaseAppSettingsStorage): BooleanConfigurable {
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

    fun playSoundNotification(appSettings: BaseAppSettingsStorage): BooleanConfigurable {
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

    fun browserIntegrationEnabled(appRepository: BaseAppRepository): BooleanConfigurable {
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

    fun browserIntegrationPort(appRepository: BaseAppRepository): IntConfigurable {
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
    fun proxyConfig(proxyManager: ProxyManager): ProxyConfigurable {
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

                    ProxyMode.Pac -> {
                        Res.string.settings_use_proxy_describe_pac_proxy
                            .asStringSourceWithARgs(
                                Res.string.settings_use_proxy_describe_pac_proxy_createArgs(
                                    value = it.pac.uri
                                )
                            )
                    }
                }
            }
        )
    }
}
