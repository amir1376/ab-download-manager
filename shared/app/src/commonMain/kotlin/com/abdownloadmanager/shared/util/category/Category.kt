package com.abdownloadmanager.shared.util.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.LocalIconFromUriResolver
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
    val hasFileTypes = acceptedFileTypes.isNotEmpty()
    val hasUrlPattern = acceptedUrlPatterns.isNotEmpty()
    private val filterCount = run {
        var count = 0
        if (hasFileTypes) count++
        if (hasUrlPattern) count++
        count
    }
    val hasFilters = filterCount > 0

    fun acceptFileName(fileName: String): Boolean {
        if (!hasFileTypes) {
            return true
        }
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

fun Category.iconSource(
    iconResolver: IIconResolver,
): IconSource? {
    return iconResolver.resolve(icon)
}

@Composable
fun Category.rememberIconPainter(): IconSource? {
    val iconResolver = LocalIconFromUriResolver.current
    return remember(icon) {
        iconResolver.resolve(icon)
    }
}
