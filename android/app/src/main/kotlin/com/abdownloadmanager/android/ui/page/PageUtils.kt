package com.abdownloadmanager.android.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.abdownloadmanager.shared.util.ui.myColors

@Composable
fun BoxScope.HeaderFade(
    topHeight: Dp,
    color: Color = myColors.background,
) {
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(topHeight)
            .background(
                Brush.verticalGradient(
                    listOf(
                        color,
                        Color.Transparent,
                    )
                )
            )
    )
}

@Composable
fun BoxScope.FooterFade(
    bottomHeight: Dp,
    color: Color = myColors.background,
) {
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(bottomHeight)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        color,
                    )
                )
            )
    )
}


@Composable
fun rememberHeaderAlpha(
    listState: LazyListState,
    headerHeightPx: Float,
): State<Float> {
    val headerHeightPx by rememberUpdatedState(headerHeightPx)
    return remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                headerHeightPx == 0f -> 1f
                else -> {
                    val scrolled = listState.firstVisibleItemScrollOffset.toFloat()
                    (scrolled / headerHeightPx).coerceIn(0f, 1f)
                }
            }
        }
    }
}

fun createAlphaForHeader(
    scrollOffset: Float,
    headerHeight: Float,
): Float {
    if (headerHeight == 0f) return 0f
    return (scrollOffset / headerHeight).coerceIn(0f..1f)
}
