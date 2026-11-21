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
import com.abdownloadmanager.shared.pages.batchdownload.BatchDownloadMode
import com.abdownloadmanager.shared.pages.batchdownload.BatchDownloadValidationResult
import com.abdownloadmanager.shared.pages.batchdownload.WildcardLength
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
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
    val mode by component.mode.collectAsState()
    val scrollState = rememberScrollState()
    val validationResult by component.validationResult.collectAsState()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Multiselect(
                    selections = BatchDownloadMode.entries,
                    selectedItem = mode,
                    onSelectionChange = component::setMode,
                    render = {
                        Text(
                            when (it) {
                                BatchDownloadMode.UrlList -> myStringResource(Res.string.batch_download_mode_links)
                                BatchDownloadMode.Wildcard -> myStringResource(Res.string.batch_download_mode_wildcard)
                            }
                        )
                    }
                )
            }
            Spacer(Modifier.height(12.dp))

            VerticalScrollableContent(
                scrollState,
                modifier = Modifier.weight(1f, false),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                ) {
                    when (mode) {
                        BatchDownloadMode.UrlList -> {
                            AndroidUrlListSection(component, validationResult)
                        }
                        BatchDownloadMode.Wildcard -> {
                            AndroidWildcardSection(component, validationResult)
                        }
                    }
                    AndroidBatchSubFolderSection(component)
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
private fun AndroidUrlListSection(
    component: AndroidBatchDownloadComponent,
    validationResult: BatchDownloadValidationResult,
) {
    val urlsText by component.urlsText.collectAsState()
    val parsedUrls by component.parsedUrls.collectAsState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LabeledContent(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(myStringResource(Res.string.links))
                ActionButton(
                    text = myStringResource(Res.string.paste),
                    onClick = {
                        val v = ClipboardUtil.read()
                        if (v != null) {
                            component.setUrlsText(v)
                        }
                    },
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                )
            }
        },
        content = {
            MyTextField(
                text = urlsText,
                onTextChange = component::setUrlsText,
                placeholder = myStringResource(Res.string.batch_download_urls_placeholder),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .height(130.dp),
                singleLine = false,
                minLines = 4,
                maxLines = 10,
            )
        }
    )

    Spacer(Modifier.height(8.dp))

    when (validationResult) {
        is BatchDownloadValidationResult.MaxRangeExceed -> {
            Text(
                myStringResource(
                    Res.string.list_is_too_large_maximum_n_items_allowed,
                    Res.string.list_is_too_large_maximum_n_items_allowed_createArgs(
                        count = validationResult.allowed.toString()
                    )
                ),
                color = myColors.error,
                fontSize = myTextSizes.sm,
            )
        }
        BatchDownloadValidationResult.NoUrlsFound -> {
            Text(
                myStringResource(Res.string.batch_download_no_links_detected),
                color = myColors.error,
                fontSize = myTextSizes.sm,
            )
        }
        else -> {}
    }

    if (parsedUrls.isNotEmpty()) {
        Text(
            myStringResource(
                Res.string.batch_download_detected_links_count,
                Res.string.batch_download_detected_links_count_createArgs(
                    count = parsedUrls.size.toString()
                )
            ),
            color = myColors.success,
            fontSize = myTextSizes.sm,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(Modifier.height(4.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(myShapes.defaultRounded)
                .background(myColors.surface)
                .padding(8.dp)
        ) {
            val previewItems = parsedUrls.take(4)
            for (u in previewItems) {
                Text(
                    u,
                    maxLines = 1,
                    fontSize = myTextSizes.sm,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            if (parsedUrls.size > 4) {
                Text(
                    "... +${parsedUrls.size - 4} ${myStringResource(Res.string.links).lowercase()}",
                    fontSize = myTextSizes.xs,
                    color = LocalContentColor.current.copy(0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AndroidBatchSubFolderSection(
    component: AndroidBatchDownloadComponent,
) {
    val saveInSubFolder by component.saveInSubFolder.collectAsState()
    val subFolder by component.subFolder.collectAsState()

    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(myShapes.defaultRounded)
            .clickable { component.setSaveInSubFolder(!saveInSubFolder) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBox(
            value = saveInSubFolder,
            onValueChange = component::setSaveInSubFolder,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            myStringResource(Res.string.batch_download_save_in_subfolder),
            fontSize = myTextSizes.base,
        )
    }

    AnimatedVisibility(visible = saveInSubFolder) {
        Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            MyTextField(
                text = subFolder,
                onTextChange = component::setSubFolder,
                placeholder = myStringResource(Res.string.batch_download_subfolder_placeholder),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun AndroidWildcardSection(
    component: AndroidBatchDownloadComponent,
    validationResult: BatchDownloadValidationResult,
) {
    val link by component.link.collectAsState()
    val setLink = component::setLink
    val start by component.start.collectAsState()
    val setStart = component::setStart
    val end by component.end.collectAsState()
    val setEnd = component::setEnd
    val linkFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        linkFocusRequester.requestFocus()
    }

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

                    else -> null
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

@Composable
fun LinkPreview(link: String) {
    Text(
        link,
        Modifier
            .fillMaxWidth()
            .clip(myShapes.defaultRounded)
            .border(1.dp, myColors.onSurface / 0.1f, myShapes.defaultRounded)
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
