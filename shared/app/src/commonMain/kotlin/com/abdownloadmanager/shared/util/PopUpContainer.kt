package com.abdownloadmanager.shared.util

import androidx.compose.runtime.*
import java.util.*

private val LocalBottomSheetContainer =
    staticCompositionLocalOf<MutableList<Pair<Any, @Composable () -> Unit>>> { error("not initialized yet") }

@Composable
fun PlaceInHost(
    item: @Composable () -> Unit
) {
    val key = remember {
        UUID.randomUUID()
    }
    val container = LocalBottomSheetContainer.current
    DisposableEffect(key) {
        val item = key to item
        container.add(item)
        onDispose {
            container.remove(item)
        }
    }
}


@Composable
fun PopUpContainer(
    content: @Composable () -> Unit
) {
    val items = remember {
        mutableStateListOf<Pair<Any, @Composable () -> Unit>>()
    }
    CompositionLocalProvider(
        LocalBottomSheetContainer provides items,
        content = {
            content()
            items.forEach { (key, content) ->
                key(key) {
                    content()
                }
            }
        }
    )
}
