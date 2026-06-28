package com.xeton.util.compose.localizationmanager

import androidx.compose.runtime.Immutable
import com.xeton.resources.contracts.MyLanguageResource

@Immutable
data class MyLocale(
    val languageCode: String,
    val countryCode: String?,
) {
    override fun toString(): String {
        return buildString {
            append(languageCode)
            countryCode?.let {
                append("_")
                append(it)
            }
        }
    }

    companion object
}
