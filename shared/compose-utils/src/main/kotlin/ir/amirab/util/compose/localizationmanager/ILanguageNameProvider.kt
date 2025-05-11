package ir.amirab.util.compose.localizationmanager

import java.util.Locale

interface ILanguageNameProvider {
    fun getNativeName(myLocale: MyLocale): String
    fun getEnglishName(myLocale: MyLocale): String
    fun getName(myLocale: MyLocale): LanguageName
}

data class LanguageName(
    val nativeName: String,
    val englishName: String,
)

object LanguageNameProvider : ILanguageNameProvider {
    private val list = mapOf(
        "af" to LanguageName("Afrikaans", "Afrikaans"),
        "ak" to LanguageName("Akan", "Akan"),
        "am" to LanguageName("አማርኛ", "Amharic"),
        "ar" to LanguageName("العربية", "Arabic"),
        "as" to LanguageName("অসমীয়া", "Assamese"),
        "az" to LanguageName("Azərbaycanca", "Azerbaijani"),
        "be" to LanguageName("Беларуская", "Belarusian"),
        "bg" to LanguageName("Български", "Bulgarian"),
        "bm" to LanguageName("Bamanankan", "Bambara"),
        "bn" to LanguageName("বাংলা", "Bengali"),
        "bo" to LanguageName("བོད་སྐད་", "Tibetan"),
        "bqi" to LanguageName("لۊری بختیاری", "Luri Bakhtiari"),
        "br" to LanguageName("Brezhoneg", "Breton"),
        "bs" to LanguageName("Bosanski", "Bosnian"),
        "ca" to LanguageName("Català", "Catalan"),
        "cs" to LanguageName("Čeština", "Czech"),
        "cy" to LanguageName("Cymraeg", "Welsh"),
        "da" to LanguageName("Dansk", "Danish"),
        "de" to LanguageName("Deutsch", "German"),
        "de_AT" to LanguageName("Österreichisches Deutsch", "Austrian German"),
        "de_CH" to LanguageName("Schweizer Hochdeutsch", "Swiss German"),
        "dz" to LanguageName("རྫོང་ཁ", "Dzongkha"),
        "ee" to LanguageName("Eʋegbe", "Ewe"),
        "el" to LanguageName("Ελληνικά", "Greek"),
        "en" to LanguageName("English", "English"),
        "eo" to LanguageName("Esperanto", "Esperanto"),
        "es" to LanguageName("Español", "Spanish"),
        "et" to LanguageName("Eesti", "Estonian"),
        "eu" to LanguageName("Euskara", "Basque"),
        "fa" to LanguageName("فارسی", "Persian"),
        "ff" to LanguageName("Pulaar", "Fulah"),
        "fi" to LanguageName("Suomi", "Finnish"),
        "fo" to LanguageName("Føroyskt", "Faroese"),
        "fr" to LanguageName("Français", "French"),
        "fr_CA" to LanguageName("Français canadien", "Canadian French"),
        "fr_CH" to LanguageName("Français suisse", "Swiss French"),
        "fy" to LanguageName("Frysk", "Western Frisian"),
        "ga" to LanguageName("Gaeilge", "Irish"),
        "gd" to LanguageName("Gàidhlig", "Scottish Gaelic"),
        "gl" to LanguageName("Galego", "Galician"),
        "gu" to LanguageName("ગુજરાતી", "Gujarati"),
        "gv" to LanguageName("Gaelg", "Manx"),
        "ha" to LanguageName("Hausa", "Hausa"),
        "he" to LanguageName("עברית", "Hebrew"),
        "hi" to LanguageName("हिन्दी", "Hindi"),
        "hr" to LanguageName("Hrvatski", "Croatian"),
        "hu" to LanguageName("Magyar", "Hungarian"),
        "hy" to LanguageName("Հայերեն", "Armenian"),
        "id" to LanguageName("Bahasa Indonesia", "Indonesian"),
        "ig" to LanguageName("Igbo", "Igbo"),
        "ii" to LanguageName("ꆈꌠꉙ", "Sichuan Yi"),
        "is" to LanguageName("Íslenska", "Icelandic"),
        "it" to LanguageName("Italiano", "Italian"),
        "ja" to LanguageName("日本語", "Japanese"),
        "ka" to LanguageName("ქართული", "Georgian"),
        "ki" to LanguageName("Gikuyu", "Kikuyu"),
        "kk" to LanguageName("Қазақ тілі", "Kazakh"),
        "kl" to LanguageName("Kalaallisut", "Greenlandic"),
        "km" to LanguageName("ខ្មែរ", "Khmer"),
        "kn" to LanguageName("ಕನ್ನಡ", "Kannada"),
        "ko" to LanguageName("한국어", "Korean"),
        "ks" to LanguageName("کٲشُر", "Kashmiri"),
        "kw" to LanguageName("Kernewek", "Cornish"),
        "ky" to LanguageName("Кыргызча", "Kyrgyz"),
        "lb" to LanguageName("Lëtzebuergesch", "Luxembourgish"),
        "lg" to LanguageName("Luganda", "Ganda"),
        "ln" to LanguageName("Lingála", "Lingala"),
        "lo" to LanguageName("ລາວ", "Lao"),
        "lt" to LanguageName("Lietuvių", "Lithuanian"),
        "lu" to LanguageName("Tshiluba", "Luba-Katanga"),
        "lv" to LanguageName("Latviešu", "Latvian"),
        "mg" to LanguageName("Malagasy", "Malagasy"),
        "mk" to LanguageName("Македонски", "Macedonian"),
        "ml" to LanguageName("മലയാളം", "Malayalam"),
        "mn" to LanguageName("Монгол", "Mongolian"),
        "mr" to LanguageName("मराठी", "Marathi"),
        "ms" to LanguageName("Bahasa Melayu", "Malay"),
        "mt" to LanguageName("Malti", "Maltese"),
        "my" to LanguageName("ဗမာ", "Burmese"),
        "nb" to LanguageName("Norsk Bokmål", "Norwegian Bokmål"),
        "nd" to LanguageName("IsiNdebele", "North Ndebele"),
        "ne" to LanguageName("नेपाली", "Nepali"),
        "nl" to LanguageName("Nederlands", "Dutch"),
        "nl_BE" to LanguageName("Vlaams", "Flemish"),
        "nn" to LanguageName("Nynorsk", "Norwegian Nynorsk"),
        "no" to LanguageName("Norsk", "Norwegian"),
        "om" to LanguageName("Oromoo", "Oromo"),
        "or" to LanguageName("ଓଡ଼ିଆ", "Odia"),
        "os" to LanguageName("Ирон", "Ossetic"),
        "pa" to LanguageName("ਪੰਜਾਬੀ", "Punjabi"),
        "pl" to LanguageName("Polski", "Polish"),
        "ps" to LanguageName("پښتو", "Pashto"),
        "pt" to LanguageName("Português", "Portuguese"),
        "pt_BR" to LanguageName("Português do Brasil", "Brazilian Portuguese"),
        "qu" to LanguageName("Runasimi", "Quechua"),
        "rm" to LanguageName("Rumantsch", "Romansh"),
        "rn" to LanguageName("Ikirundi", "Rundi"),
        "ro" to LanguageName("Română", "Romanian"),
        "ro_MD" to LanguageName("moldovenească", "Moldovan"),
        "ru" to LanguageName("Русский", "Russian"),
        "rw" to LanguageName("Kinyarwanda", "Kinyarwanda"),
        "se" to LanguageName("Davvisámegiella", "Northern Sami"),
        "sg" to LanguageName("Sängö", "Sango"),
        "sh" to LanguageName("Srpskohrvatski", "Serbo-Croatian"),
        "si" to LanguageName("සිංහල", "Sinhala"),
        "sk" to LanguageName("Slovenčina", "Slovak"),
        "sl" to LanguageName("Slovenščina", "Slovene"),
        "sn" to LanguageName("chiShona", "Shona"),
        "so" to LanguageName("Soomaali", "Somali"),
        "sq" to LanguageName("Shqip", "Albanian"),
        "sr" to LanguageName("Српски", "Serbian"),
        "sv" to LanguageName("Svenska", "Swedish"),
        "sw" to LanguageName("Kiswahili", "Swahili"),
        "ta" to LanguageName("தமிழ்", "Tamil"),
        "te" to LanguageName("తెలుగు", "Telugu"),
        "th" to LanguageName("ไทย", "Thai"),
        "ti" to LanguageName("ትግርኛ", "Tigrinya"),
        "tl" to LanguageName("Tagalog", "Tagalog"),
        "to" to LanguageName("lea fakatonga", "Tongan"),
        "tr" to LanguageName("Türkçe", "Turkish"),
        "ug" to LanguageName("ئۇيغۇرچە", "Uyghur"),
        "uk" to LanguageName("Українська", "Ukrainian"),
        "ur" to LanguageName("اردو", "Urdu"),
        "uz" to LanguageName("Oʻzbekcha", "Uzbek"),
        "vi" to LanguageName("Tiếng Việt", "Vietnamese"),
        "yi" to LanguageName("ייִדיש", "Yiddish"),
        "yo" to LanguageName("Èdè Yorùbá", "Yoruba"),
        "zh" to LanguageName("中文", "Chinese"),
        "zh_CN" to LanguageName("简体中文", "Simplified Chinese"),
        "zh_TW" to LanguageName("正體中文", "Traditional Chinese"),
        "zu" to LanguageName("isiZulu", "Zulu")
    )

    override fun getNativeName(myLocale: MyLocale): String {
        return getName(myLocale).nativeName
    }

    override fun getEnglishName(myLocale: MyLocale): String {
        return getName(myLocale).englishName
    }

    override fun getName(myLocale: MyLocale): LanguageName {
        val languageCode = myLocale.languageCode
        val countryCode = myLocale.countryCode
        if (countryCode != null) {
            list["${languageCode}_${countryCode}"]?.let {
                return it
            }
        }
        list[languageCode]?.let {
            return it
        }
        return default(myLocale).let { LanguageName(it, it) }
    }
    
    private fun default(myLocale: MyLocale): String {
        return myLocale
            .toLocale()
            .let { it.getDisplayName(it) }
    }
}

private fun MyLocale.toLocale(): Locale {
    val language = languageCode
    val country = countryCode
    return if (country == null) {
        Locale(language)
    } else {
        Locale(language, country)
    }
}
