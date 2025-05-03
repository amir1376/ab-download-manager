package com.abdownloadmanager.desktop.pages.singleDownloadPage

import com.abdownloadmanager.desktop.utils.configurable.RenderConfigurable
import com.abdownloadmanager.desktop.pages.singleDownloadPage.SingleDownloadPageSections.*
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.WithContentAlpha
import ir.amirab.util.compose.IconSource
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberComponentRectPositionProvider
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.ui.widget.customtable.CellSize
import com.abdownloadmanager.shared.ui.widget.customtable.Table
import com.abdownloadmanager.shared.ui.widget.customtable.TableCell
import com.abdownloadmanager.shared.ui.widget.customtable.TableState
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.LocalSizeUnit
import com.abdownloadmanager.shared.utils.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.utils.ui.useIsInDebugMode
import com.abdownloadmanager.shared.utils.div
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
        Res.string.settings.asStringSource(),
        MyIcons.settings
    ),
}

private val tabs = SingleDownloadPageSections.entries.toList()

@Composable
fun ProgressDownloadPage(singleDownloadComponent: SingleDownloadComponent, itemState: ProcessingDownloadItemState) {
    var selectedTab by remember { mutableStateOf(Info) }
    val showPartInfo by singleDownloadComponent.showPartInfo.collectAsState()
    val setShowPartInfo = singleDownloadComponent::setShowPartInfo
    val resizingState = LocalSingleDownloadPageSizing.current
    val horizontalPadding = 16.dp

    LaunchedEffect(resizingState.resizingPartInfo) {
        if (resizingState.partInfoHeight <= 0.dp) {
            setShowPartInfo(false)
        }
    }
    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .padding(1.dp),
        ) {
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
                    )
                }
            }
            val scrollState = rememberScrollState()
            //info / settings ...
            val tabContentModifier = Modifier

            Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.surface))
            Box(
                Modifier
                    .height(150.dp)
                    .background(myColors.background)
                    .verticalScroll(scrollState)
            ) {
                when (selectedTab) {
                    Info -> RenderInfo(
                        tabContentModifier,
                        horizontalPadding,
                        singleDownloadComponent,
                    )

                    Settings -> RenderSettings(
                        modifier = tabContentModifier.padding(end = 12.dp),
                        horizontalPadding = horizontalPadding,
                        singleDownloadComponent = singleDownloadComponent,
                    )
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.matchParentSize().wrapContentWidth(Alignment.End),
                    style = LocalScrollbarStyle.current.copy(
                        shape = RectangleShape
                    )
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
                .weight(1f)
                .background(myColors.surface / 0.5f)
        ) {
            Column(
                Modifier
                    .padding(horizontal = horizontalPadding)
            ) {
                Spacer(Modifier.size(8.dp))
                RenderProgressBar(itemState)
                Spacer(Modifier.size(8.dp))
                RenderActions(itemState, singleDownloadComponent, showPartInfo, setShowPartInfo)
                Spacer(Modifier.size(8.dp))
            }
            if (showPartInfo) {
                RenderPartInfo(
                    modifier = Modifier.weight(1f),
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
    singleDownloadComponent: SingleDownloadComponent,
) {
    Column(modifier) {
        for (configurable in singleDownloadComponent.settings) {
            RenderConfigurable(
                configurable, Modifier
                    // I'm using Configurable object which their renderer by default uses 8.dp, we want 16.dp, so I only add 8.dp here 16-8 == 8
                    // I may improve this later
                    .padding(horizontal = (horizontalPadding - 8.dp).coerceAtLeast(0.dp))
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
            .clip(RoundedCornerShape(6.dp))
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
        val singleDownloadPageSizing = LocalSingleDownloadPageSizing.current
        val mutableInteractionSource = remember { MutableInteractionSource() }
        val isDraggingHandle by mutableInteractionSource.collectIsDraggedAsState()
        LaunchedEffect(isDraggingHandle) {
            singleDownloadPageSizing.resizingPartInfo = isDraggingHandle
        }
        Column(
            Modifier.weight(1f)
        ) {
            RenderParts(
                itemState.parts,
                Modifier
                    .padding(horizontal = horizontalPadding)
                    .height(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(myColors.onBackground / 15)
            )
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
                                        PartDownloadStatus.SendGet -> true
                                    }
                                }
                            } else {
                                parts
                            }
                        }
                        .withIndex()
                        .toList()
                }
                Table(
                    list = listToShow,
                    key = {
                        it.value.from
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    wrapHeader = {
                        WithContentAlpha(0.75f) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .padding(vertical = 4.dp)
                            ) {
                                it()
                            }
                        }
                    },
                    tableState = remember {
                        TableState(
                            cells = PartInfoCells.all()
                        )
                    },
                    wrapItem = { _, _, content ->
                        WithContentAlpha(1f) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()
                            Box(
                                Modifier
                                    .padding(horizontal = horizontalPadding)
                                    .hoverable(interactionSource)
                                    .background(
                                        if (isHovered) myColors.onSurface / 10
                                        else Color.Transparent
                                    )
                            ) {
                                content()
                            }
                        }
                    }
                ) { cell, it ->
                    when (cell) {
                        PartInfoCells.Number -> {
                            SimpleCellText("${it.index + 1}")
                        }

                        PartInfoCells.Status -> {
                            SimpleCellText(prettifyStatus(it.value.status).rememberString())
                        }

                        PartInfoCells.Downloaded -> {
                            SimpleCellText(
                                convertPositiveSizeToHumanReadable(
                                    it.value.howMuchProceed,
                                    LocalSizeUnit.current
                                ).rememberString()
                            )
                        }

                        PartInfoCells.Total -> {
                            SimpleCellText(
                                it.value.length?.let { length ->
                                    convertPositiveSizeToHumanReadable(length, LocalSizeUnit.current).rememberString()
                                } ?: myStringResource(Res.string.unknown),
                            )
                        }
                    }
                }
                if (useIsInDebugMode()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 8.dp, end = 8.dp)
                            .onClick { setOnlyActiveParts(!onlyActiveParts) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Only Actives")
                        Spacer(Modifier.width(4.dp))
                        CheckBox(onlyActiveParts, { setOnlyActiveParts(it) })
                    }
                }
            }
        }
        Handle(
            Modifier.fillMaxWidth().height(8.dp),
            orientation = Orientation.Vertical,
            interactionSource = mutableInteractionSource
        ) {
            singleDownloadPageSizing.partInfoHeight += it
        }
    }
}

