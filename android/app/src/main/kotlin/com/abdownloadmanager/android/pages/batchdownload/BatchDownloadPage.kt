package com.abdownloadmanager.android.pages.batchdownload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.batchdownload.BatchDownloadValidationResult
import com.abdownloadmanager.shared.pages.batchdownload.WildcardLength
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

@Composable
fun BatchDownloadSheet(
    component: AndroidBatchDownloadComponent?,
    onDismiss: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    state.OnFullyDismissed(onDismiss)
    LaunchedEffect(component) {
        if (component == null) {
            state.hide()
        } else {
            state.show()
        }
    }
    ResponsiveDialog(state, state::hide) {
        component?.let {
            BatchDownloadPage(component, state::hide)
        }
    }
}

@Composable
private fun ResponsiveDialogScope.BatchDownloadPage(
    component: AndroidBatchDownloadComponent,
    onDismiss: () -> Unit,
) {
    val link by component.link.collectAsState()
    val setLink = component::setLink
    val start by component.start.collectAsState()
    val setStart = component::setStart
    val end by component.end.collectAsState()
    val setEnd = component::setEnd
    val scrollState = rememberScrollState()
    val validationResult by component.validationResult.collectAsState()
    val linkFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        linkFocusRequester.requestFocus()
    }
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    SheetTitle(
                        myStringResource(Res.string.batch_download)
                    )
                }
            )
        }
    ) {
        Column(
            Modifier
                .padding(16.dp)
        ) {
            VerticalScrollableContent(
                scrollState,
                modifier = Modifier.weight(1f, false),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                ) {
                    LabeledContent(
                        label = {
                            Text(myStringResource(Res.string.batch_download_link_help))
                        },
                        content = {
                            MyTextFieldWithIcons(
                                text = link,
                                onTextChange = setLink,
                                placeholder = "https://example.com/photo-*.png",
                                modifier = Modifier
                                    .focusRequester(linkFocusRequester)
                                    .fillMaxWidth(),
                                start = {
                                    MyTextFieldIcon(MyIcons.link)
                                },
                                end = {
                                    MyTextFieldIcon(MyIcons.paste, onClick = {
                                        val v = ClipboardUtil.read()
                                        if (v != null) {
                                            setLink(v)
                                        }
                                    })
                                },
                                errorText = when (val v = validationResult) {
                                    BatchDownloadValidationResult.URLInvalid -> {
                                        myStringResource(Res.string.invalid_url)
                                    }

                                    is BatchDownloadValidationResult.MaxRangeExceed -> myStringResource(
                                        Res.string.list_is_too_large_maximum_n_items_allowed,
                                        Res.string.list_is_too_large_maximum_n_items_allowed_createArgs(
                                            count = v.allowed.toString()
                                        )
                                    )

                                    BatchDownloadValidationResult.Others -> null
                                    BatchDownloadValidationResult.Ok -> null
                                }
                            )
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    LabeledContent(
                        label = {
                            Text(myStringResource(Res.string.enter_range))
                        },
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MyTextFieldWithIcons(
                                    text = start,
                                    onTextChange = setStart,
                                    placeholder = "",
                                    modifier = Modifier.width(90.dp),
                                    start = {
                                        Text(
                                            "${myStringResource(Res.string.range_from)}:",
                                            Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("...")
                                Spacer(Modifier.width(8.dp))

                                MyTextFieldWithIcons(
                                    text = end,
                                    onTextChange = setEnd,
                                    placeholder = "",
                                    modifier = Modifier.width(90.dp),
                                    start = {
                                        Text(
                                            "${myStringResource(Res.string.range_to)}:",
                                            Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    LabeledContent(
                        label = {
                            Text(myStringResource(Res.string.batch_download_wildcard_length))
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

                        Spacer(
                            Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                                .then(lineModifier)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LabeledContent(
                        label = {
                            Text(myStringResource(Res.string.first_link))
                        },
                        content = {
                            LinkPreview(component.startLinkResult.collectAsState().value)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    LabeledContent(
                        label = {
                            Text(myStringResource(Res.string.last_link))
                        },
                        content = {
                            LinkPreview(component.endLinkResult.collectAsState().value)
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val buttonModifier = Modifier.weight(1f)
                ActionButton(
                    myStringResource(Res.string.close),
                    onClick = onDismiss,
                    modifier = buttonModifier,
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = myStringResource(Res.string.ok),
                    enabled = component.canConfirm.collectAsState().value,
                    onClick = component::confirm,
                    modifier = buttonModifier,
                )
            }
        }
    }

}

@Composable
fun LinkPreview(link: String) {
    Text(
        link,
        Modifier
            .fillMaxWidth()
            .clip(myShapes.defaultRounded)
            .border(1.dp, myColors.onSurface / 0.1f, myShapes.defaultRounded)
//            .background(myColors.surface)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    )
}

enum class WildcardSelect(
    val text: StringSource,
) {
    Auto(Res.string.auto.asStringSource()),
    Unspecified(Res.string.unspecified.asStringSource()),
    Custom(Res.string.custom.asStringSource());

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
        mutableIntStateOf(2)
    }
    FlowRow(
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Multiselect(
            selections = WildcardSelect.entries,
            selectedItem = WildcardSelect.fromWildcardLength(wildcardLength),
            onSelectionChange = {
                onChangeWildcardLength(
                    when (it) {
                        WildcardSelect.Auto -> WildcardLength.Auto
                        WildcardSelect.Unspecified -> WildcardLength.Unspecified
                        WildcardSelect.Custom -> WildcardLength.Custom(customLength)
                    }
                )
            },
            render = {
                Text(it.text.rememberString())
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
                    modifier = Modifier.width(96.dp)
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
