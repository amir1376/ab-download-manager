package com.abdownloadmanager.shared.ui.util

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import java.awt.Window

val LocalWindow: ProvidableCompositionLocal<Window> = compositionLocalOf {
    error("LocalWindow not provided yet")
}
