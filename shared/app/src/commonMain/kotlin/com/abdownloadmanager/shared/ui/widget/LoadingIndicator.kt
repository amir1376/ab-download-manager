package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.util.ui.myColors
import androidx.annotation.FloatRange
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(
    modifier: Modifier,
    sweepAngle: Float = 90f, // angle (length) of indicator arc
    color: Color = myColors.primary, // color of indicator arc line
    strokeWidth: Dp = 4.dp,
) {
    val transition = rememberInfiniteTransition()

    // define the changing value from 0 to 360.
    // This is the angle of the beginning of indicator arc
    // this value will change over time from 0 to 360 and repeat indefinitely.
    // it changes starting position of the indicator arc and the animation is obtained
    val currentArcStartAngle by transition.animateValue(
        0,
        360,
        Int.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            )
        )
    )

    IndicatorCanvas(
        modifier = modifier,
        currentArcStartAngle = currentArcStartAngle,
        strokeWidth = strokeWidth,
        color = SolidColor(color),
        sweepAngle = sweepAngle,
    )
}
@Composable
fun LoadingIndicator(
    modifier: Modifier,
    color: Color = myColors.primary, // color of indicator arc line
    strokeWidth: Dp = 4.dp,
    @FloatRange(0.0,1.0)
    progress:Float
) {
    IndicatorCanvas(
        modifier = modifier,
        currentArcStartAngle = 0,
        sweepAngle = (progress * 360).coerceIn(0f, 360f),
        strokeWidth = strokeWidth,
        color = SolidColor(color),
    )
}

@Composable
fun LoadingIndicatorWithBrush(
    modifier: Modifier,
    brush: Brush = SolidColor(myColors.primary), // color of indicator arc line
    strokeWidth: Dp = 4.dp,
    @FloatRange(0.0, 1.0)
    progress: Float
) {
    IndicatorCanvas(
        modifier = modifier,
        currentArcStartAngle = 0,
        sweepAngle = (progress*360).coerceIn(0f,360f),
        strokeWidth = strokeWidth,
        color = brush,
    )
}
@Composable
fun IndicatorCanvas(
    modifier: Modifier,
    currentArcStartAngle: Int,
    sweepAngle:Float,
    strokeWidth: Dp,
    color: Brush,
) {
    // define stroke with given width and arc ends type considering device DPI
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    }
    // draw on canvas
    Canvas(
        modifier
            .progressSemantics() // (optional) for Accessibility services
            .padding(strokeWidth / 2) //padding. otherwise, not the whole circle will fit in the canvas
    ) {
        // draw arc with the same stroke
        drawArc(
            color,
            // arc start angle
            // -90 shifts the start position towards the y-axis
            startAngle = currentArcStartAngle.toFloat() - 90,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = stroke
        )
    }
}
