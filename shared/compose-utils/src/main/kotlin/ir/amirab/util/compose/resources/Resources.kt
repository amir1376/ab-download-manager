package ir.amirab.util.compose.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ir.amirab.util.compose.localizationmanager.LocalLanguageManager
import ir.amirab.util.compose.localizationmanager.withReplacedArgs

typealias MyStringResource = ir.amirab.resources.contracts.MyStringResource

@Composable
fun myStringResource(key: MyStringResource): String {
    val languageManager = LocalLanguageManager.current
    val language by languageManager.selectedLanguage.collectAsState()
    return remember(language, key) {
        languageManager.getMessage(key.id)
    }
}

@Composable
fun myStringResource(key: MyStringResource, args: Map<String, String>): String {
    val languageManager = LocalLanguageManager.current
    val language by languageManager.selectedLanguage.collectAsState()
    return remember(language, key, args) {
        languageManager
            .getMessage(key.id)
            .withReplacedArgs(args)
    }
}