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
        "az" to LanguageName("azərbaycan", "Azerbaijani"),
        "be" to LanguageName("беларуская", "Belarusian"),
        "bg" to LanguageName("български", "Bulgarian"),
        "bm" to LanguageName("bamanakan", "Bambara"),
        "bn" to LanguageName("বাংলা", "Bengali"),
        "bo" to LanguageName("བོད་སྐད་", "Tibetan"),
        "br" to LanguageName("brezhoneg", "Breton"),
        "bs" to LanguageName("bosanski", "Bosnian"),
        "ca" to LanguageName("català", "Catalan"),
        "cs" to LanguageName("čeština", "Czech"),
        "cy" to LanguageName("Cymraeg", "Welsh"),
        "da" to LanguageName("dansk", "Danish"),
        "de" to LanguageName("Deutsch", "German"),
        "de_AT" to LanguageName("Österreichisches Deutsch", "Austrian German"),
        "de_CH" to LanguageName("Schweizer Hochdeutsch", "Swiss German"),
        "dz" to LanguageName("རྫོང་ཁ", "Dzongkha"),
        "ee" to LanguageName("eʋegbe", "Ewe"),
        "el" to LanguageName("Ελληνικά", "Greek"),
        "en" to LanguageName("English", "English"),
        "eo" to LanguageName("esperanto", "Esperanto"),
        "es" to LanguageName("español", "Spanish"),
        "et" to LanguageName("eesti", "Estonian"),
        "eu" to LanguageName("euskara", "Basque"),
        "fa" to LanguageName("فارسی", "Persian"),
        "ff" to LanguageName("Pulaar", "Fulah"),
        "fi" to LanguageName("suomi", "Finnish"),
        "fo" to LanguageName("føroyskt", "Faroese"),
        "fr" to LanguageName("français", "French"),
        "fr_CA" to LanguageName("français canadien", "Canadian French"),
        "fr_CH" to LanguageName("français suisse", "Swiss French"),
        "fy" to LanguageName("West-Frysk", "Western Frisian"),
        "ga" to LanguageName("Gaeilge", "Irish"),
        "gd" to LanguageName("Gàidhlig", "Scottish Gaelic"),
        "gl" to LanguageName("galego", "Galician"),
        "gu" to LanguageName("ગુજરાતી", "Gujarati"),
        "gv" to LanguageName("Gaelg", "Manx"),
        "ha" to LanguageName("Hausa", "Hausa"),
        "he" to LanguageName("עברית", "Hebrew"),
        "hi" to LanguageName("हिंदी", "Hindi"),
        "hr" to LanguageName("hrvatski", "Croatian"),
        "hu" to LanguageName("magyar", "Hungarian"),
        "hy" to LanguageName("հայերեն", "Armenian"),
        "id" to LanguageName("Bahasa Indonesia", "Indonesian"),
        "ig" to LanguageName("Igbo", "Igbo"),
        "ii" to LanguageName("ꆈꌠꉙ", "Sichuan Yi"),
        "is" to LanguageName("íslenska", "Icelandic"),
        "it" to LanguageName("italiano", "Italian"),
        "ja" to LanguageName("日本語", "Japanese"),
        "ka" to LanguageName("ქართული", "Georgian"),
        "ki" to LanguageName("Gikuyu", "Kikuyu"),
        "kk" to LanguageName("қазақ тілі", "Kazakh"),
        "kl" to LanguageName("kalaallisut", "Greenlandic"),
        "km" to LanguageName("ខ្មែរ", "Khmer"),
        "kn" to LanguageName("ಕನ್ನಡ", "Kannada"),
        "ko" to LanguageName("한국어", "Korean"),
        "ks" to LanguageName("کٲشُر", "Kashmiri"),
        "kw" to LanguageName("kernewek", "Cornish"),
        "ky" to LanguageName("кыргызча", "Kyrgyz"),
        "lb" to LanguageName("Lëtzebuergesch", "Luxembourgish"),
        "lg" to LanguageName("Luganda", "Ganda"),
        "ln" to LanguageName("lingála", "Lingala"),
        "lo" to LanguageName("ລາວ", "Lao"),
        "lt" to LanguageName("lietuvių", "Lithuanian"),
        "lu" to LanguageName("Tshiluba", "Luba-Katanga"),
        "lv" to LanguageName("latviešu", "Latvian"),
        "mg" to LanguageName("Malagasy", "Malagasy"),
        "mk" to LanguageName("македонски", "Macedonian"),
        "ml" to LanguageName("മലയാളം", "Malayalam"),
        "mn" to LanguageName("монгол", "Mongolian"),
        "mr" to LanguageName("मराठी", "Marathi"),
        "ms" to LanguageName("Bahasa Melayu", "Malay"),
        "mt" to LanguageName("Malti", "Maltese"),
        "my" to LanguageName("ဗမာ", "Burmese"),
        "nb" to LanguageName("norsk bokmål", "Norwegian Bokmål"),
        "nd" to LanguageName("isiNdebele", "North Ndebele"),
        "ne" to LanguageName("नेपाली", "Nepali"),
        "nl" to LanguageName("Nederlands", "Dutch"),
        "nl_BE" to LanguageName("Vlaams", "Flemish"),
        "nn" to LanguageName("nynorsk", "Norwegian Nynorsk"),
        "no" to LanguageName("norsk", "Norwegian"),
        "om" to LanguageName("Oromoo", "Oromo"),
        "or" to LanguageName("ଓଡ଼ିଆ", "Odia"),
        "os" to LanguageName("ирон", "Ossetic"),
        "pa" to LanguageName("ਪੰਜਾਬੀ", "Punjabi"),
        "pl" to LanguageName("polski", "Polish"),
        "ps" to LanguageName("پښتو", "Pashto"),
        "pt" to LanguageName("português", "Portuguese"),
        "pt_BR" to LanguageName("português do Brasil", "Brazilian Portuguese"),
        "qu" to LanguageName("Runasimi", "Quechua"),
        "rm" to LanguageName("rumantsch", "Romansh"),
        "rn" to LanguageName("Ikirundi", "Rundi"),
        "ro" to LanguageName("română", "Romanian"),
        "ro_MD" to LanguageName("moldovenească", "Moldovan"),
        "ru" to LanguageName("русский", "Russian"),
        "rw" to LanguageName("Kinyarwanda", "Kinyarwanda"),
        "se" to LanguageName("davvisámegiella", "Northern Sami"),
        "sg" to LanguageName("Sängö", "Sango"),
        "sh" to LanguageName("Srpskohrvatski", "Serbo-Croatian"),
        "si" to LanguageName("සිංහල", "Sinhala"),
        "sk" to LanguageName("slovenčina", "Slovak"),
        "sl" to LanguageName("slovenščina", "Slovene"),
        "sn" to LanguageName("chiShona", "Shona"),
        "so" to LanguageName("Soomaali", "Somali"),
        "sq" to LanguageName("shqip", "Albanian"),
        "sr" to LanguageName("српски", "Serbian"),
        "sv" to LanguageName("svenska", "Swedish"),
        "sw" to LanguageName("Kiswahili", "Swahili"),
        "ta" to LanguageName("தமிழ்", "Tamil"),
        "te" to LanguageName("తెలుగు", "Telugu"),
        "th" to LanguageName("ไทย", "Thai"),
        "ti" to LanguageName("ትግርኛ", "Tigrinya"),
        "tl" to LanguageName("Tagalog", "Tagalog"),
        "to" to LanguageName("lea fakatonga", "Tongan"),
        "tr" to LanguageName("Türkçe", "Turkish"),
        "ug" to LanguageName("ئۇيغۇرچە", "Uyghur"),
        "uk" to LanguageName("українська", "Ukrainian"),
        "ur" to LanguageName("اردو", "Urdu"),
        "uz" to LanguageName("oʻzbekcha", "Uzbek"),
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
