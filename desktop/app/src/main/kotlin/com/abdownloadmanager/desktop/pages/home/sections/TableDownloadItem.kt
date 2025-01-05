package com.abdownloadmanager.desktop.pages.home.sections

import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.widget.MyIcon
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.CheckBox
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.*
import com.abdownloadmanager.shared.utils.FileIconProvider
import com.abdownloadmanager.shared.utils.category.Category
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.compose.resources.MyStringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.*

val LocalDownloadItemProperties =
    compositionLocalOf<DownloadItemProperties> { error("not provided download properties") }


data class DownloadItemProperties(
    val isSelected: Boolean,
    val iDownloadItemState: IDownloadItemState,
)


@Composable
private fun isSelected(): Boolean {
    return LocalDownloadItemProperties.current.isSelected
}


@Composable
fun CheckCell(
    onCheckedChange: (Long, Boolean) -> Unit,
    dItemState: IDownloadItemState,
) {
    val isChecked = isSelected()
    CheckBox(
        value = isChecked,
        onValueChange = {
            onCheckedChange(dItemState.id, it)
        },
        modifier = Modifier,
        size = 12.dp,
    )
}

@Composable
fun NameCell(
    itemState: IDownloadItemState,
    category: Category?,
    fileIconProvider: FileIconProvider,
) {
    val fileIcon = fileIconProvider.rememberIcon(itemState.name)
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyIcon(
            icon = fileIcon,
            modifier = Modifier.size(16.dp),
            contentDescription = null,
//            tint = LocalContentColor.current / 75
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                text = itemState.name,
                maxLines = 1,
                fontSize = myTextSizes.base,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                category?.name ?: myStringResource(Res.string.general), maxLines = 1, fontSize = myTextSizes.xs,
                color = LocalContentColor.current / 50
            )
        }
    }

}

