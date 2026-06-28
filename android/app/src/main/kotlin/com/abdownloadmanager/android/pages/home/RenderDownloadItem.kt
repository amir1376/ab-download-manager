package com.abdownloadmanager.android.pages.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment.Companion.Unbounded
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.singledownloadpage.createStatusStringWithReason
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.abdownloadmanager.shared.util.ui.*
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.utils.ExceptionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val PROGRESS_HEIGHT = 6

@Composable
fun RenderDownloadItem(
    checked: Boolean?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    downloadItem: IDownloadItemState,
    errorReason: DownloadErrorReason?,
    fileIconProvider: FileIconProvider,
    modifier: Modifier,
) {
    Row(
        modifier
    ) {
        WithContentColor(
            myColors.onSurface,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .let {
                        if (checked == true) {
                            val selectionColor = myColors.onBackground
                            it.background(myColors.selectionGradient(0.15f, 0.03f, selectionColor))
                        } else {
                            it.border(1.dp, Color.Transparent)
                        }
                    }
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        checked != null
                    ) {
                        Row {
                            val isChecked = checked ?: false
                            CheckBox(
                                value = isChecked,
                                onValueChange = { onLongClick() },
                                size = 18.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                    RenderFileIcon(
                        downloadItem = downloadItem,
                        fileIconProvider = fileIconProvider,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            downloadItem.name,
                            maxLines = 1,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RenderProgressBar(
                                downloadItem, Modifier
                                    .weight(1f)
                                    .height(PROGRESS_HEIGHT.dp)
                            )
                            if (downloadItem is ProcessingDownloadItemState) {
                                Spacer(Modifier.width(2.dp))
                                RenderProgressLight(downloadItem)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                RenderSubTexts(downloadItem, errorReason)
            }
        }
    }
}

@Composable
fun RenderProgressLight(itemState: IDownloadItemState) {
    val color = when (val status = itemState.statusOrFinished()) {
        is DownloadJobStatus.IsActive -> {
            myColors.primaryGradient
        }

        is DownloadJobStatus.CanBeResumed -> {
            if (status is DownloadJobStatus.Canceled && !ExceptionUtils.isNormalCancellation(status.e)) {
                myColors.errorGradient
            } else {
                myColors.warningGradient
            }
        }

        DownloadJobStatus.Finished -> {
            myColors.successGradient
        }
    }
    Box(
        modifier = Modifier
            .size((PROGRESS_HEIGHT).dp)
            .background(color, CircleShape),
    )
}

@Composable
fun RenderSubTexts(itemState: IDownloadItemState, errorReason: DownloadErrorReason?) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = myTextSizes.xs),
        LocalContentAlpha provides 0.8f
    ) {
        Box(
            Modifier.fillMaxWidth()
        ) {
            RenderLeftSubText(itemState, Modifier.align(Alignment.CenterStart))
            RenderCenterSubText(itemState, errorReason, Modifier.align(Alignment.Center))
            RenderRightSubText(itemState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun RenderEta(itemState: ProcessingDownloadItemState, modifier: Modifier) {
    val eta = remember(itemState.remainingTime) {
        itemState.remainingTime?.let {
            convertTimeRemainingToHumanReadable(
                it,
                TimeNames.ShortNames
            )
        }.orEmpty()
    }
    Text(eta, modifier)
}

@OptIn(ExperimentalTime::class)
@Composable
private fun RenderAddedTime(itemState: IDownloadItemState, modifier: Modifier) {
    var dateAddedString by remember { mutableStateOf("") }
    val useRelativeDateTime = LocalUseRelativeDateTime.current

    LaunchedEffect(
        itemState.dateAdded,
        useRelativeDateTime,
    ) {
        val instant = Instant.fromEpochMilliseconds(itemState.dateAdded)
        if (useRelativeDateTime) {
            while (isActive) {
                val now = Clock.System.now()
                val period = now.periodUntil(instant, TimeZone.UTC)
                val relativeTime = prettifyRelativeTime(period)
                dateAddedString = relativeTime
                delay(1000.milliseconds)
            }
        } else {
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            dateAddedString = dateTime.format(MyDateAndTimeFormats.fullDateTime)
        }
    }
    Text(dateAddedString, modifier)
}

@Composable
fun RenderRightSubText(itemState: IDownloadItemState, modifier: Modifier) {
    if (itemState is ProcessingDownloadItemState && itemState.status is DownloadJobStatus.IsActive) {
        RenderEta(itemState, modifier)
    } else {
        RenderAddedTime(itemState, modifier)
    }
}

@Composable
fun RenderCenterSubText(itemState: IDownloadItemState, errorReason: DownloadErrorReason?, modifier: Modifier) {
    if (itemState is ProcessingDownloadItemState) {
        if (itemState.status is DownloadJobStatus.IsActive) {
            RenderSpeed(itemState.speed, modifier)
        } else {
            RenderTextStatus(itemState, errorReason, modifier)
        }
    }
}

@Composable
fun RenderTextStatus(
    itemState: IDownloadItemState,
    errorReason: DownloadErrorReason?,
    modifier: Modifier,
) {
    val status = createStatusStringWithReason(itemState, errorReason)
    Text(
        status.rememberString(),
        color = if (errorReason != null) {
            myColors.error
        } else {
            LocalContentColor.current
        },
        modifier = modifier,
    )
}

@Composable
fun RenderSpeed(speed: Long, modifier: Modifier) {
    val target = LocalSpeedUnit.current
    val speedString = remember(speed) {
        convertPositiveSpeedToHumanReadable(speed, target)
    }
    Text(speedString, modifier)
}

@Composable
fun RenderLeftSubText(itemState: IDownloadItemState, modifier: Modifier) {
    val totalSize = itemState.contentLength
    val sizeUnit = LocalSizeUnit.current
    val totalSizeString = remember(totalSize, sizeUnit) {
        convertPositiveSizeToHumanReadable(totalSize, sizeUnit, true)
    }
    val progress = (itemState as? ProcessingDownloadItemState)?.progress
    val progressStringOrNull = remember(progress, sizeUnit) {
        progress?.let {
            convertPositiveSizeToHumanReadable(progress, sizeUnit, true)
        }
    }
    val text = when {
        else -> {
            buildString {
                progressStringOrNull?.let {
                    append(it.rememberString())
                    append("/")
                }
                append(totalSizeString.rememberString())
            }
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (itemState is ProcessingDownloadItemState && itemState.supportResume == false) {
            MyIcon(
                MyIcons.pause,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = myColors.error,
            )
        }
        Text(text)
    }
}


@Composable
private fun RenderFileIcon(
    downloadItem: IDownloadItemState,
    fileIconProvider: FileIconProvider,
) {
    MyIcon(
        icon = fileIconProvider.rememberIcon(downloadItem.name),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun RenderProgressBar(
    itemState: IDownloadItemState,
    modifier: Modifier,
) {
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
        modifier
            .fillMaxSize()
            .clip(myShapes.defaultRounded)
            .background(myColors.onBackground / 15)
    ) {
        progress?.let { progress ->
            Box(
                Modifier
                    .clip(myShapes.defaultRounded)
                    .background(background)
                    .fillMaxHeight()
                    .fillMaxWidth(
                        animateFloatAsState(
                            progress,
                            tween(100, easing = LinearEasing)
                        ).value
                    )
            ) {
//                if (status is DownloadJobStatus.Downloading) {
//                    JetFade(
//                        Modifier
//                            .fillMaxSize()
//                            .padding(end = 1.dp)
//                    )
//                }
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
private fun JetFade(modifier: Modifier) {
    val color = myColors.onContrast / 0.80f
    Box(
        modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            Modifier
                .blur(2.dp, edgeTreatment = Unbounded)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(color, Color.Transparent)
                    )
                )
        )
        Box(
            Modifier
                .blur(1.dp, edgeTreatment = Unbounded)
                .fillMaxHeight(0.6f)
                .fillMaxWidth(0.4f)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            color,
                        )
                    )
                )
        )
    }
}
