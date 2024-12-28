package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.desktop.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    backgroundColor: Brush = SolidColor(myColors.surface),
    disabledBackgroundColor: Brush = SolidColor(myColors.surface / 0.5f),
    borderColor: Brush = SolidColor(myColors.onBackground / 10),
    disabledBorderColor: Brush = SolidColor(myColors.onBackground / 10),
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(vertical = 6.dp, horizontal = 24.dp),
    start: (@Composable RowScope.() -> Unit)? = null,
    end: (@Composable RowScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .border(1.dp, if (enabled) borderColor else disabledBorderColor, shape)
            .clip(shape)
            .background(if (enabled) backgroundColor else disabledBackgroundColor)
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(contentPadding)
    ) {
        WithContentColor(contentColor) {
            WithContentAlpha(if (enabled) 1f else 0.5f) {
                start?.let {
                    it()
                }
                Text(
                    text = text,
                    modifier = Modifier,
                    fontSize = myTextSizes.base,
                    maxLines = 1,
                    softWrap = false,
                )
                end?.let {
                    it()
                }
            }
        }
    }
}