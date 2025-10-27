package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.util.ui.theme.MyShapes
import com.abdownloadmanager.shared.util.ui.theme.MySpacings
import com.abdownloadmanager.shared.util.ui.theme.TextSizes
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle

@Composable
expect fun myPlatformScrollbarStyle(): ScrollbarStyle

@Composable
expect fun myPlatformTextSizes(): TextSizes

@Composable
expect fun myPlatformShapes(): MyShapes

@Composable
expect fun myPlatformSpacing(): MySpacings

@Composable
expect fun PlatformDependentProviders(content: @Composable () -> Unit)
