package com.abdownloadmanager.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

val LocalContentColor = compositionLocalOf { Color.Black }
val LocalContentAlpha = compositionLocalOf { 1f }

val LocalTextStyle = compositionLocalOf(structuralEqualityPolicy()) { TextStyle() }

@Composable
fun ProvideTextStyle(value: TextStyle, content: @Composable () -> Unit) {
    val mergedStyle = LocalTextStyle.current.merge(value)
    CompositionLocalProvider(LocalTextStyle provides mergedStyle, content = content)
}

@Composable
fun WithContentAlpha(
    newAlpha: Float,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentAlpha provides newAlpha,
        content = content
    )
}

@Composable
fun WithContentColor(
    newColor: Color,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides newColor,
        content = content
    )
}