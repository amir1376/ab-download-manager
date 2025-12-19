package com.abdownloadmanager.android.util.compose

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable

@Composable
fun useBack(): OnBackPressedDispatcher? {
    return LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher
}
