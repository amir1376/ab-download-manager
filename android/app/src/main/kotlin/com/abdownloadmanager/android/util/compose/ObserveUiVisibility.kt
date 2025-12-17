package com.abdownloadmanager.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation

@Composable
fun ObserveUiVisibility(
    onVisibilityChange: (isVisible: Boolean) -> Unit,
) {
    val onVisibilityChange by rememberUpdatedState(onVisibilityChange)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                onVisibilityChange(true)
                awaitCancellation()
            } finally {
                onVisibilityChange(false)
            }
        }
    }
}

@Composable
fun rememberIsUiVisible(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentState by lifecycleOwner.lifecycle.currentStateAsState()
    return currentState.isAtLeast(Lifecycle.State.RESUMED)
}
