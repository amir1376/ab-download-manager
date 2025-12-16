package com.abdownloadmanager.desktop.pages.home.sections

import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun SearchBox(
    text: String,
    onTextChange: (String) -> Unit,
    textPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: String = myStringResource(Res.string.search_in_the_list),
    modifier: Modifier,
) {
    val shape = myShapes.defaultRounded
    val textSize = myTextSizes.base
    MyTextField(
        text = text,
        fontSize = textSize,
        onTextChange = onTextChange,
        shape = shape,
        textPadding = textPadding,
        interactionSource = interactionSource,
        start = {
            WithContentAlpha(
                animateFloatAsState(if (text.isBlank()) 0.9f else 1f).value
            ) {
                MyIcon(
                    MyIcons.search,
                    myStringResource(Res.string.search),
                    Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        },
        end = {
            AnimatedContent(text.isNotBlank()) {
                MyIcon(
                    MyIcons.clear,
                    myStringResource(Res.string.clear),
                    Modifier
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .pointerHoverIcon(PointerIcon.Default)
                        .clickable(
                            enabled = it,
                            onClick = {
                                onTextChange("")
                            }
                        )
                        .padding(4.dp)
                        .size(16.dp)
                    ,
                    tint = myColors.error.copy(
                        alpha = if (it) 1f else 0f
                    )
                )
            }
        },
        modifier = modifier,
        placeholder = placeholder
    )
}
