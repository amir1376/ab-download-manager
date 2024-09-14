package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.utils.compose.widget.MyIcon
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun IntTextField(
    value: Int, onValueChange: (Int) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    range: ClosedRange<Int>,
    modifier: Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions,
    prettify: (Int) -> String = { it.toString() },
    placeholder: String = "",
) {
    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        enc = {
            value + it
        },
        toValue = {
            it.toIntOrNull()
        },
        prettify = prettify,
        fromValue = {
            it.toString()
        },
        range = range,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        placeholder = placeholder,
    )
}

@Composable
fun LongTextField(
    value: Long,
    onValueChange: (Long) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    range: ClosedRange<Long>,
    modifier: Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = "",
) {
    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        enc = {
            value + it
        },
        toValue = {
            it.toLongOrNull()
        },
        fromValue = {
            it.toString()
        },
        range = range,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Decimal
        ),
        interactionSource = interactionSource,
        placeholder = placeholder,
    )
}

@Composable
fun DoubleTextField(
    value: Double,
    onValueChange: (Double) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    range: ClosedRange<Double>,
    modifier: Modifier,
    unit: Double = 0.5,
    prettify: (Double) -> String = {
        it.toString()
    },
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = ""
) {
    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        enc = {
            value + it * unit
        },
        toValue = {
            it.toDoubleOrNull()
        },
        fromValue = prettify,
        range = range,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Decimal
        ),
        interactionSource = interactionSource,
        placeholder = placeholder
    )
}

@Composable
fun FloatTextField(
    value: Float,
    onValueChange: (Float) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    range: ClosedRange<Float>,
    modifier: Modifier,
    unit: Float = 0.5f,
    enabled: Boolean = true,
    prettify: (Float) -> String = { it.toString() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = "",
) {
    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        enc = {
            value + it * unit
        },
        toValue = {
            it.toFloat()
        },
        fromValue = prettify,
        range = range,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Decimal
        ),
        interactionSource = interactionSource,
        placeholder = placeholder
    )
}

//a null symbol used by NumberTextField
private val NULL = Any()

@Composable
fun <T : Comparable<T>> NumberTextField(
    value: T,
    onValueChange: (T) -> Unit,
    enc: (unit: Int) -> T,
    toValue: (String) -> T?,
    fromValue: (T) -> String,
    prettify: (T) -> String = fromValue,
    range: ClosedRange<T>,
    modifier: Modifier,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: String = "",
) {

    val value by rememberUpdatedState(value)
    val shape = RectangleShape
    val isFocused by interactionSource.collectIsFocusedAsState()
    var haveWrongValue by remember(value) {
        mutableStateOf(false)
    }
    var myText by remember {
        mutableStateOf("")
    }
    var lastEmittedValueByMe by remember {
        mutableStateOf(NULL as Any?)
    }
    // we observe new values here
    // we want to check who is changing that value
    // if lastEmittedValueByMe == value then we do that,
    // so we can stop prettifying that value until focus gone
    // this logic depends on [set] function [prettify] parameter
    // if it set lastEmittedByMe to null then it will recall prettify
    // another purpose of this logic is to handle new value that are coming in the composable
    LaunchedEffect(value) {
        if (lastEmittedValueByMe != value) {
            myText = prettify(value)
        }
    }
    fun set(v:T,prettify: Boolean):Boolean{
        val isInRange = v in range
        val valueInRange = if (isInRange) v else v.coerceIn(range)
        lastEmittedValueByMe = if (prettify || !isInRange) {
            NULL
        } else {
            valueInRange
        }
        onValueChange(valueInRange)
        return isInRange
    }
    LaunchedEffect(isFocused, haveWrongValue) {
        if (!isFocused) {
            if (haveWrongValue) {
                set(range.start, true)
            } else {
                myText = prettify(value)
            }
        }
    }
    MyTextField(
        textPadding = PaddingValues(4.dp),
        shape = shape,
        modifier = modifier.onKeyEvent {
            when (it.key) {
                Key.DirectionUp -> {
                    set(enc(1), true)
                    true
                }

                Key.DirectionDown -> {
                    set(enc(-1), true)
                    true
                }

                else -> {
                    false
                }
            }
        },
        placeholder = placeholder,
        text = myText,
        onTextChange = {
            if (it.isBlank()) {
                myText = ""
                haveWrongValue = true
                return@MyTextField
            }
            val v = toValue(it)
            if (v != null) {
                if (v == value) {
                    //only update text (not prettify until focus lost
                    myText = it
                } else {
                    val wasInRange = set(v, false)
                    if (wasInRange){
                        myText = it
                    }
                }
            }
        },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        end = {
            VerticalDirectionHandle({
                set(it, true)
            }, enc, enabled)
        }
    )
}

@Composable
private fun <T : Comparable<T>> VerticalDirectionHandle(
    onValueChange: (T) -> Unit,
    enc: (unit: Int) -> T,
    enabled: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragging by interactionSource.collectIsDraggedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    WithContentAlpha(
        animateFloatAsState(if (isDragging || isHovered) 1f else 0.5f).value
    ) {
        MyIcon(
            MyIcons.verticalDirection,
            null,
            Modifier
                .ifThen(enabled) {
                    hoverable(interactionSource)
                        .resizeHandle(
                            Orientation.Vertical, interactionSource
                        ) {
                            val times = it.value.toInt()
                            //we reverse this as Y is top to down
                            onValueChange(enc(-times))
                        }
                }
                .padding(end = 8.dp)
                .size(16.dp),
        )
    }
}