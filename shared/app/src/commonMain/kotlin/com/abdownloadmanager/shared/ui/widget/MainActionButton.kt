package com.abdownloadmanager.shared.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors


@Composable
fun PrimaryMainActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val backgroundColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 30
        }
    )
    val borderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors
    )
    val disabledBorderColor = Brush.horizontalGradient(
        myColors.primaryGradientColors.map {
            it / 50
        }
    )
    ActionButton(
        text = text,
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        backgroundColor = backgroundColor,
        disabledBackgroundColor = backgroundColor,
        borderColor = borderColor,
        disabledBorderColor = disabledBorderColor,
    )
}
