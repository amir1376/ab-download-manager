package com.abdownloadmanager.desktop.pages.batchdownload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.addDownload.single.MyTextFieldIcon
import com.abdownloadmanager.desktop.pages.batchdownload.WildcardSelect.*
import com.abdownloadmanager.desktop.ui.customwindow.WindowTitle
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.util.ifThen
import com.abdownloadmanager.desktop.ui.widget.*
import com.abdownloadmanager.desktop.utils.ClipboardUtil
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.utils.compose.LocalContentColor
import com.abdownloadmanager.utils.compose.WithContentAlpha
import com.abdownloadmanager.utils.compose.widget.MyIcon
import ir.amirab.util.compose.IconSource

@Composable
fun BatchDownload(
    component: BatchDownloadComponent,
) {
    WindowTitle("Batch Download")
    val link by component.link.collectAsState()
    val setLink = component::setLink
    val start by component.start.collectAsState()
    val setStart = component::setStart
    val end by component.end.collectAsState()
    val setEnd = component::setEnd
    val scrollState = rememberScrollState()
    val scrollAdapter = rememberScrollbarAdapter(scrollState)
    val validationResult by component.validationResult.collectAsState()
    val linkFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        linkFocusRequester.requestFocus()
    }
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                LabeledContent(
                    label = {
                        Text("Enter a link that contains wildcards (use *)")
                    },
                    content = {
                        BatchDownloadPageTextField(
                            text = link,
                            onTextChange = setLink,
                            placeholder = "Link: https://example.com/photo-*.png",
                            modifier = Modifier
                                .focusRequester(linkFocusRequester)
                                .fillMaxWidth(),
                            start = {
                                MyTextFieldIcon(MyIcons.link)
                            },
                            end = {
                                MyTextFieldIcon(MyIcons.paste, {
                                    val v = ClipboardUtil.read()
                                    if (v != null) {
                                        setLink(v)
                                    }
                                })
                            },
                            errorText = when (val v = validationResult) {
                                BatchDownloadValidationResult.URLInvalid -> {
                                    "Invalid URL"
                                }

                                is BatchDownloadValidationResult.MaxRangeExceed -> "List is too large! maximum ${v.allowed} items allowed"
                                BatchDownloadValidationResult.Others -> null
                                BatchDownloadValidationResult.Ok -> null
                            }
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
                LabeledContent(
                    label = {
                        Text("Enter range")
                    },
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BatchDownloadPageTextField(
                                text = start,
                                onTextChange = setStart,
                                placeholder = "",
                                modifier = Modifier.width(90.dp),
                                start = {
                                    Text("From:", Modifier.padding(horizontal = 8.dp))
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("...")
                            Spacer(Modifier.width(8.dp))

                            BatchDownloadPageTextField(
                                text = end,
                                onTextChange = setEnd,
                                placeholder = "",
                                modifier = Modifier.width(90.dp),
                                start = {
                                    Text("To:", Modifier.padding(horizontal = 8.dp))
                                }
                            )
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                LabeledContent(
                    label = {
                        Text("Wildcard length")
                    },
                    content = {
                        WildcardLengthUi(
                            component.wildcardLength.collectAsState().value,
                            component::setWildCardLength
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lineModifier = Modifier
                        .height(1.dp)
                        .padding(horizontal = 5.dp)
                        .background(LocalContentColor.current.copy(0.05f))

                    Spacer(Modifier.padding(vertical = 4.dp).fillMaxWidth().then(lineModifier))
                }
                Spacer(Modifier.height(8.dp))
                LabeledContent(
                    label = {
                        Text("First Link")
                    },
                    content = {
                        LinkPreview(component.startLinkResult.collectAsState().value)
                    }
                )
                Spacer(Modifier.height(8.dp))
                LabeledContent(
                    label = {
                        Text("Last Link")
                    },
                    content = {
                        LinkPreview(component.endLinkResult.collectAsState().value)
                    }
                )
            }
            VerticalScrollbar(scrollAdapter, Modifier.fillMaxHeight())
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
        ) {
            ActionButton(
                text = "OK",
                enabled = component.canConfirm.collectAsState().value,
                onClick = component::confirm
            )
            Spacer(Modifier.width(8.dp))
            ActionButton("Cancel", onClick = component.onClose)
        }
    }
}

@Composable
fun LinkPreview(link: String) {
    Text(
        link,
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(myColors.surface)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    )
}

enum class WildcardSelect {
    Auto, Unspecified, Custom;

    companion object {
        fun fromWildcardLength(wildcardLength: WildcardLength): WildcardSelect {
            return when (wildcardLength) {
                WildcardLength.Auto -> Auto
                is WildcardLength.Custom -> Custom
                WildcardLength.Unspecified -> Unspecified
            }
        }
    }
}

@Composable
private fun WildcardLengthUi(
    wildcardLength: WildcardLength,
    onChangeWildcardLength: (WildcardLength) -> Unit,
) {
    var customLength by remember {
        mutableStateOf(2)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Multiselect(
            selections = entries,
            selectedItem = WildcardSelect.fromWildcardLength(wildcardLength),
            onSelectionChange = {
                onChangeWildcardLength(
                    when (it) {
                        Auto -> WildcardLength.Auto
                        Unspecified -> WildcardLength.Unspecified
                        Custom -> WildcardLength.Custom(customLength)
                    }
                )
            },
            render = {
                Text(it.toString())
            }
        )
        AnimatedVisibility(wildcardLength is WildcardLength.Custom) {
            Row {
                Spacer(Modifier.width(8.dp))
                IntTextField(
                    value = customLength,
                    onValueChange = {
                        customLength = it
                        onChangeWildcardLength(
                            WildcardLength.Custom(it)
                        )
                    },
                    range = 1..10,
                    keyboardOptions = KeyboardOptions.Default,
                    modifier = Modifier.width(72.dp)
                )
            }
        }
    }
}

@Composable
private fun LabeledContent(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        label()
        Spacer(Modifier.height(8.dp))
        content()
    }
}


@Composable
private fun BatchDownloadPageTextField(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    errorText: String? = null,
    start: @Composable() (() -> Unit)? = null,
    end: @Composable() (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val dividerModifier = Modifier
        .fillMaxHeight()
        .padding(vertical = 1.dp)
        //to not conflict with text-field border
        .width(1.dp)
        .background(if (isFocused) myColors.onBackground / 10 else Color.Transparent)
    Column(modifier) {
        MyTextField(
            text,
            onTextChange,
            placeholder,
            modifier = Modifier.fillMaxWidth(),
            background = myColors.surface / 50,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(6.dp),
            start = start?.let {
                {
                    WithContentAlpha(0.5f) {
                        it()
                    }
                    Spacer(dividerModifier)
                }
            },
            end = end?.let {
                {
                    Spacer(dividerModifier)
                    it()
                }
            }
        )
        AnimatedVisibility(errorText != null) {
            if (errorText != null) {
                Text(
                    errorText,
                    Modifier.padding(bottom = 4.dp, start = 4.dp),
                    fontSize = myTextSizes.sm,
                    color = myColors.error,
                )
            }
        }
    }
}

@Composable
private fun MyTextFieldIcon(
    icon: IconSource,
    onClick: (() -> Unit)? = null,
) {
    MyIcon(icon, null, Modifier
        .fillMaxHeight()
        .ifThen(onClick != null) {
            pointerHoverIcon(PointerIcon.Default)
                .clickable { onClick?.invoke() }
        }
        .wrapContentHeight()
        .padding(horizontal = 8.dp)
        .size(16.dp))
}