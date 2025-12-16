package com.abdownloadmanager.android.pages.home

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@Composable
fun SimplePager(
    pageCount: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    var pageWidth by remember { mutableStateOf(0) }

    // Snap scroll when page changes externally
    LaunchedEffect(currentPage, pageWidth) {
        val target = currentPage * pageWidth
        if (scrollState.value != target) {
            scrollState.animateScrollTo(target)
        }
    }

    // When user finishes scrolling -> snap to nearest page
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress && pageWidth > 0) {
            val pos = scrollState.value
            val newPage = (pos.toFloat() / pageWidth).roundToInt().coerceIn(0, pageCount - 1)

            val snapPos = newPage * pageWidth
            if (snapPos != pos) {
                scrollState.animateScrollTo(snapPos)
            }

            if (newPage != currentPage) {
                onPageChanged(newPage)
            }
        }
    }

    Box(
        modifier
            .onSizeChanged { pageWidth = it.width }
            .horizontalScroll(scrollState)
    ) {
        Row {
            repeat(pageCount) { index ->
                Box(
                    Modifier
                        .width(with(LocalDensity.current) { pageWidth.toDp() })
                        .fillMaxHeight()
                ) {
                    content(index)
                }
            }
        }
    }
}
