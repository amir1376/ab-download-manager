package com.abdownloadmanager.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.abdownloadmanager.android.ui.configurable.comon.CommonConfigurableRenderersForAndroid
import com.abdownloadmanager.android.ui.configurable.comon.ConfigurableRenderersForAndroid
import com.abdownloadmanager.android.util.AppInfo
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.ui.ProvideCommonSettings
import com.abdownloadmanager.shared.ui.ProvideSizeUnits
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRendererRegistry
import com.abdownloadmanager.shared.ui.theme.ABDownloaderTheme
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.shared.ui.widget.NotificationManager
import com.abdownloadmanager.shared.ui.widget.ProvideLanguageManager
import com.abdownloadmanager.shared.ui.widget.ProvideNotificationManager
import com.abdownloadmanager.shared.util.PopUpContainer
import com.abdownloadmanager.shared.util.ResponsiveBox
import com.abdownloadmanager.shared.util.ui.ProvideDebugInfo
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.localizationmanager.LanguageManager
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun ABDownloadManagerApplicationContent(
    languageManager: LanguageManager,
    themeManager: ThemeManager,
    appSettingsStorage: BaseAppSettingsStorage,
    iconResolver: IIconResolver,
    appRepository: BaseAppRepository,
    notificationManager: NotificationManager,
    content: @Composable () -> Unit,
) {
    val configurableRendererRegistry = remember {
        ConfigurableRendererRegistry {
            listOf(
                CommonConfigurableRenderersForAndroid,
                ConfigurableRenderersForAndroid
            ).forEach {
                it.getAllRenderers().forEach { (key, renderer) ->
                    this.register(key, renderer)
                }
            }
        }
    }
    ProvideDebugInfo(AppInfo.isInDebugMode) {
        ProvideLanguageManager(languageManager) {
            ProvideCommonSettings(
                appSettings = appSettingsStorage,
                iconProvider = iconResolver,
                configurableRendererRegistry = configurableRendererRegistry,
            ) {
                ProvideNotificationManager(notificationManager) {
                    val myColors by themeManager.currentThemeColor.collectAsState()
                    val uiScale by appSettingsStorage.uiScale.collectAsState()
                    ABDownloaderTheme(
                        myColors = myColors,
                        fontFamily = null,
                        uiScale = uiScale,
                    ) {
                        ResponsiveBox {
                            ProvideSizeUnits(
                                appRepository
                            ) {
                                PopUpContainer {
                                    content()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
