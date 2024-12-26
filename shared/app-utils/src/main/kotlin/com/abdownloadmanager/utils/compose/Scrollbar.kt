package com.abdownloadmanager.utils.compose

fun androidx.compose.foundation.v2.ScrollbarAdapter.needScroll(): Boolean {
    return contentSize > viewportSize
}