package com.abdownloadmanager.shared.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.abdownloadmanager.shared.utils.ui.LocalTitleBarDirection
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.compose.localizationmanager.LocalLanguageManager
import ir.amirab.util.compose.localizationmanager.LocaleLanguageDirection

@Composable
fun ProvideLanguageManager(
    languageManager: LanguageManager,
    content: @Composable () -> Unit,
) {
    val isRtl = languageManager.isRtl.collectAsState().value
    val languageDirection = if (isRtl) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    CompositionLocalProvider(
        LocalLanguageManager provides languageManager,
        LocalLayoutDirection provides languageDirection,
        LocalTitleBarDirection provides LayoutDirection.Ltr,
        LocaleLanguageDirection provides languageDirection
    ) {
        content()
    }
}