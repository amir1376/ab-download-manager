package com.abdownloadmanager.shared.util.ui.widget

import androidx.compose.runtime.Composable

@Composable
actual fun MPBackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    // improvements: hook to [escape] button using onKeyEvent and trigger on back here
}
