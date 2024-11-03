package ir.amirab.util.compose.localizationmanager

import java.util.Locale

interface ILanguageNameProvider {
    fun getNativeName(myLocale: MyLocale): String
}

object LanguageNameProvider : ILanguageNameProvider {
    private val list = mapOf(
        "af" to "Afrikaans",
        "ak" to "Akan",
        "am" to "አማርኛ",
        "ar" to "العربية",
        "as" to "অসমীয়া",
        "az" to "azərbaycan",
        "be" to "беларуская",
        "bg" to "български",
        "bm" to "bamanakan",
        "bn" to "বাংলা",
        "bo" to "བོད་སྐད་",
        "br" to "brezhoneg",
        "bs" to "bosanski",
        "ca" to "català",
        "cs" to "čeština",
        "cy" to "Cymraeg",
        "da" to "dansk",
        "de" to "Deutsch",
        "de_AT" to "Österreichisches Deutsch",
        "de_CH" to "Schweizer Hochdeutsch",
        "dz" to "རྫོང་ཁ",
        "ee" to "eʋegbe",
        "el" to "Ελληνικά",
        "en" to "English",
        "eo" to "esperanto",
        "es" to "español",
        "et" to "eesti",
        "eu" to "euskara",
        "fa" to "فارسی",
        "ff" to "Pulaar",
        "fi" to "suomi",
        "fo" to "føroyskt",
        "fr" to "français",
        "fr_CA" to "français canadien",
        "fr_CH" to "français suisse",
        "fy" to "West-Frysk",
        "ga" to "Gaeilge",
        "gd" to "Gàidhlig",
        "gl" to "galego",
        "gu" to "ગુજરાતી",
        "gv" to "Gaelg",
        "ha" to "Hausa",
        "he" to "עברית",
        "hi" to "हिंदी",
        "hr" to "hrvatski",
        "hu" to "magyar",
        "hy" to "հայերեն",
        "id" to "Bahasa Indonesia",
        "ig" to "Igbo",
        "ii" to "ꆈꌠꉙ",
        "is" to "íslenska",
        "it" to "italiano",
        "ja" to "日本語",
        "ka" to "ქართული",
        "ki" to "Gikuyu",
        "kk" to "қазақ тілі",
        "kl" to "kalaallisut",
        "km" to "ខ្មែរ",
        "kn" to "ಕನ್ನಡ",
        "ko" to "한국어",
        "ks" to "کٲشُر",
        "kw" to "kernewek",
        "ky" to "кыргызча",
        "lb" to "Lëtzebuergesch",
        "lg" to "Luganda",
        "ln" to "lingála",
        "lo" to "ລາວ",
        "lt" to "lietuvių",
        "lu" to "Tshiluba",
        "lv" to "latviešu",
        "mg" to "Malagasy",
        "mk" to "македонски",
        "ml" to "മലയാളം",
        "mn" to "монгол",
        "mr" to "मराठी",
        "ms" to "Bahasa Melayu",
        "mt" to "Malti",
        "my" to "ဗမာ",
        "nb" to "norsk bokmål",
        "nd" to "isiNdebele",
        "ne" to "नेपाली",
        "nl" to "Nederlands",
        "nl_BE" to "Vlaams",
        "nn" to "nynorsk",
        "no" to "norsk",
        "om" to "Oromoo",
        "or" to "ଓଡ଼ିଆ",
        "os" to "ирон",
        "pa" to "ਪੰਜਾਬੀ",
        "pl" to "polski",
        "ps" to "پښتو",
        "pt" to "português",
        "pt_BR" to "português do Brasil",
        "qu" to "Runasimi",
        "rm" to "rumantsch",
        "rn" to "Ikirundi",
        "ro" to "română",
        "ro_MD" to "moldovenească",
        "ru" to "русский",
        "rw" to "Kinyarwanda",
        "se" to "davvisámegiella",
        "sg" to "Sängö",
        "sh" to "Srpskohrvatski",
        "si" to "සිංහල",
        "sk" to "slovenčina",
        "sl" to "slovenščina",
        "sn" to "chiShona",
        "so" to "Soomaali",
        "sq" to "shqip",
        "sr" to "српски",
        "sv" to "svenska",
        "sw" to "Kiswahili",
        "ta" to "தமிழ்",
        "te" to "తెలుగు",
        "th" to "ไทย",
        "ti" to "ትግርኛ",
        "tl" to "Tagalog",
        "to" to "lea fakatonga",
        "tr" to "Türkçe",
        "ug" to "ئۇيغۇرچە",
        "uk" to "українська",
        "ur" to "اردو",
        "uz" to "oʻzbekcha",
        "vi" to "Tiếng Việt",
        "yi" to "ייִדיש",
        "yo" to "Èdè Yorùbá",
        "zh" to "中文",
        "zh_CN" to "简体中文",
        "zh_TW" to "繁體中文",
        "zu" to "isiZulu",
    )

    override fun getNativeName(myLocale: MyLocale): String {
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
        return default(myLocale)
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