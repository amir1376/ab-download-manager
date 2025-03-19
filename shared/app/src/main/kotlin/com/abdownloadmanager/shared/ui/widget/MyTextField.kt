package com.abdownloadmanager.shared.ui.widget

import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.LocalTextStyle
import com.abdownloadmanager.shared.utils.ui.myColors
import ir.amirab.util.ifThen
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse

@Composable
fun MyTextField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    background: Color = myColors.surface,
    contentColor: Color = myColors.getContentColorFor(background).takeIf { it.isSpecified }
        ?: LocalContentColor.current,
    focusedBorderColor: Color = myColors.primary,
    borderColor: Color = myColors.onBackground / 0.1f,
    shape: Shape = RoundedCornerShape(12.dp),
    textPadding: PaddingValues = PaddingValues(8.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    fontSize: TextUnit = TextUnit.Unspecified,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    start: @Composable (RowScope.() -> Unit)? = null,
    end: @Composable (RowScope.() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val fm = LocalFocusManager.current
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textSize = fontSize.takeOrElse { LocalTextStyle.current.fontSize }
    Row(
        modifier
            .ifThen(!enabled) {
                alpha(0.5f)
            }
            .clip(shape)
            .height(IntrinsicSize.Max)
//            .height(32.dp)
            .pointerHoverIcon(
                if (enabled) PointerIcon.Text
                else PointerIcon.Default
            )
            .onKeyEvent {
                if (it.key == Key.Escape) {
                    fm.clearFocus()
                    true
                } else false
            }
            .onClick {
                focusRequester.requestFocus()
            }
            .border(
                1.dp,
                animateColorAsState(
                    if (isFocused) focusedBorderColor
                    else borderColor
                ).value,
                shape
            )
            .background(background),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        start?.let {
            it()
        }

        BasicTextField(
            value = text,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            onValueChange = onTextChange,
            interactionSource = interactionSource,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .padding(textPadding)
                .focusRequester(focusRequester),
            textStyle = LocalTextStyle.current.merge(
                TextStyle(
                    color = LocalContentColor.current.ifThen(!enabled) {
                        copy(0.5f)
                    },
                    fontSize = fontSize
                )
            ),
            decorationBox = {
                Box {
                    androidx.compose.animation.AnimatedVisibility(
                        text.isEmpty(),
//                modifier = Modifier.matchParentSize(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Text(
                            text = placeholder,
                            maxLines = 1,
                            color = contentColor / 50,
                            fontSize = textSize
                        )
                    }
                    it()
                }
            },
            cursorBrush = SolidColor(myColors.primary),
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
        )
        end?.let {
            it()
        }
    }
}