@Composable
fun TimeLeftCell(
    itemState: IDownloadItemState,
) {
    (itemState as? ProcessingDownloadItemState)?.remainingTime?.let { remaining ->
        Text(
            text = convertTimeRemainingToHumanReadable(remaining, TimeNames.ShortNames),
            maxLines = 1,
            fontSize = myTextSizes.base,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DateAddedCell(
    itemState: IDownloadItemState,
) {

    val t by produceState(
        key1 = itemState.dateAdded,
        initialValue = "",
    ) {
        while (isActive) {
            val now = Clock.System.now()
            val instant = Instant.fromEpochMilliseconds(itemState.dateAdded)
            val period = now.periodUntil(instant, TimeZone.UTC)
            val t = prettifyRelativeTime(period)
            value = t
            delay(1000)
        }
    }
    Text(
        text = t,
        maxLines = 1,
        fontSize = myTextSizes.base,
        overflow = TextOverflow.Ellipsis,
    )

}

@Composable
fun SpeedCell(
    itemState: IDownloadItemState,
) {
    (itemState as? ProcessingDownloadItemState)?.speed?.let { remaining ->
        if (itemState.status == DownloadJobStatus.Downloading) {
            Text(
                text = convertPositiveSpeedToHumanReadable(
                    remaining,
                    LocalSpeedUnit.current,
                ),
                maxLines = 1,
                fontSize = myTextSizes.base,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SizeCell(
    item: IDownloadItemState,
) {
    item.contentLength.let {
        Text(
            convertPositiveSizeToHumanReadable(
                it,
                LocalSizeUnit.current
            ).rememberString(),
            maxLines = 1,
            fontSize = myTextSizes.base,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StatusCell(
    itemState: IDownloadItemState,
) {
    when (itemState) {
        is ProcessingDownloadItemState -> {
            when (val status = itemState.status) {
                is DownloadJobStatus.Canceled -> {
                    ProgressAndPercent(
                        itemState.percent,
                        if (ExceptionUtils.isNormalCancellation(status.e)) {
                            if (!itemState.gotAnyProgress) {
                                DownloadProgressStatus.Added
                            } else {
                                DownloadProgressStatus.Paused
                            }
                        } else {
                            DownloadProgressStatus.Error
                        },
                        itemState.gotAnyProgress
                    )
                }

                DownloadJobStatus.IDLE -> {
                    ProgressAndPercent(
                        itemState.percent,
                        if (!itemState.gotAnyProgress) {
                            DownloadProgressStatus.Added
                        } else {
                            DownloadProgressStatus.Paused
                        },
                        itemState.gotAnyProgress
                    )
                }

                DownloadJobStatus.Downloading -> {
                    ProgressAndPercent(
                        itemState.percent,
                        DownloadProgressStatus.Downloading,
                        itemState.gotAnyProgress
                    )
                }

                is DownloadJobStatus.PreparingFile -> {
                    ProgressAndPercent(
                        status.percent,
                        DownloadProgressStatus.CreatingFile,
                        itemState.gotAnyProgress
                    )
                }

                DownloadJobStatus.Finished,
                DownloadJobStatus.Resuming,
                    -> SimpleStatus(myStringResource(itemState.status.toStringResource()))
            }
        }

        is CompletedDownloadItemState -> {
            SimpleStatus(myStringResource(Res.string.finished))
        }
    }

}

@Composable
private fun DownloadJobStatus.toStringResource(): MyStringResource {
    return when (this) {
        is DownloadJobStatus.Canceled -> {
            Res.string.canceled
        }

        DownloadJobStatus.Downloading -> {
            Res.string.downloading
        }

        DownloadJobStatus.Finished -> {
            Res.string.finished
        }

        DownloadJobStatus.IDLE -> {
            Res.string.idle
        }

        is DownloadJobStatus.PreparingFile -> {
            Res.string.preparing_file
        }

        DownloadJobStatus.Resuming -> {
            Res.string.resuming
        }
    }
}

private fun DownloadProgressStatus.toStringResource(): MyStringResource {
    return when (this) {
        DownloadProgressStatus.Added -> {
            Res.string.added
        }

        DownloadProgressStatus.Error -> {
            Res.string.error
        }

        DownloadProgressStatus.Paused -> {
            Res.string.paused
        }

        DownloadProgressStatus.CreatingFile -> {
            Res.string.creating_file
        }

        DownloadProgressStatus.Downloading -> {
            Res.string.downloading
        }
    }
}

@Composable
private fun SimpleStatus(string: String) {
    Text(
        text = string,
        maxLines = 1,
        fontSize = myTextSizes.base,
        overflow = TextOverflow.Ellipsis,
    )
}

private enum class DownloadProgressStatus {
    Added, Error, Paused, CreatingFile, Downloading
}

@Composable
private fun ProgressAndPercent(
    percent: Int?,
    status: DownloadProgressStatus,
    gotAnyProgress: Boolean,
) {
    val background = when (status) {
        DownloadProgressStatus.Error -> myColors.errorGradient
        DownloadProgressStatus.Paused, DownloadProgressStatus.Added -> myColors.warningGradient
        DownloadProgressStatus.CreatingFile -> myColors.infoGradient
        DownloadProgressStatus.Downloading -> myColors.primaryGradient
    }
    val statusString = myStringResource(status.toStringResource())
    Column {
        val statusText = if (gotAnyProgress) {
            "${percent ?: "."}% $statusString"
        } else {
            statusString
        }
        SimpleStatus(statusText)
        if (status != DownloadProgressStatus.Added) {
            Spacer(Modifier.height(2.5.dp))
            ProgressStatus(
                percent, background
            )
        }
    }
}

@Composable
private fun ProgressStatus(
    percent: Int?,
    background: Brush = myColors.primaryGradient,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(myColors.surface)
    ) {
        if (percent != null) {
            val w = (percent / 100f).coerceIn(0f..1f)
            Spacer(
                Modifier
                    .height(5.dp)
                    .fillMaxWidth(
                        animateFloatAsState(
                            w,
                            tween(100)
                        ).value
                    )
                    .background(background)
            )
        }
    }
}