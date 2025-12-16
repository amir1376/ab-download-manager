package com.abdownloadmanager.android.pages.singledownload

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.*
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.shared.singledownloadpage.createStatusString
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
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
fun ShowDownloadDialog(singleDownloadComponent: AndroidSingleDownloadComponent) {
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
    ResponsiveDialog(
        dialogState, dialogState::hide
    ) {
        itemState?.let { downloadItemState ->
            SheetUI(header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(getDownloadTitle(downloadItemState))
                    }
                )
            }) {
                AnimatedContent(
                    targetState = downloadItemState,
                    contentKey = {
                        when (downloadItemState) {
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



