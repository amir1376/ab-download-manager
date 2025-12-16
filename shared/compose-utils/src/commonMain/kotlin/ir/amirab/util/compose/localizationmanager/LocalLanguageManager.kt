package ir.amirab.util.compose.localizationmanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val LocalLanguageManager = staticCompositionLocalOf<LanguageManager> {
    error("LocalLanguageManager not provided")
}
val LocaleLanguageDirection = staticCompositionLocalOf<LayoutDirection> {
    error("LocaleLanguageDirection not provided")
}

@Composable
fun WithLanguageDirection(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LocaleLanguageDirection.current,
    ) {
        content()
    }
}