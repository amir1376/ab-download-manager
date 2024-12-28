package ir.amirab.util.compose.localizationmanager

import androidx.compose.runtime.Immutable

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
}