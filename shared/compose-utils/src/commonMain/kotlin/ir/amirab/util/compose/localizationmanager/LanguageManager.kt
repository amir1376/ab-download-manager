package ir.amirab.util.compose.localizationmanager

import androidx.compose.runtime.Immutable
import ir.amirab.resources.contracts.MyLanguageResource
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.*

class LanguageManager(
    storage: LanguageStorage,
    private val languageSourceProvider: LanguageSourceProvider,
) {
    @Suppress("PrivatePropertyName")
    private val DefaultLanguageInfo = languageSourceProvider
        .defaultLanguageResource
        .let {
            MyLocale
                .fromLanguageResource(it)
                .toLanguageInfo(it)
        }

    private val _languageList: MutableStateFlow<List<LanguageInfo>> = MutableStateFlow(emptyList())
    val languageList = _languageList.asStateFlow()
    val systemLanguageOrDefault: LanguageInfo by lazy {
        getSystemLanguageIfWeCanUse()
    }
    val selectedLanguageInStorage = storage.selectedLanguage
    val selectedLanguage = storage.selectedLanguage.mapStateFlow {
        it ?: systemLanguageOrDefault.toLocaleString()
    }

    //    val selectedLanguageInfo = selectedLanguage.mapStateFlow {
//        bestLanguageInfo(it)
//    }
    val isRtl = selectedLanguage.mapStateFlow { selectedLanguage ->
        rtlLanguages.any { selectedLanguage.startsWith(it) }
    }

    fun boot() {
        _languageList.value = getAvailableLanguages()
        instance = this
    }

    fun selectLanguage(languageInfo: LanguageInfo?) {
//        ensure that language info is in the list!
//        val languageInfo = languageList.value.find { it == languageInfo }
//        selectedLanguage.value = (languageInfo ?: DefaultLanguageInfo).toLocaleString()
        selectedLanguageInStorage.value = languageInfo?.toLocaleString()
    }

    fun getMessage(key: String): String {
        return getMessageContainer().getMessage(key)?.takeIf { it.isNotBlank() }
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
                        openStream(languageInfo.resource)
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

    /**
     * Find the best language info for the given locale.
     * the returned language is guaranteed to be available. (at least [DefaultLanguageInfo])
     */
    private fun bestLanguageInfo(locale: String): LanguageInfo {
        return languageList.value.find {
            it.toLocaleString() == locale
        } ?: DefaultLanguageInfo
    }

    private fun getMessageContainer(): MessageData {
        val requestedLanguage = getRequestedLanguage()
        this.loadedLanguage.let { loadedLanguage ->
            if (loadedLanguage != null && loadedLanguage.languageInfo.toLocaleString() == requestedLanguage) {
                return loadedLanguage.messageData
            }
        }
        synchronized(this) {
            // make sure not created earlier
            this.loadedLanguage.let { loadedLanguage ->
                if (loadedLanguage != null && loadedLanguage.languageInfo.toLocaleString() == requestedLanguage) {
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
        return languageSourceProvider.allLanguageResources
            .mapNotNull {
                runCatching {
                    MyLocale
                        .fromLanguageResource(it)
                        .toLanguageInfo(it)
                }.onFailure {
                    println("fail to load $it")
                    it.printStackTrace()
                }
                    .getOrNull()
            }

    }

    private fun getSystemLanguageIfWeCanUse(): LanguageInfo {
        val systemLocale = getSystemLocale().toString()
        return bestLanguageInfo(systemLocale)
    }

    companion object {
        lateinit var instance: LanguageManager

        fun openStream(source: MyLanguageResource): InputStream {
            return runBlocking {
                source.getData().inputStream()
            }
        }

        private fun MyLocale.toLanguageInfo(
            languageResource: MyLanguageResource,
        ): LanguageInfo {
            return LanguageInfo(
                locale = MyLocale(
                    languageCode = languageCode,
                    countryCode = countryCode,
                ),
                nativeName = LanguageNameProvider.getNativeName(this),
                resource = languageResource,
            )
        }

        private val rtlLanguages = arrayOf("ar", "bqi", "ckb", "fa", "he", "iw", "ji", "ur", "yi")
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
    val locale: MyLocale,
    val nativeName: String,
    val resource: MyLanguageResource,
) {
    fun toLocaleString(): String {
        return locale.toString()
    }
}

private fun getSystemLocale(): MyLocale {
    val javaSystemLocale = Locale.getDefault(Locale.Category.DISPLAY)
    return MyLocale(
        languageCode = javaSystemLocale.language,
        countryCode = javaSystemLocale.country,
    )
}
fun MyLocale.Companion.fromLanguageResource(languageResource: MyLanguageResource): MyLocale {
    return languageResource.language.split("_").run {
        MyLocale(
            languageCode = get(0),
            countryCode = getOrNull(1)
        )
    }
}
