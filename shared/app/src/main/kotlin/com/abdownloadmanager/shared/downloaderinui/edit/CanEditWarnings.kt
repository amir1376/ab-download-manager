package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSourceWithARgs

sealed interface CanEditWarnings {
    fun asStringSource(): StringSource
    data class FileSizeNotMatch(
        val currentSize: Long,
        val newSize: Long,
    ) : CanEditWarnings {
        override fun asStringSource(): StringSource {
            return Res.string.edit_download_saved_download_item_size_not_match
                .asStringSourceWithARgs(
                    Res.string.edit_download_saved_download_item_size_not_match_createArgs(
                        currentSize = "$currentSize",
                        newSize = "$newSize",
                    )
                )
        }

    }
}
