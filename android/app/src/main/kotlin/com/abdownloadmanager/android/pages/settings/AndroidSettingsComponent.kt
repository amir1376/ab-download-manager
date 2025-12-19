package com.abdownloadmanager.android.pages.settings

import android.content.Context
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.android.util.pagemanager.PermissionsPageManager
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.settings.BaseSettingsComponent
import com.abdownloadmanager.shared.settings.CommonSettings
import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class AndroidSettingsComponent(
    ctx: ComponentContext,
    perHostSettingsPageManager: PerHostSettingsPageManager,
    permissionsPageManager: PermissionsPageManager,
) : BaseSettingsComponent(
    ctx
), KoinComponent {
    private val context by inject<Context>()
    private val appSettings by inject<AppSettingsStorage>()
    //    private val pageStorage by inject<PageStatesStorage>()
    private val appRepository by inject<BaseAppRepository>()
    private val proxyManager by inject<ProxyManager>()
    private val themeManager by inject<ThemeManager>()
    private val languageManager by inject<LanguageManager>()
    override val configurables: StateFlow<List<ConfigurableGroup>> = MutableStateFlow(
        listOf(
            ConfigurableGroup(
                mainConfigurable = CommonSettings.themeConfig(themeManager, scope),
                nestedVisible = themeManager.currentThemeInfo.mapStateFlow {
                    it.id == ThemeManager.systemThemeInfo.id
                },
                nestedConfigurable = listOfNotNull(
                    CommonSettings.defaultDarkThemeConfig(themeManager, scope),
                    CommonSettings.defaultLightThemeConfig(themeManager, scope),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.languageConfig(languageManager, scope),
//                            DesktopSettings.fontConfig(fontManager, scope),
                    CommonSettings.uiScaleConfig(appSettings),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOfNotNull(
//                            DesktopSettings.useNativeMenuBarConfig(appSettings),
//                            DesktopSettings.mergeTopBarWithTitleBarConfig(appSettings),
//                    CommonSettings.showIconLabels(appSettings),
                    CommonSettings.useRelativeDateTime(appSettings),
                    CommonSettings.playSoundNotification(appSettings),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.autoStartConfig(appSettings),
//                            DesktopSettings.useSystemTray(appSettings),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.sizeUnit(appRepository, scope),
                    CommonSettings.speedUnit(appRepository, scope),
                    CommonSettings.useAverageSpeedConfig(appRepository),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.autoShowDownloadProgressWindow(appSettings),
                    CommonSettings.showDownloadFinishWindow(appSettings),
                )
            ),
            // download engine

            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.defaultDownloadFolderConfig(appSettings),
                    CommonSettings.useCategoryByDefault(appSettings),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.speedLimitConfig(appRepository),
                    CommonSettings.threadCountConfig(appRepository),
                    CommonSettings.maxDownloadRetryCount(appRepository),
                    CommonSettings.dynamicPartDownloadConfig(appRepository),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.perHostSettings(perHostSettingsPageManager),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.proxyConfig(proxyManager),
                    CommonSettings.userAgent(appSettings),
                    CommonSettings.ignoreSSLCertificates(appSettings),
                    CommonSettings.useServerLastModified(appRepository),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    CommonSettings.trackDeletedFilesOnDisk(appRepository),
                    CommonSettings.appendExtensionToIncompleteDownloads(appRepository),
                    CommonSettings.deletePartialFileOnDownloadCancellation(appSettings),
                    CommonSettings.useSparseFileAllocation(appRepository),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    AndroidSettings.permissionSettings(permissionsPageManager),
                    AndroidSettings.ignoreBatteryOptimizations(context),
                )
            ),

            // browser integration
            // disabled for now
//            ConfigurableGroup(
//                nestedConfigurable = listOf(
//                    CommonSettings.browserIntegrationEnabled(appRepository),
//                    CommonSettings.browserIntegrationPort(appRepository)
//                )
//            )
        )
    )

}
