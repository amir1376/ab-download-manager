package com.abdownloadmanager.shared.util.ui.widget

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun MPBackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = isEnabled, onBack = onBack)
}
