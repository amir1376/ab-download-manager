package com.abdownloadmanager.android.pages.onboarding.initialsetup

import com.abdownloadmanager.shared.settings.CommonSettings
import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.localizationmanager.LanguageManager

class InitialSetupComponent(
    ctx: ComponentContext,
    private val languageManager: LanguageManager,
    private val themeManager: ThemeManager,
    private val onFinish: () -> Unit
) : BaseComponent(ctx) {
    val configurables = listOf(
            CommonSettings.languageConfig(languageManager, scope),
            CommonSettings.themeConfig(themeManager, scope),
        )

    fun onUserPressFinish() {
        onFinish()
    }
}
