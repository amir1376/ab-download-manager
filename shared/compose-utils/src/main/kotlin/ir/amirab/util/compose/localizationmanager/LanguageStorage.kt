package ir.amirab.util.compose.localizationmanager

import kotlinx.coroutines.flow.MutableStateFlow

interface LanguageStorage {
    val selectedLanguage: MutableStateFlow<String>
}