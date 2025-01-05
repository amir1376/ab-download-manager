package com.abdownloadmanager.shared.utils.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.fromUri
import ir.amirab.util.wildcardMatch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param path
 * this is a default download path for this category
 * @param icon
 * can be used by [IconSource]
 */
@Immutable
@Serializable
data class Category(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("icon")
    val icon: String,
    @SerialName("path")
    // don't directly use this check for usePath first! see [getDownloadPath()]
    val path: String,
    @SerialName("usePath")
    val usePath: Boolean = true,
    @SerialName("acceptedFileTypes")
    val acceptedFileTypes: List<String> = emptyList(),
    // this is optional if nothing provided it means that every url is acceptable
    @SerialName("acceptedUrlPatterns")
    val acceptedUrlPatterns: List<String> = emptyList(),
    @SerialName("items")
    val items: List<Long> = emptyList(),
) {
    val hasUrlPattern = acceptedUrlPatterns.isNotEmpty()

    fun acceptFileName(fileName: String): Boolean {
        return acceptedFileTypes.any { ext ->
            fileName.endsWith(
                suffix = ".$ext",
                ignoreCase = true
            )
        }
    }

    fun withExtraItems(newItems: List<Long>): Category {
        return copy(
            items = items.plus(newItems).distinct()
        )
    }
    fun getDownloadPath(): String? {
        return if (usePath) path else null
    }
    fun acceptUrl(url: String): Boolean {
        if (!hasUrlPattern) {
            return true
        }
        return acceptedUrlPatterns.any {
            wildcardMatch(
                pattern = it,
                input = url
            )
        }
    }
}

fun Category.iconSource(): IconSource? {
    return IconSource.fromUri(icon)
}

@Composable
fun Category.rememberIconPainter(): IconSource? {
    return remember(icon) {
        iconSource()
    }
}