package com.abdownloadmanager.shared.utils

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun rememberDotLoading(): String {
    val transition = rememberInfiniteTransition()
    val count by transition.animateValue(
        1,
        4,
        Int.VectorConverter,
        infiniteRepeatable(
            tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    return buildString {
        for (i in 1..3) {
            if (i <= count) {
                append(".")
            } else {
                append(" ")
            }
        }
    }
}