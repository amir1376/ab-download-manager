package com.abdownloadmanager.shared.singledownloadpage

import androidx.compose.runtime.Immutable
import ir.amirab.util.compose.StringSource

@Immutable
data class SingleDownloadPagePropertyItem(
    val name: StringSource,
    val value: StringSource,
    val valueState: ValueType = ValueType.Normal,
) {
    enum class ValueType { Normal, Error, Success }
}
