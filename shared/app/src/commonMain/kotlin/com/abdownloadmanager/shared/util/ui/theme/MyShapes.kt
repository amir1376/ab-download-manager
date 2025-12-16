package com.abdownloadmanager.shared.util.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp


val LocalMyShapes = staticCompositionLocalOf<MyShapes> {
    error("LocalMyShapes not provided")
}

val myShapes
    @Composable
    get() = LocalMyShapes.current

private val ZeroCornerSize = CornerSize(0.dp)

@Stable
data class MyShapes(
    val defaultRounded: RoundedCornerShape,
) {
    val bottomSheet = defaultRounded.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize,
    )
    val dialog = defaultRounded
    fun createSheetWithCustomEdges(
        topStart: Boolean,
        bottomStart: Boolean,
        topEnd: Boolean,
        bottomEnd: Boolean,
    ): RoundedCornerShape {
        return RoundedCornerShape(
            bottomStart = if (bottomStart) defaultRounded.bottomStart else ZeroCornerSize,
            bottomEnd = if (bottomEnd) defaultRounded.bottomEnd else ZeroCornerSize,
            topStart = if (topStart) defaultRounded.topStart else ZeroCornerSize,
            topEnd = if (topEnd) defaultRounded.topEnd else ZeroCornerSize,
        )
    }
}
