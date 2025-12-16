package com.abdownloadmanager.shared.util.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.ScrollbarAdapter
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter


@Composable
fun TwoDimensionScrollableContent(
    modifier: Modifier,
    content: @Composable () -> Unit,
    verticalAdapter: ScrollbarAdapter,
    horizontalAdapter: ScrollbarAdapter
) {
    Row(modifier) {
        Column(Modifier.weight(1f)) {
            Box(Modifier.weight(1f)) {
                content()
            }
            if (horizontalAdapter.needScroll()) {
                MultiplatformHorizontalScrollbar(
                    horizontalAdapter,
                    Modifier.padding(
                        top = 4.dp,
                        bottom = 4.dp,
                    )
                )
            }
        }
        if (verticalAdapter.needScroll()) {
            MultiplatformVerticalScrollbar(
                verticalAdapter,
                Modifier.padding(
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
            )
        }
    }
}

@Composable
fun VerticalScrollableContent(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    VerticalScrollableContent(
        verticalAdapter = rememberScrollbarAdapter(scrollState),
        modifier = modifier,
        content = content,
    )
}

@Composable
fun VerticalScrollableContent(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    VerticalScrollableContent(
        verticalAdapter = rememberScrollbarAdapter(lazyListState),
        modifier = modifier,
        content = content,
    )
}

@Composable
fun VerticalScrollableContent(
    verticalAdapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    style: ScrollbarStyle = LocalMultiplatformScrollbarStyle.current,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        val needScroll = verticalAdapter.needScroll()
        val horizontalPadding = 4.dp
        val endPadding = if (needScroll) {
            style.thickness + horizontalPadding
        } else {
            0.dp
        }
        Box(Modifier.padding(end = endPadding)) {
            content()
        }
        if (needScroll) {
            MultiplatformVerticalScrollbar(
                verticalAdapter,
                modifier = Modifier
                    .matchParentSize()
                    .wrapContentWidth(Alignment.End)
                    .padding(
                        horizontal = horizontalPadding,
                    )
                    .width(style.thickness),
                style = style,
            )
        }
    }
}
