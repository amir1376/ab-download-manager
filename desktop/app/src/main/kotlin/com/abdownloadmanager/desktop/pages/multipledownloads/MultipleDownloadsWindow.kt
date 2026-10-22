package com.abdownloadmanager.desktop.pages.multipledownloads

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.util.rememberMyDirectoryPickerLauncher
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.rememberWindowController
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.PrimaryMainActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.LocalSizeUnit
import com.abdownloadmanager.shared.util.LocalSpeedUnit
import com.abdownloadmanager.shared.util.TimeNames
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.shared.util.convertTimeRemainingToHumanReadable
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.awt.Frame
import java.io.File

@Composable
fun MultipleDownloadsWindow(appComponent: AppComponent) {
    if (!appComponent.showMultipleDownloads.collectAsState().value) return
    val controller = appComponent.multipleDownloads
    val running by controller.running.collectAsState()
    val text by controller.text.collectAsState()
    val folder by controller.folder.collectAsState()
    val items by controller.items.collectAsState()
    val startedIds by appComponent.multipleDownloadIds.collectAsState()
    val downloadStates by appComponent.downloadSystem.downloadMonitor.downloadListFlow.collectAsState()
    val rows = remember(items, startedIds, downloadStates) { buildRows(items, startedIds, downloadStates) }
    val title = myStringResource(Res.string.download_multiple_files)
    val linksLabel = myStringResource(Res.string.links)
    val locationLabel = myStringResource(Res.string.download_location)
    val focus = remember { FocusRequester() }
    LaunchedEffect(controller) {
        while (isActive) {
            controller.refreshActiveDownloads()
            delay(500)
        }
    }
    CustomWindow(
        state = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                sizeProvider = WindowSizeProvider.Fixed(DpSize(860.dp, 780.dp).applyUiScale(LocalUiScale.current)),
                positionProvider = WindowPositionProvider.CenteredOnScreen,
            )
        ),
        onCloseRequest = appComponent::closeMultipleDownloads,
        windowController = rememberWindowController(title = title),
        minSize = DpSize(680.dp, 640.dp),
    ) {
        // clicking "Download multiple files" again brings this window back instead of doing nothing
        LaunchedEffect(Unit) {
            appComponent.multipleDownloadsFocusRequests.collect {
                if (window.state == Frame.ICONIFIED) window.state = Frame.NORMAL
                window.toFront()
                window.requestFocus()
            }
        }
        val folderPicker = rememberMyDirectoryPickerLauncher(
            title = locationLabel,
            initialDirectory = folder.takeIf { it.isNotBlank() },
        ) { it?.let(controller::setFolder) }
        LaunchedEffect(Unit) { focus.requestFocus() }
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                myStringResource(Res.string.multiple_downloads_help),
                fontSize = myTextSizes.sm,
                color = myColors.onBackground / 0.7f,
            )
            Section {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(linksLabel)
                    Spacer(Modifier.width(8.dp))
                    CountBadge(items.size.toString(), myColors.primary)
                    Spacer(Modifier.weight(1f))
                    ActionButton(
                        myStringResource(Res.string.paste), enabled = !running,
                        onClick = { ClipboardUtil.read()?.let(controller::setText) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    ActionButton(
                        myStringResource(Res.string.clear), enabled = !running && text.isNotEmpty(),
                        onClick = { controller.setText("") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
                MyTextField(
                    text = text,
                    onTextChange = controller::setText,
                    placeholder = "https://example.com/file-1.zip\nhttps://example.com/file-2.zip",
                    singleLine = false,
                    enabled = !running,
                    focusRequester = focus,
                    fontSize = myTextSizes.base,
                    modifier = Modifier.fillMaxWidth().height(128.dp)
                        .semantics { contentDescription = linksLabel },
                )
                if (text.isNotBlank() && items.isEmpty()) {
                    Text(myStringResource(Res.string.nothing_will_be_imported), color = myColors.error, fontSize = myTextSizes.sm)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(locationLabel, fontSize = myTextSizes.sm, color = myColors.onBackground / 0.7f)
                    MyTextField(
                        text = folder,
                        onTextChange = controller::setFolder,
                        placeholder = locationLabel,
                        enabled = !running,
                        modifier = Modifier.weight(1f).semantics { contentDescription = locationLabel },
                    )
                    ActionButton(
                        myStringResource(Res.string.change), enabled = !running,
                        onClick = { folderPicker.launch() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    )
                    IconActionButton(
                        icon = MyIcons.folderOpen,
                        contentDescription = Res.string.open_folder.asStringSource(),
                        enabled = folder.isNotBlank() && File(folder).isDirectory,
                        onClick = { FileUtils.openFolder(File(folder)) },
                    )
                }
            }
            DownloadsSection(
                rows = rows,
                running = running,
                onPauseAll = { appComponent.pauseDownloads(rows.filter { it.canPause }.mapNotNull { it.state?.id }) },
                onResumeAll = { appComponent.resumeDownloads(rows.filter { it.canResume }.mapNotNull { it.state?.id }) },
                onRetryFailed = controller::retryFailed,
                onPause = { appComponent.pauseDownloads(listOf(it)) },
                onResume = { appComponent.resumeDownloads(listOf(it)) },
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    myStringResource(Res.string.multiple_downloads_queue_help),
                    fontSize = myTextSizes.sm,
                    color = myColors.onBackground / 0.6f,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(myStringResource(Res.string.close), onClick = appComponent::closeMultipleDownloads)
                if (running) {
                    ActionButton(myStringResource(Res.string.multiple_downloads_stop), onClick = controller::stop)
                } else {
                    PrimaryMainActionButton(
                        text = myStringResource(Res.string.download),
                        modifier = Modifier,
                        enabled = folder.isNotBlank() && items.any { it.status != MultipleDownloadsController.Status.Queued },
                        onClick = controller::start,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadsSection(
    rows: List<DownloadRow>,
    running: Boolean,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onRetryFailed: () -> Unit,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
    modifier: Modifier,
) {
    Section(modifier) {
        val active = rows.count { it.kind == RowKind.Downloading || it.kind == RowKind.Preparing }
        val waiting = rows.count { it.kind == RowKind.Waiting || it.kind == RowKind.Resolving }
        val finished = rows.count { it.kind == RowKind.Finished }
        val failed = rows.count { it.kind == RowKind.Failed || it.kind == RowKind.Error }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(myStringResource(Res.string.multiple_downloads_downloads))
            Row(
                Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatChip(myStringResource(Res.string.downloading), active, myColors.primary)
                StatChip(myStringResource(Res.string.waiting), waiting, myColors.info)
                StatChip(myStringResource(Res.string.finished), finished, myColors.success)
                StatChip(myStringResource(Res.string.error), failed, myColors.error)
            }
            IconActionButton(
                icon = MyIcons.pause,
                contentDescription = Res.string.multiple_downloads_pause_all.asStringSource(),
                enabled = rows.any { it.canPause },
                onClick = onPauseAll,
            )
            IconActionButton(
                icon = MyIcons.resume,
                contentDescription = Res.string.multiple_downloads_resume_all.asStringSource(),
                enabled = rows.any { it.canResume },
                onClick = onResumeAll,
            )
            IconActionButton(
                icon = MyIcons.refresh,
                contentDescription = Res.string.multiple_downloads_retry_failed.asStringSource(),
                enabled = !running && rows.any { it.kind == RowKind.Failed },
                onClick = onRetryFailed,
            )
        }
        OverallProgress(rows)
        if (rows.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    myStringResource(Res.string.multiple_downloads_empty),
                    fontSize = myTextSizes.sm,
                    color = myColors.onBackground / 0.5f,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(rows, key = { _, row -> row.item.url }) { index, row ->
                    DownloadRowView(index + 1, row, onPause, onResume)
                }
            }
        }
    }
}

@Composable
private fun OverallProgress(rows: List<DownloadRow>) {
    val sized = rows.mapNotNull { it.state }.filter { it.contentLength > 0 }
    val total = sized.sumOf { it.contentLength }
    val done = sized.sumOf { state ->
        when (state) {
            is CompletedDownloadItemState -> state.contentLength
            is ProcessingDownloadItemState -> state.progress.coerceAtMost(state.contentLength)
            else -> 0L
        }
    }
    val speed = rows.mapNotNull { it.state as? ProcessingDownloadItemState }
        .filter { it.status == DownloadJobStatus.Downloading }
        .sumOf { it.speed }
    val percent = if (total > 0) ((done * 100) / total).toInt() else null
    val sizeUnit = LocalSizeUnit.current
    val speedUnit = LocalSpeedUnit.current
    val doneText = convertPositiveSizeToHumanReadable(done, sizeUnit).rememberString()
    val totalText = convertPositiveSizeToHumanReadable(total, sizeUnit).rememberString()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                myStringResource(Res.string.multiple_downloads_overall),
                fontSize = myTextSizes.sm,
                color = myColors.onBackground / 0.7f,
                modifier = Modifier.weight(1f),
            )
            val details = buildList {
                if (percent != null) add("$percent%")
                if (total > 0) add("$doneText / $totalText")
                if (speed > 0) add(convertPositiveSpeedToHumanReadable(speed, speedUnit))
            }
            Text(details.joinToString("  ·  "), fontSize = myTextSizes.sm)
        }
        ProgressBar(percent, myColors.primaryGradient, height = 6.dp)
    }
}

@Composable
private fun DownloadRowView(
    number: Int,
    row: DownloadRow,
    onPause: (Long) -> Unit,
    onResume: (Long) -> Unit,
) {
    val shape = myShapes.defaultRounded
    Row(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(myColors.background / 0.6f)
            .border(1.dp, myColors.onBackground / 0.06f, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("$number", fontSize = myTextSizes.sm, color = myColors.onBackground / 0.5f, modifier = Modifier.widthIn(min = 18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(row.statusText(), fontSize = myTextSizes.sm, color = row.kind.color(), maxLines = 1)
            }
            if (row.kind.showsProgress) {
                ProgressBar(row.percent, row.kind.brush())
            }
            Text(
                row.detailsText(),
                fontSize = myTextSizes.xs,
                color = if (row.kind == RowKind.Failed || row.kind == RowKind.Error) myColors.error else myColors.onBackground / 0.55f,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val state = row.state
        when {
            row.canPause && state != null -> IconActionButton(
                icon = MyIcons.pause,
                contentDescription = Res.string.pause.asStringSource(),
                onClick = { onPause(state.id) },
            )

            row.canResume && state != null -> IconActionButton(
                icon = MyIcons.resume,
                contentDescription = Res.string.resume.asStringSource(),
                onClick = { onResume(state.id) },
            )

            state is CompletedDownloadItemState -> IconActionButton(
                icon = MyIcons.folderOpen,
                contentDescription = Res.string.open_folder.asStringSource(),
                onClick = { FileUtils.openFolderOfFile(state.getFullPath()) },
            )

            else -> Spacer(Modifier.size(28.dp))
        }
    }
}

@Composable
private fun DownloadRow.statusText(): String {
    val percentPrefix = percent?.let { "$it% " }.orEmpty()
    return when (kind) {
        RowKind.Resolving -> myStringResource(Res.string.multiple_downloads_resolving)
        RowKind.Pending -> myStringResource(Res.string.multiple_downloads_pending)
        RowKind.Ready -> myStringResource(Res.string.multiple_downloads_ready)
        RowKind.Queued -> myStringResource(Res.string.multiple_downloads_active)
        RowKind.Failed, RowKind.Error -> myStringResource(Res.string.error)
        RowKind.Waiting -> myStringResource(Res.string.waiting)
        RowKind.Downloading -> percentPrefix + myStringResource(Res.string.downloading)
        RowKind.Preparing -> myStringResource(
            if ((state as? ProcessingDownloadItemState)?.status is DownloadJobStatus.PreparingFile) Res.string.preparing_file
            else Res.string.resuming
        )
        RowKind.Retrying -> myStringResource(Res.string.retrying)
        RowKind.Paused -> percentPrefix + myStringResource(Res.string.paused)
        RowKind.Finished -> myStringResource(Res.string.finished)
    }
}

@Composable
private fun DownloadRow.detailsText(): String {
    if (kind == RowKind.Failed) return item.detail.ifBlank { item.url }
    val state = state ?: return item.url
    val sizeUnit = LocalSizeUnit.current
    val speedUnit = LocalSpeedUnit.current
    val totalText = convertPositiveSizeToHumanReadable(state.contentLength, sizeUnit).rememberString()
    val doneText = (state as? ProcessingDownloadItemState)
        ?.let { convertPositiveSizeToHumanReadable(it.progress, sizeUnit).rememberString() }
    val hasTotal = state.contentLength > 0
    return buildList {
        when (state) {
            is CompletedDownloadItemState -> if (hasTotal) add(totalText)
            is ProcessingDownloadItemState -> {
                add(if (hasTotal) "$doneText / $totalText" else doneText.orEmpty())
                if (state.status == DownloadJobStatus.Downloading) {
                    add(convertPositiveSpeedToHumanReadable(state.speed, speedUnit))
                    state.remainingTime?.let { add(convertTimeRemainingToHumanReadable(it, TimeNames.ShortNames)) }
                }
                (state.status as? DownloadJobStatus.Canceled)?.e
                    ?.takeUnless { ExceptionUtils.isNormalCancellation(it) }
                    ?.let { add(it.message ?: it::class.simpleName.orEmpty()) }
            }

            else -> {}
        }
        add(state.folder)
    }.filter { it.isNotBlank() }.joinToString("  ·  ")
}

@Composable
private fun Section(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = myShapes.defaultRounded
    Column(
        modifier.fillMaxWidth()
            .clip(shape)
            .background(myColors.surface / 0.45f)
            .border(1.dp, myColors.onBackground / 0.08f, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = myTextSizes.lg)
}

@Composable
private fun CountBadge(text: String, color: Color) {
    Text(
        text,
        fontSize = myTextSizes.xs,
        color = color,
        modifier = Modifier.clip(CircleShape).background(color / 0.15f).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Row(
        Modifier.clip(CircleShape).background(color / 0.12f).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text("$label $count", fontSize = myTextSizes.xs, maxLines = 1)
    }
}

@Composable
private fun ProgressBar(percent: Int?, brush: Brush, height: Dp = 4.dp) {
    Box(
        Modifier.fillMaxWidth().height(height)
            .clip(CircleShape)
            .background(myColors.onBackground / 0.08f)
    ) {
        if (percent != null) {
            val fraction by animateFloatAsState((percent / 100f).coerceIn(0f, 1f), tween(150))
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(brush))
        }
    }
}

private enum class RowKind(val showsProgress: Boolean) {
    Pending(false), Resolving(false), Ready(false), Queued(false), Failed(false),
    Waiting(true), Downloading(true), Preparing(true), Retrying(true), Paused(true), Error(true), Finished(true),
}

@Composable
private fun RowKind.color(): Color = when (this) {
    RowKind.Downloading -> myColors.primary
    RowKind.Preparing, RowKind.Resolving, RowKind.Waiting -> myColors.info
    RowKind.Paused, RowKind.Retrying -> myColors.warning
    RowKind.Failed, RowKind.Error -> myColors.error
    RowKind.Finished -> myColors.success
    RowKind.Pending, RowKind.Ready, RowKind.Queued -> myColors.onBackground / 0.7f
}

@Composable
private fun RowKind.brush(): Brush = when (this) {
    RowKind.Finished -> myColors.successGradient
    RowKind.Paused, RowKind.Retrying -> myColors.warningGradient
    RowKind.Error -> myColors.errorGradient
    RowKind.Preparing, RowKind.Waiting -> myColors.infoGradient
    else -> myColors.primaryGradient
}

private class DownloadRow(
    val item: MultipleDownloadsController.Item,
    val state: IDownloadItemState?,
) {
    val kind: RowKind = when {
        item.status == MultipleDownloadsController.Status.Resolving -> RowKind.Resolving
        item.status == MultipleDownloadsController.Status.Failed -> RowKind.Failed
        state is CompletedDownloadItemState -> RowKind.Finished
        state is ProcessingDownloadItemState && state.isWaiting -> RowKind.Waiting
        state is ProcessingDownloadItemState -> when (val status = state.status) {
            DownloadJobStatus.Downloading -> RowKind.Downloading
            DownloadJobStatus.Resuming, is DownloadJobStatus.PreparingFile -> RowKind.Preparing
            is DownloadJobStatus.Retrying -> RowKind.Retrying
            is DownloadJobStatus.Canceled -> if (ExceptionUtils.isNormalCancellation(status.e)) RowKind.Paused else RowKind.Error
            DownloadJobStatus.IDLE -> RowKind.Paused
            DownloadJobStatus.Finished -> RowKind.Finished
        }

        item.status == MultipleDownloadsController.Status.Ready -> RowKind.Ready
        item.status == MultipleDownloadsController.Status.Queued -> RowKind.Queued
        else -> RowKind.Pending
    }
    val title: String = state?.name
        ?: item.detail.takeIf { it.isNotBlank() && kind != RowKind.Failed }
        ?: item.url.substringAfterLast('/').substringBefore('?').substringBefore('#').ifBlank { item.url }
    val percent: Int? = when (state) {
        is CompletedDownloadItemState -> 100
        is ProcessingDownloadItemState -> state.percent
        else -> null
    }
    val canPause = kind == RowKind.Downloading || kind == RowKind.Preparing || kind == RowKind.Retrying || kind == RowKind.Waiting
    val canResume = kind == RowKind.Paused || kind == RowKind.Error
}

/** Pairs every link with the download it started (or an existing active download of the same link). */
private fun buildRows(
    items: List<MultipleDownloadsController.Item>,
    startedIds: Map<String, Long>,
    states: List<IDownloadItemState>,
): List<DownloadRow> {
    val byId = states.associateBy { it.id }
    val latestByLink = states.groupBy { MultipleDownloadsController.linkKey(it.downloadLink) }
        .mapValues { (_, sameLink) -> sameLink.maxBy { it.id } }
    return items.map { item ->
        val key = MultipleDownloadsController.linkKey(item.url)
        val state = startedIds[key]?.let(byId::get)
            ?: latestByLink[key]?.takeIf { item.status == MultipleDownloadsController.Status.Queued }
        DownloadRow(item, state)
    }
}
