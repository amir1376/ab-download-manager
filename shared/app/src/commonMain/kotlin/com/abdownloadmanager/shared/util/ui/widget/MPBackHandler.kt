package com.abdownloadmanager.shared.util.ui.widget

import androidx.compose.runtime.Composable

@Composable
expect fun MPBackHandler(
    isEnabled: Boolean = true,
    onBack: () -> Unit,
)
