package com.abdownloadmanager.desktop.utils

import androidx.compose.runtime.*
import java.util.*

private val LocalBottomSheetContainer =
    compositionLocalOf<MutableMap<Any, @Composable () -> Unit>> { error("not initialized yet") }

@Composable
fun PlaceInHost(
    item: @Composable () -> Unit
) {
    val key = remember {
        UUID.randomUUID()
    }
    val container = LocalBottomSheetContainer.current
    DisposableEffect(key) {
        container[key] = item
        onDispose {
            container.remove(key)
        }
    }
}


@Composable
fun PopUpContainer(
    context: @Composable () -> Unit
) {
    val items = remember {
        mutableStateMapOf<Any, @Composable () -> Unit>()
    }
    CompositionLocalProvider(
        LocalBottomSheetContainer provides items,
        content = context
    )
    items.forEach {
        key(it.key) {
            it.value()
        }
    }
}