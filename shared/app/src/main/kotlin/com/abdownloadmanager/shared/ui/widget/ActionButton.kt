package com.abdownloadmanager.shared.ui.widget

import androidx.compose.foundation.LocalIndication
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import com.abdownloadmanager.shared.utils.ui.WithContentColor
import com.abdownloadmanager.shared.utils.div
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.theme.myShapes

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    backgroundColor: Brush = SolidColor(myColors.surface),
    disabledBackgroundColor: Brush = SolidColor(myColors.surface / 0.5f),
    borderColor: Brush = SolidColor(myColors.onBackground / 10),
    focusedBorderColor: Brush = SolidColor(myColors.focusedBorderColor),
    disabledBorderColor: Brush = SolidColor(myColors.onBackground / 10),
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(vertical = 6.dp, horizontal = 24.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    start: (@Composable RowScope.() -> Unit)? = null,
    end: (@Composable RowScope.() -> Unit)? = null,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = myShapes.defaultRounded
    val borderColor = if (isFocused) focusedBorderColor else borderColor
    Row(
        modifier
            .border(1.dp, if (enabled) borderColor else disabledBorderColor, shape)
            .clip(shape)
            .background(if (enabled) backgroundColor else disabledBackgroundColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
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
