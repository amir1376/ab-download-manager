package com.abdownloadmanager.shared.utils.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val LocalTitleBarDirection = staticCompositionLocalOf<LayoutDirection> {
    error("TitleBarDirection not provided")
}

@Composable
fun WithTitleBarDirection(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LocalTitleBarDirection.current
    ) {
        content()
    }
}