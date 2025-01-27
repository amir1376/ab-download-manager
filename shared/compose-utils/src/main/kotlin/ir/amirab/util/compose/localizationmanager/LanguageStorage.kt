package ir.amirab.util.compose.localizationmanager

import kotlinx.coroutines.flow.MutableStateFlow

interface LanguageStorage {
    // null means auto
    val selectedLanguage: MutableStateFlow<String?>
}
