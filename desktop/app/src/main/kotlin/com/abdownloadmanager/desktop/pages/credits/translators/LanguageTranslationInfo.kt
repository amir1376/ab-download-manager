package com.abdownloadmanager.desktop.pages.credits.translators

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class LanguageTranslationInfo(
    val locale: String,// en,es_ES etc...
    val englishName: String,//Persian etc...
    val nativeName: String,//فارسی ...
    val translators: List<Translator>,
)

typealias TranslatorData = @Serializable Map<String, List<Translator>>

@Serializable
data class Translator(
    @SerialName("name")
    val name: String,
    @SerialName("link")
    val link: String,
)