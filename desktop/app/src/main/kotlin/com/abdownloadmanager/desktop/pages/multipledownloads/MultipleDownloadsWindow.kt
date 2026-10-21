package com.abdownloadmanager.desktop.pages.multipledownloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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
import com.abdownloadmanager.shared.ui.widget.MyTextField
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MultipleDownloadsWindow(appComponent: AppComponent) {
    if (!appComponent.showMultipleDownloads.collectAsState().value) return
    val controller = appComponent.multipleDownloads
    val running by controller.running.collectAsState()
    val text by controller.text.collectAsState()
    val folder by controller.folder.collectAsState()
    val items by controller.items.collectAsState()
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
                sizeProvider = WindowSizeProvider.Fixed(DpSize(740.dp, 700.dp).applyUiScale(LocalUiScale.current)),
                positionProvider = WindowPositionProvider.CenteredOnScreen,
            )
        ),
        onCloseRequest = appComponent::closeMultipleDownloads,
        windowController = rememberWindowController(title = title),
        minSize = DpSize(560.dp, 660.dp),
    ) {
        val folderPicker = rememberMyDirectoryPickerLauncher(
            title = locationLabel,
            initialDirectory = folder.takeIf { it.isNotBlank() },
        ) { it?.let(controller::setFolder) }
        LaunchedEffect(Unit) { focus.requestFocus() }
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(myStringResource(Res.string.multiple_downloads_help))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(linksLabel, Modifier.weight(1f))
                ActionButton(myStringResource(Res.string.paste), enabled = !running, onClick = {
                    ClipboardUtil.read()?.let(controller::setText)
                }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp))
                Spacer(Modifier.width(8.dp))
                ActionButton(myStringResource(Res.string.clear), enabled = !running && text.isNotEmpty(), onClick = {
                    controller.setText("")
                }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp))
            }
            MyTextField(
                text = text,
                onTextChange = controller::setText,
                placeholder = "https://example.com/file-1.zip\nhttps://example.com/file-2.zip",
                singleLine = false,
                enabled = !running,
                focusRequester = focus,
                modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 100.dp)
                    .semantics { contentDescription = linksLabel }
                    .verticalScroll(rememberScrollState()),
            )
            Text(locationLabel)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MyTextField(
                    text = folder,
                    onTextChange = controller::setFolder,
                    placeholder = locationLabel,
                    enabled = !running,
                    modifier = Modifier.weight(1f).semantics { contentDescription = locationLabel },
                )
                ActionButton(myStringResource(Res.string.change), enabled = !running, onClick = { folderPicker.launch() })
            }
            val queued = items.count { it.status == MultipleDownloadsController.Status.Queued }
            val failed = items.count { it.status == MultipleDownloadsController.Status.Failed }
            Text("${items.size} $linksLabel · $queued ${myStringResource(Res.string.multiple_downloads_active)} · $failed ${myStringResource(Res.string.error)}",
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (text.isNotBlank() && items.isEmpty()) {
                Text(myStringResource(Res.string.nothing_will_be_imported), color = myColors.error)
            }
            if (items.isNotEmpty()) {
                LazyColumn(
                    Modifier.fillMaxWidth().height(104.dp).background(myColors.surface),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(items, key = { _, item -> item.url }) { index, item ->
                        Column {
                            val status = when (item.status) {
                                MultipleDownloadsController.Status.Pending -> myStringResource(Res.string.multiple_downloads_pending)
                                MultipleDownloadsController.Status.Resolving -> myStringResource(Res.string.multiple_downloads_resolving)
                                MultipleDownloadsController.Status.Queued -> myStringResource(Res.string.multiple_downloads_active)
                                MultipleDownloadsController.Status.Ready -> myStringResource(Res.string.multiple_downloads_ready)
                                MultipleDownloadsController.Status.Failed -> myStringResource(Res.string.error)
                            }
                            Text("${index + 1}. $status · ${item.detail.ifBlank { item.url }}", maxLines = 2, overflow = TextOverflow.Ellipsis,
                                color = if (item.status == MultipleDownloadsController.Status.Failed) myColors.error else myColors.onBackground)
                        }
                    }
                }
            }
            Text(myStringResource(Res.string.multiple_downloads_queue_help))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                ActionButton(myStringResource(Res.string.close), onClick = appComponent::closeMultipleDownloads)
                if (running) {
                    ActionButton(myStringResource(Res.string.multiple_downloads_stop), onClick = controller::stop)
                } else {
                    ActionButton(myStringResource(Res.string.download),
                        enabled = folder.isNotBlank() && items.any { it.status != MultipleDownloadsController.Status.Queued },
                        onClick = controller::start)
                }
            }
        }
    }
}
