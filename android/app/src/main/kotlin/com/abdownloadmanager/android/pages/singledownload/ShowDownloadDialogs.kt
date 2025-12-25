package com.abdownloadmanager.android.pages.singledownload

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.*
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.singledownloadpage.createStatusString
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.delay

@Composable
private fun getDownloadTitle(itemState: IDownloadItemState): String {
    return buildString {
        if (itemState is ProcessingDownloadItemState && itemState.percent != null) {
            append("${itemState.percent}%")
            append(" ")
        }
        append(createStatusString(itemState).rememberString())
    }
}


@Composable
fun ShowDownloadDialog(
    singleDownloadComponent: AndroidSingleDownloadComponent,
    onRequestShowInDownloads: () -> Unit,
) {
    val itemState by singleDownloadComponent.itemStateFlow.collectAsState()
    val dialogState = rememberResponsiveDialogState(false)
    dialogState.OnFullyDismissed {
        singleDownloadComponent.close()
    }
    LaunchedEffect(Unit) {
        // animate open after activity becomes fully open
        // is there a better way?
        delay(10)
        dialogState.show()
    }
    val closeDialog = dialogState::hide
    ResponsiveDialog(
        dialogState, closeDialog
    ) {
        itemState?.let { downloadItemState ->
            SheetUI(header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(getDownloadTitle(downloadItemState))
                    },
                    headerActions = {
                        if (singleDownloadComponent.comesFromExternalApplication) {
                            TransparentIconActionButton(
                                MyIcons.externalLink,
                                contentDescription = Res.string.show_downloads.asStringSource(),
                                onClick = onRequestShowInDownloads,
                            )
                        }
                        TransparentIconActionButton(
                            MyIcons.close,
                            contentDescription = Res.string.close.asStringSource(),
                            onClick = closeDialog
                        )
                    }
                )
            }) {
                AnimatedContent(
                    targetState = downloadItemState,
                    contentKey = {
                        when (it) {
                            is CompletedDownloadItemState -> 0
                            is ProcessingDownloadItemState -> 1
                        }
                    }
                ) { downloadItemState ->
                    when (downloadItemState) {
                        is CompletedDownloadItemState -> {
                            CompletedDownloadPage(
                                singleDownloadComponent,
                                downloadItemState,
                            )
                        }

                        is ProcessingDownloadItemState -> {
                            ProgressDownloadPage(
                                singleDownloadComponent,
                                downloadItemState,
                            )
                        }
                    }
                }

            }
        }
    }

}



