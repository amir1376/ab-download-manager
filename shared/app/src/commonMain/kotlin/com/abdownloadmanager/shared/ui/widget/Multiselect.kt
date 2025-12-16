package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings

@Composable
fun <T> Multiselect(
    selections: List<T>,
    selectedItem: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = myShapes.defaultRounded,
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
                    .heightIn(mySpacings.thumbSize)
                    .clip(shape)
                    .ifThen(isSelected) {
                        background(selectedColor)
                    }
                    .clickable {
                        onSelectionChange(item)
                    }
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
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
