package com.abdownloadmanager.android.pages.singledownload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import com.abdownloadmanager.shared.ui.configurable.RenderConfigurable
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.abdownloadmanager.shared.ui.widget.rememberMyComponentRectPositionProvider
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.singledownloadpage.SingleDownloadPagePropertyItem
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.util.LocalSizeUnit
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.ui.useIsInDebugMode
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.VerticalScrollableContent
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.*
import ir.amirab.downloader.part.PartDownloadStatus
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

enum class SingleDownloadPageSections(
    val title: StringSource,
    val icon: IconSource,
) {
    Info(
        Res.string.info.asStringSource(),
        MyIcons.info
    ),
    Settings(
        Res.string.speed.asStringSource(),
        MyIcons.fast,
    ),
    OnCompletion(
        Res.string.on_completion.asStringSource(),
        MyIcons.flag
    ),
}

private val tabs = SingleDownloadPageSections.entries.toList()

@Composable
fun ProgressDownloadPage(
    singleDownloadComponent: AndroidSingleDownloadComponent,
    itemState: ProcessingDownloadItemState
) {
    var selectedTab by remember { mutableStateOf(SingleDownloadPageSections.Info) }
    val showPartInfo by singleDownloadComponent.showPartInfo.collectAsState()
    val setShowPartInfo = singleDownloadComponent::setShowPartInfo
    val horizontalPadding = 16.dp

    Column {
        Column(
            Modifier
                .clip(myShapes.defaultRounded)
                .padding(1.dp),
        ) {
            val scrollState = rememberScrollState()
            //info / settings ...
            val tabContentModifier = Modifier
            VerticalScrollableContent(
                scrollState,
            ) {
                Box(
                    Modifier
                        .animateContentSize()
//                        .height(150.dp)
                        .verticalScroll(scrollState)
                ) {
                    when (selectedTab) {
                        SingleDownloadPageSections.Info -> RenderInfo(
                            tabContentModifier,
                            horizontalPadding,
                            singleDownloadComponent,
                        )

                        SingleDownloadPageSections.Settings -> RenderSettings(
                            modifier = tabContentModifier,
                            horizontalPadding = horizontalPadding,
                            singleDownloadComponent = singleDownloadComponent,
                        )

                        SingleDownloadPageSections.OnCompletion -> RenderOnCompletion(
                            modifier = tabContentModifier,
                            horizontalPadding = horizontalPadding,
                            singleDownloadComponent = singleDownloadComponent,
                        )
                    }
                }
            }
        }
        //tabs
        MyTabRow {
            for (tab in tabs) {
                MyTab(
                    selected = tab == selectedTab,
                    onClick = {
                        selectedTab = tab
                    },
                    icon = tab.icon,
                    title = tab.title,
                    selectionBackground = Color.Transparent
                )
            }
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Column(
            Modifier
                .background(myColors.surface / 0.5f)
        ) {
            Column(
                Modifier
                    .padding(horizontal = horizontalPadding)
            ) {
                Spacer(Modifier.size(8.dp))
                RenderProgressBar(itemState)
                Spacer(Modifier.size(8.dp))
                RenderParts(
                    itemState.parts,
                    Modifier
                        .height(4.dp)
                        .clip(myShapes.defaultRounded)
                        .background(myColors.onBackground / 15)
                )
                Spacer(Modifier.size(mySpacings.largeSpace))
                RenderActions(itemState, singleDownloadComponent, showPartInfo, setShowPartInfo)
                Spacer(Modifier.size(mySpacings.largeSpace))
            }
            AnimatedVisibility(showPartInfo) {
                RenderPartInfo(
                    modifier = Modifier.height(240.dp),
                    itemState = itemState,
                    horizontalPadding = horizontalPadding,
                )
            }
        }
    }
}


@Composable
private fun RenderSettings(
    modifier: Modifier,
    horizontalPadding: Dp,
    singleDownloadComponent: AndroidSingleDownloadComponent,
) {
    Column(modifier) {
        for (configurable in singleDownloadComponent.settings) {
            RenderConfigurable(
                configurable,
                ConfigurableUiProps(
                    modifier = Modifier,
                    itemPaddingValues = PaddingValues(
                        horizontal = horizontalPadding,
                    )
                )
            )
        }
    }
}

@Composable
private fun RenderOnCompletion(
    modifier: Modifier,
    horizontalPadding: Dp,
    singleDownloadComponent: AndroidSingleDownloadComponent,
) {
    Column(modifier) {
        for (configurable in singleDownloadComponent.onCompletion) {
            RenderConfigurable(
                configurable,
                ConfigurableUiProps(
                    modifier = Modifier,
                    itemPaddingValues = PaddingValues(
                        horizontal = horizontalPadding,
                    )
                )
            )
        }
    }
}


@Composable
private fun RenderProgressBar(itemState: IDownloadItemState) {
    val progress = when (itemState) {
        is CompletedDownloadItemState -> 100
        is ProcessingDownloadItemState -> when (val status = itemState.status) {
            is DownloadJobStatus.PreparingFile -> status.percent
            else -> itemState.percent
        }
    }?.let {
        it / 100f
    }

    val status = itemState.statusOrFinished()
    val background = when (status) {
        is DownloadJobStatus.Finished -> myColors.successGradient
        is DownloadJobStatus.Canceled -> if (ExceptionUtils.isNormalCancellation(status.e)) {
            myColors.warningGradient
        } else {
            myColors.errorGradient
        }

        DownloadJobStatus.IDLE -> myColors.warningGradient
        is DownloadJobStatus.Retrying -> myColors.errorGradient
        DownloadJobStatus.Finished -> myColors.successGradient
        is DownloadJobStatus.PreparingFile -> myColors.infoGradient
        DownloadJobStatus.Resuming,
        DownloadJobStatus.Downloading,
            -> myColors.primaryGradient
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(myShapes.defaultRounded)
            .height(14.dp)
            .background(myColors.onBackground / 15)
    ) {
        progress?.let { progress ->
            Box(
                Modifier
                    .background(background)
                    .fillMaxHeight()
                    .fillMaxWidth(
                        animateFloatAsState(
                            progress,
                            tween(100, easing = LinearEasing)
                        ).value
                    )
            ) {
                if (progress == 1f) {
                    MyIcon(
                        MyIcons.check,
                        null,
                        Modifier
                            .padding(1.dp)
                            .clip(CircleShape)
                            .background(myColors.onBackground)
                            .padding(1.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd),
                        tint = myColors.background,
                    )
                }
            }
        }
        if (progress == null && status is DownloadJobStatus.IsActive) {
            val anim = rememberInfiniteTransition()
            val l = 2000
            val endPos by anim.animateFloat(
                0f,
                1f,
                infiniteRepeatable(tween(l), RepeatMode.Restart)
            )
            val width by anim.animateFloat(
                6f, 16f, infiniteRepeatable(
                    keyframes {
                        durationMillis = l
                        0f atFraction 0f
                        0.75f atFraction 0.25f
                        0f atFraction 1f
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(endPos)
            ) {
                Box(
                    Modifier
                        .background(background)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(width)
                )
            }
        }
    }
}

@Composable
private fun RenderPartInfo(
    modifier: Modifier,
    itemState: ProcessingDownloadItemState,
    horizontalPadding: Dp,
) {
    Column(modifier) {
        Column(
            Modifier.weight(1f)
        ) {
            Box(
                Modifier.weight(1f)
            ) {
                val (onlyActiveParts, setOnlyActiveParts) = rememberSaveable {
                    mutableStateOf(true)
                }
                val listToShow = remember(itemState, onlyActiveParts) {
                    itemState.parts
                        .let { parts ->
                            if (onlyActiveParts) {
                                parts.filter {
                                    when (it.status) {
                                        is PartDownloadStatus.Canceled -> true
                                        PartDownloadStatus.Completed -> false
                                        PartDownloadStatus.IDLE -> false
                                        PartDownloadStatus.ReceivingData -> true
                                        PartDownloadStatus.Connecting -> true
                                    }
                                }
                            } else {
                                parts
                            }
                        }
                        .withIndex()
                        .toList()
                }
                LazyColumn(
                    Modifier.fillMaxSize(),
                    state = rememberLazyListState()
                ) {
                    items(listToShow, key = { it.value.id }) { item ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(vertical = 4.dp)
                        ) {
                            RenderSinglePart(
                                index = item.index + 1,
                                part = item.value,
                                size = listToShow.size
                            )
                        }
                    }
                }
                if (useIsInDebugMode()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp, end = 8.dp)
                            .clickable { setOnlyActiveParts(!onlyActiveParts) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Only Actives")
                        Spacer(Modifier.width(4.dp))
                        CheckBox(onlyActiveParts, { setOnlyActiveParts(it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderSinglePart(
    index: Int,
    part: UiPart,
    size: Int,
) {
    val sizeStringLength = size.toString().length
    Row {
        Text(
            index.toString().padStart(sizeStringLength, '0'),
            color = LocalContentColor.current / 0.5f,
            modifier = Modifier,
        )
        Spacer(Modifier.width(mySpacings.mediumSpace))
        Text(
            prettifyStatus(part.status).rememberString(),
            color = LocalContentColor.current / 0.75f,
            modifier = Modifier.weight(1f),
        )
        val progress = convertPositiveSizeToHumanReadable(
            part.howMuchProceed,
            LocalSizeUnit.current
        ).rememberString()
        val total = part.length?.let { length ->
            convertPositiveSizeToHumanReadable(length, LocalSizeUnit.current).rememberString()
        } ?: myStringResource(Res.string.unknown)

        Text(
            "$progress / $total",
            color = LocalContentColor.current / 0.75f,
            modifier = Modifier
        )
    }
}

private fun prettifyStatus(status: PartDownloadStatus): StringSource {
    return when (status) {
        is PartDownloadStatus.Canceled -> Res.string.disconnected
        PartDownloadStatus.IDLE -> Res.string.idle
        PartDownloadStatus.Completed -> Res.string.finished
        PartDownloadStatus.ReceivingData -> Res.string.receiving_data
        PartDownloadStatus.Connecting -> Res.string.connecting
    }.asStringSource()
}


@Composable
private fun RenderPropertyItem(propertyItem: SingleDownloadPagePropertyItem) {
    val title = propertyItem.name
    val value = propertyItem.value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        WithContentAlpha(0.75f) {
            Text(
                text = "${title.rememberString()}:",
                modifier = Modifier.weight(0.3f),
                maxLines = 1,
                fontSize = myTextSizes.base
            )
        }
        WithContentAlpha(1f) {
            Text(
                text = value.rememberString(),
                modifier = Modifier
                    .basicMarquee(
                        iterations = Int.MAX_VALUE
                    )
                    .weight(0.7f),
                maxLines = 1,
                fontSize = myTextSizes.base,
                color = when (propertyItem.valueState) {
                    SingleDownloadPagePropertyItem.ValueType.Normal -> LocalContentColor.current
                    SingleDownloadPagePropertyItem.ValueType.Error -> myColors.error
                    SingleDownloadPagePropertyItem.ValueType.Success -> myColors.success
                }
            )
        }
    }
}

@Composable
private fun RenderInfo(
    modifier: Modifier,
    horizontalPadding: Dp,
    singleDownloadComponent: AndroidSingleDownloadComponent,
) {
    Column(
        modifier
            .padding(horizontal = horizontalPadding)
            .padding(top = 8.dp)
    ) {
        for (propertyItem in singleDownloadComponent.extraDownloadProgressInfo.collectAsState().value) {
            Spacer(Modifier.height(2.dp))
            RenderPropertyItem(propertyItem)
        }
    }
}

@Composable
private fun RenderActions(
    itemState: ProcessingDownloadItemState,
    singleDownloadComponent: AndroidSingleDownloadComponent,
    showingPartInfo: Boolean,
    onRequestShowPartInfo: (show: Boolean) -> Unit,
) {
    Row {
        PartInfoButton(showingPartInfo, onRequestShowPartInfo)
        Spacer(Modifier.width(8.dp))
        ToggleButton(
            itemState = itemState,
            toggle = singleDownloadComponent::toggle,
            pause = singleDownloadComponent::pause,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        CancelButton(
            cancel = singleDownloadComponent::cancel,
            icon = if (singleDownloadComponent.deletePartialFileOnDownloadCancellation.collectAsState().value) {
                MyIcons.stop
            } else {
                null
            },
            modifier = Modifier,
        )
    }
}

@Composable
private fun PartInfoButton(
    showing: Boolean,
    onClick: (Boolean) -> Unit,
) {
    val partsInfoTitle = Res.string.parts_info.asStringSource()
    Tooltip(partsInfoTitle) {
        IconActionButton(
            onClick = {
                onClick(!showing)
            },
            contentDescription = partsInfoTitle,
            icon = if (showing) {
                MyIcons.up
            } else {
                MyIcons.down
            }
        )
    }
}

@Composable
private fun SingleDownloadPageButton(
    onClick: () -> Unit,
    text: String,
    color: Color = LocalContentColor.current,
    icon: IconSource? = null,
    modifier: Modifier,
) {
    ActionButton(
        modifier = modifier,
        text = text,
        start = {
            icon?.let {
                Row {
                    MyIcon(it, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
            }
        },
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
        contentColor = color,
        onClick = onClick,
    )
}

@Composable
private fun CancelButton(
    cancel: () -> Unit,
    icon: IconSource?,
    modifier: Modifier,
) {
    SingleDownloadPageButton(
        {
            cancel()
        },
        icon = icon,
        text = myStringResource(Res.string.cancel),
        modifier = modifier,
    )
}

@Composable
private fun ToggleButton(
    itemState: ProcessingDownloadItemState,
    toggle: () -> Unit,
    pause: () -> Unit,
    modifier: Modifier,
) {
    var showPromptOnNonePresumablePause by remember(itemState.status is DownloadJobStatus.IsActive) {
        mutableStateOf(false)
    }

    val isResumeSupported = itemState.supportResume == true
    val (icon, text) = when (itemState.status) {
        is DownloadJobStatus.CanBeResumed -> {
            MyIcons.resume to Res.string.resume
        }

        is DownloadJobStatus.IsActive -> {
            MyIcons.pause to Res.string.pause
        }

        else -> return
    }

    Box(modifier) {
        SingleDownloadPageButton(
            {
                if (isResumeSupported) {
                    toggle()
                } else {
                    if (itemState.status is DownloadJobStatus.IsActive) {
                        showPromptOnNonePresumablePause = true
                    } else {
                        toggle()
                    }
                }
            },
            icon = icon,
            text = myStringResource(text),
            color = if (isResumeSupported) {
                LocalContentColor.current
            } else {
                if (itemState.status is DownloadJobStatus.IsActive) {
                    myColors.error
                } else {
                    LocalContentColor.current
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (showPromptOnNonePresumablePause) {
            val shape = myShapes.defaultRounded
            val closePopup = {
                showPromptOnNonePresumablePause = false
            }
            Popup(
                popupPositionProvider = rememberMyComponentRectPositionProvider(
                    offset = DpOffset.Zero,
                    anchor = Alignment.TopEnd,
                    alignment = Alignment.TopStart,
                ),
                onDismissRequest = closePopup
            ) {
                Column(
                    Modifier
                        .clip(shape)
                        .border(2.dp, myColors.onBackground / 10, shape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    myColors.surface,
                                    myColors.background,
                                )
                            )
                        )
                        .padding(16.dp)
                        .widthIn(max = 140.dp)
                ) {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(color = myColors.warning)) {
                            append("${myStringResource(Res.string.warning)}:\n")
                        }
                        append(myStringResource(Res.string.unsupported_resume_warning))
                    })
                    Spacer(Modifier.height(8.dp))
                    ActionButton(
                        myStringResource(Res.string.stop_anyway),
                        onClick = {
                            closePopup()
                            pause()
                        },
                        contentColor = myColors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderParts(parts: List<UiPart>, modifier: Modifier) {
    Row(
        modifier
            .fillMaxWidth()
    ) {
        if (parts.isNotEmpty()) {
            val sortedParts = remember(parts) {
                parts.sortedBy {
                    it.id
                }
            }
            for (p in sortedParts) {
                val partSpace = p.partSpace
                if (partSpace <= 0f) continue
                key(p.id) {
                    RenderPart(
                        p,
                        Modifier
                            .fillMaxHeight()
                            .weight(partSpace)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderPart(part: UiPart, modifier: Modifier) {
    val partProgress = part.percent?.let {
        it / 100f
    } ?: 0f

    val foregroundColor = when (part.status) {
        is PartDownloadStatus.Canceled -> myColors.error
        PartDownloadStatus.Completed -> myColors.info
        PartDownloadStatus.IDLE -> myColors.info / 25
        PartDownloadStatus.ReceivingData -> myColors.success
        PartDownloadStatus.Connecting -> myColors.warning
    }
    Row(modifier) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(partProgress)
                    .fillMaxHeight()
                    .background(foregroundColor)
            )
        }
    }
}
