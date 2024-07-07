package com.abdownloadmanager.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalIsDebugMode = staticCompositionLocalOf { false }

@Composable
fun ProvideDebugInfo(
    debug:Boolean,
    content:@Composable ()->Unit
){
    CompositionLocalProvider(
        LocalIsDebugMode provides debug,
        content
    )
}

@Composable
fun useIsInDebugMode(): Boolean {
    return LocalIsDebugMode.current
}