private fun prettifyStatus(status: PartDownloadStatus): StringSource {
    return when (status) {
        is PartDownloadStatus.Canceled -> Res.string.disconnected
        PartDownloadStatus.IDLE -> Res.string.idle
        PartDownloadStatus.Completed -> Res.string.finished
        PartDownloadStatus.ReceivingData -> Res.string.receiving_data
        PartDownloadStatus.SendGet -> Res.string.connecting
    }.asStringSource()
}

@Composable
private fun SimpleCellText(text: String) {
    Text(text, fontSize = myTextSizes.base, maxLines = 1)
}

sealed class PartInfoCells : TableCell<IndexedValue<UiPart>> {
    data object Number : PartInfoCells() {
        override val id: String = "#"
        override val name: StringSource = "#".asStringSource()
        override val size: CellSize = CellSize.Fixed(32.dp)
    }

    data object Status : PartInfoCells() {
        override val id: String = "Status"
        override val name: StringSource = Res.string.status.asStringSource()
        override val size: CellSize = CellSize.Resizeable(100.dp..200.dp)
    }

    data object Downloaded : PartInfoCells() {
        override val id: String = "Downloaded"
        override val name: StringSource = Res.string.parts_info_downloaded_size.asStringSource()
        override val size: CellSize = CellSize.Resizeable(90.dp..200.dp)
    }

    data object Total : PartInfoCells() {
        override val id: String = "Total"
        override val name: StringSource = Res.string.parts_info_total_size.asStringSource()
        override val size: CellSize = CellSize.Resizeable(90.dp..200.dp)
    }

    companion object {
        fun all(): List<PartInfoCells> {
            return listOf(
                Number,
                Status,
                Downloaded,
                Total,
            )
        }
    }
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
                    .basicMarquee()
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
    singleDownloadComponent: SingleDownloadComponent,
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
    singleDownloadComponent: SingleDownloadComponent,
    showingPartInfo: Boolean,
    onRequestShowPartInfo: (show: Boolean) -> Unit,
) {
    Row {
        PartInfoButton(showingPartInfo, onRequestShowPartInfo)
        Spacer(Modifier.weight(1f))
        ToggleButton(
            itemState = itemState,
            toggle = singleDownloadComponent::toggle,
            pause = singleDownloadComponent::pause,
        )
        Spacer(Modifier.width(8.dp))
        CancelButton(singleDownloadComponent::cancel)
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
            contentDescription = partsInfoTitle.rememberString(),
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
) {
    ActionButton(
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
) {
    SingleDownloadPageButton(
        {
            cancel()
        },
        text = myStringResource(Res.string.cancel)
    )
}

@Composable
private fun ToggleButton(
    itemState: ProcessingDownloadItemState,
    toggle: () -> Unit,
    pause: () -> Unit,
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

    Box {
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
        )
        if (showPromptOnNonePresumablePause) {
            val shape = RoundedCornerShape(6.dp)
            val closePopup = {
                showPromptOnNonePresumablePause = false
            }
            Popup(
                popupPositionProvider = rememberComponentRectPositionProvider(
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
                    it.from
                }
            }
            val total = sortedParts.last().to?.let {
                it + 1 // parts are end inclusive
            } ?: return
            for (p in sortedParts) {
                val partSpace = (p.length!!.toDouble() / total).toFloat()
                if (partSpace <= 0f) continue
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

@Composable
private fun RenderPart(part: UiPart, modifier: Modifier) {
    val partProgress = part.percent!! / 100f

    val foregroundColor = when (part.status) {
        is PartDownloadStatus.Canceled -> myColors.error
        PartDownloadStatus.Completed -> myColors.info
        PartDownloadStatus.IDLE -> myColors.info / 25
        PartDownloadStatus.ReceivingData -> myColors.success
        PartDownloadStatus.SendGet -> myColors.warning
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
