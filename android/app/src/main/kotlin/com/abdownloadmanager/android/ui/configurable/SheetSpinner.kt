package com.abdownloadmanager.android.ui.configurable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.defaultValueToString
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.MultiplatformVerticalScrollbar
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.ifThen
import kotlin.collections.set


@Composable
fun <T> RenderSpinnerInSheet(
    title: StringSource,
    isOpened: Boolean,
    onDismiss: () -> Unit,
    possibleValues: List<T>,
    value: T,
    onSelect: (T) -> Unit,
    valueToString: (T) -> List<String> = ::defaultValueToString,
//    minWidth:Dp,
    render: @Composable (T) -> Unit,
) {
    val verticalPadding = 4.dp
    val horizontalPadding = 16.dp

    val shape = myShapes.defaultRounded
    val borderWidth = 1.dp
    ConfigurableSheet(
        title = title,
        isOpened = isOpened,
        onDismiss = onDismiss,
        headerActions = {
            TransparentIconActionButton(
                MyIcons.close,
                contentDescription = myStringResource(Res.string.close),
                onClick = onDismiss
            )
        }
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        val possibleValuePositions = remember(possibleValues) {
            mutableStateMapOf<Int, Float>()
        }
        var itemToBeIndicated: Int by remember {
            mutableStateOf(-1)
        }
        val scrollState = rememberScrollState()
        VerticalScrollableContent(scrollState) {
            Column(
                Modifier
                    .focusRequester(focusRequester)
                    .clip(shape)
                    .verticalScroll(scrollState)
            ) {
                WithContentColor(myColors.onSurface) {
                    for ((index, p) in possibleValues.withIndex()) {
                        key(p) {
                            val isIndicating = itemToBeIndicated == index
                            Row(
                                modifier = Modifier
                                    .onGloballyPositioned {
                                        possibleValuePositions[index] = it.positionInParent().y
                                    }
                                    .ifThen(isIndicating) {
                                        background(
                                            myColors.onBackground / 0.05f
                                        )
                                    }
                                    .clickable(onClick = {
                                        onSelect(p)
                                    })
                                    .heightIn(mySpacings.thumbSize)
                                    .padding(horizontal = horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val selected = p == value
                                WithContentAlpha(if (selected) 1f else 0.75f) {
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .padding(vertical = verticalPadding)
                                    ) {
                                        render(p)
                                    }
                                }
                                Spacer(
                                    Modifier.width(borderWidth)
                                )
                                if (selected) {
                                    MyIcon(
                                        MyIcons.check, null, Modifier
                                            .padding(4.dp)
                                            .size(16.dp)
                                    )
                                }
                            }
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                myColors.onSurface / 0.05f,
                                                myColors.onSurface / 0.1f,
                                                myColors.onSurface / 0.05f,
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
