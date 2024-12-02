package com.abdownloadmanager.utils.compose.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


@Composable
fun BoxScope.ScrollFade(
    scrollState: ScrollState,
    orientation: Orientation,
    gradientLength: Float = 0.2f, // 0f .. 1f
    targetBackground: Color,
) {
    AnimatedVisibility(
        scrollState.canScrollBackward,
        modifier = Modifier.matchParentSize(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Spacer(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.gradientOrientation(
                        colorStops = arrayOf(
                            0f to targetBackground,
                            gradientLength to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                        orientation = orientation,
                    )
                )
        )
    }
    AnimatedVisibility(
        scrollState.canScrollForward,
        modifier = Modifier.matchParentSize(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Spacer(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.gradientOrientation(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            (1 - gradientLength) to Color.Transparent,
                            1f to targetBackground,
                        ),
                        orientation = orientation,
                    )
                )
        )
    }
}

private fun Brush.Companion.gradientOrientation(
    vararg colorStops: Pair<Float, Color>,
    orientation: Orientation,
): Brush {
    return when (orientation) {
        Orientation.Vertical -> Brush.verticalGradient(
            colorStops = colorStops
        )

        Orientation.Horizontal -> {
            Brush.horizontalGradient(
                colorStops = colorStops
            )
        }
    }
}
