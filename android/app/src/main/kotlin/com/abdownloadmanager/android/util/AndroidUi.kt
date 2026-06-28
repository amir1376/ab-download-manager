package com.abdownloadmanager.android.util

import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.xeton.util.compose.localizationmanager.LanguageManager
import com.xeton.util.guardedEntry
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AndroidUi : KoinComponent {
    val themeManager: ThemeManager by inject()
    val languageManager: LanguageManager by inject()
    private var booted = guardedEntry()
    fun boot() {
        booted.action {
            themeManager.boot()
            languageManager.boot()
        }
    }
}
