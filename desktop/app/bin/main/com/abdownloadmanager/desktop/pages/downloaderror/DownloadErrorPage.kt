package com.abdownloadmanager.desktop.pages.downloaderror

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.ui.template.DialogFooter
import com.abdownloadmanager.desktop.ui.template.DialogMainContent
import com.abdownloadmanager.desktop.ui.template.DialogUi
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.WindowTitle
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderror.DownloadErrorComponent
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.abdownloadmanager.shared.util.rememberChild
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale

@Composable
fun DownloadErrorDialog(
    appComponent: AppComponent
) {
    appComponent.downloadErrorDialogSlot.rememberChild()?.let {
        DownloadErrorDialog(it)
    }
}

@Composable
private fun DownloadErrorDialog(
    downloadErrorComponent: DownloadErrorComponent
) {
    CustomWindow(
        state = rememberWindowState(
            size = DpSize(400.dp, 300.dp)
                .applyUiScale(LocalUiScale.current),
            position = WindowPosition.Aligned(Alignment.Center),
        ),
        onCloseRequest = downloadErrorComponent.onClose,
        alwaysOnTop = true,
    ) {
        WindowTitle(
            myStringResource(Res.string.download_error)
        )
        DownloadErrorDialog(
            downloadErrorComponent.downloadItem.link,
            downloadErrorComponent.reason,
            onClose = downloadErrorComponent.onClose,
            onRequestCopyToClipboard = downloadErrorComponent::onRequestCopyToClipboard
        )
    }
}

@Composable
private fun DownloadErrorDialog(
    link: String,
    errorReason: DownloadErrorReason,
    onClose: () -> Unit,
    onRequestCopyToClipboard: () -> Unit,
) {
    DialogUi(
        mainContent = {
            DialogMainContent {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MyIcon(
                            MyIcons.info,
                            contentDescription = null,
                            Modifier.size(mySpacings.iconSize * 2),
                            tint = myColors.error,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                errorReason.title,
                                fontSize = myTextSizes.lg,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                            )
                            Spacer(Modifier.height(mySpacings.smallSpace))
                            WithContentAlpha(0.8f) {
                                Text(
                                    link,
                                    overflow = TextOverflow.MiddleEllipsis,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.onBackground / 0.1f))
                    Column(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            errorReason.description,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            myStringResource(Res.string.suggestion),
                            fontSize = myTextSizes.lg,
                            color = myColors.success,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            errorReason.suggestion,
                        )
                    }
                }
            }
        },
        footer = {
            DialogFooter {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ActionButton(
                        text = myStringResource(Res.string.copy),
                        onClick = onRequestCopyToClipboard,
                    )
                    Spacer(Modifier.width(8.dp))
                    ActionButton(
                        text = myStringResource(Res.string.ok),
                        onClick = onClose,
                    )
                }
            }
        }
    )
}
