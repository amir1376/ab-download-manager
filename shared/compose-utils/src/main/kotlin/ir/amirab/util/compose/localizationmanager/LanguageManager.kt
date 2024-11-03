package ir.amirab.util.compose.localizationmanager

import androidx.compose.runtime.Immutable
import ir.amirab.util.compose.contants.FILE_PROTOCOL
import ir.amirab.util.compose.contants.RESOURCE_PROTOCOL
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import java.io.InputStream
import java.net.URI
import java.util.Locale
import java.util.Properties

class LanguageManager(
    private val storage: LanguageStorage,
) {
    private val _languageList: MutableStateFlow<List<LanguageInfo>> = MutableStateFlow(emptyList())
    val languageList = _languageList.asStateFlow()
    val selectedLanguage = storage.selectedLanguage
    val isRtl = selectedLanguage.mapStateFlow {
        rtlLanguages.contains(it)
    }

    fun boot() {
        _languageList.value = getAvailableLanguages()
        instance = this
    }

    fun selectLanguage(code: String?) {
        val languageCode = code ?: Locale.getDefault().language
        val languageInfo = languageList.value.find { it.languageCode == languageCode }
        selectedLanguage.value = (languageInfo ?: DefaultLanguageInfo).languageCode
    }

    fun getMessage(key: String): String {
        return getMessageContainer().getMessage(key)
            ?: defaultLanguageData.value.getMessage(key)
            ?: key
    }

    private fun getRequestedLanguage(): String {
        return selectedLanguage.value
    }

    @Volatile
    private var loadedLanguage: LoadedLanguage? = null

    private val defaultLanguageData = lazy {
        createMessageContainer(DefaultLanguageInfo)
    }

    private fun createMessageContainer(
        languageInfo: LanguageInfo,
    ): MessageData {
        return when {
            languageInfo == DefaultLanguageInfo && defaultLanguageData.isInitialized() -> defaultLanguageData.value
            else -> PropertiesMessageContainer(
                Properties().apply {
                    kotlin.runCatching {
                        openStream(languageInfo.path)
                            .reader(Charsets.UTF_8)
                            .use {
                                load(it)
                            }
                    }.onFailure {
                        println("Error while loading language data!")
                        it.printStackTrace()
                    }
                }
            )
        }
    }

    private fun bestLanguageInfo(code: String): LanguageInfo {
        return languageList.value.find {
            it.languageCode == code
        } ?: DefaultLanguageInfo
    }

    private fun getMessageContainer(): MessageData {
        val requestedLanguage = getRequestedLanguage()
        this.loadedLanguage.let { loadedLanguage ->
            if (loadedLanguage != null && loadedLanguage.languageInfo.languageCode == requestedLanguage) {
                return loadedLanguage.messageData
            }
        }
        synchronized(this) {
            // make sure not created earlier
            this.loadedLanguage.let { loadedLanguage ->
                if (loadedLanguage != null && loadedLanguage.languageInfo.languageCode == requestedLanguage) {
                    return loadedLanguage.messageData
                }
            }
            val languageInfo = bestLanguageInfo(requestedLanguage)
            val created = LoadedLanguage(
                languageInfo,
                createMessageContainer(languageInfo)
            )
            this.loadedLanguage = created
            return created.messageData
        }
    }

    private fun getAvailableLanguages(): List<LanguageInfo> {
        val fileSystem = FileSystem.RESOURCES
        return fileSystem
            .list(LOCALES_PATH.toPath())
            .mapNotNull { path ->
                kotlin.runCatching {
                    if (fileSystem.metadataOrNull(path)?.isRegularFile == false) {
                        return@runCatching null
                    }
                    val languageCodeAndCountryCode = extractLanguageCodeAndCountryCodeFromFileName(path.name)
                        ?: return@runCatching null
                    languageCodeAndCountryCode.toLanguageInfo(
                        path = "$RESOURCE_PROTOCOL://$path",
                    )
                }.getOrNull()
            }
    }

    companion object {
        lateinit var instance: LanguageManager
        private const val LOCALES_PATH = "/com/abdownloadmanager/resources/locales"
        val DefaultLanguageInfo = LanguageInfo(
            languageCode = "en",
            countryCode = "US",
            nativeName = "English",
            path = URI("$RESOURCE_PROTOCOL:$LOCALES_PATH/en_US.properties")
        )

        fun openStream(uri: URI): InputStream {
            return when (uri.scheme) {
                RESOURCE_PROTOCOL -> FileSystem.RESOURCES.source(uri.path.toPath())
                FILE_PROTOCOL -> FileSystem.SYSTEM.source(uri.path.toPath())
                else -> error("unsupported URI")
            }.buffer().inputStream()
        }

        private fun MyLocale.toLanguageInfo(
            path: String,
        ): LanguageInfo {
            return LanguageInfo(
                languageCode = languageCode,
                countryCode = countryCode,
                nativeName = LanguageNameProvider.getNativeName(this),
                path = URI(path),
            )
        }

        private val rtlLanguages = arrayOf("ar", "fa", "he", "iw", "ji", "ur", "yi")

        private fun extractLanguageCodeAndCountryCodeFromFileName(name: String): MyLocale? {
            return name
                .split(".")
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    it.split("_").run {
                        MyLocale(
                            languageCode = get(0),
                            countryCode = getOrNull(1)
                        )
                    }
                }
        }
    }
}

interface MessageData {
    fun getMessage(key: String): String?
}

class PropertiesMessageContainer(
    private val properties: Properties,
) : MessageData {
    override fun getMessage(key: String): String? {
        return properties.getProperty(key)
    }
}

private data class LoadedLanguage(
    val languageInfo: LanguageInfo,
    val messageData: MessageData,
)

@Immutable
data class LanguageInfo(
    val languageCode: String,
    val countryCode: String?,
    val nativeName: String,
    val path: URI,
)
