package com.abdownloadmanager.desktop.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.utils.compose.WithContentAlpha

@Composable
fun <T> Multiselect(
    selections: List<T>,
    selectedItem: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
    backgroundColour: Color = myColors.surface,
    selectedColor: Color = LocalContentColor.current / 10,
    unselectedAlpha: Float = 0.5f,
    render: @Composable (T) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(backgroundColour)
    ) {
        for (item in selections) {
            val isSelected = item == selectedItem
            Box(
                Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .clip(shape)
                    .ifThen(isSelected) {
                        background(selectedColor)
                    }
                    .clickable {
                        onSelectionChange(item)
                    }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            ) {
                WithContentAlpha(
                    if (isSelected) {
                        1f
                    } else {
                        unselectedAlpha
                    }
                ) {
                    render(item)
                }
            }
        }
    }
}