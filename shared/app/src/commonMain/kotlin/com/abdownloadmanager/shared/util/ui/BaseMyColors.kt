package com.abdownloadmanager.shared.util.ui

import androidx.compose.ui.graphics.vector.ImageVector
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.contants.ICON_PROTOCOL

abstract class BaseMyColors : IMyIcons, IIconResolver {
    val iconMap = mutableMapOf<String, IconSource>()
    fun ImageVector.asIconSource(
        name: String,
        requiredTint: Boolean = true,
    ): IconSource {
        val uri = "$ICON_PROTOCOL:$name"
        return IconSource.VectorIconSource(this, requiredTint, uri)
            .asIconSource()
    }

//    fun String.asIconSource(
//        path: String,
//        requiredTint: Boolean = true
//    ): IconSource = apply {
//        val uri = "$RESOURCE_PROTOCOL:$path?tint=${requiredTint}"
//        return IconSource.ResourceIconSource(this, requiredTint, uri).asSource()
//    }

    fun IconSource.asIconSource(): IconSource = apply {
        uri?.let {
            iconMap[it] = this
        }
    }

    override fun resolve(uri: String): IconSource? {
        return iconMap[uri]
    }
}
