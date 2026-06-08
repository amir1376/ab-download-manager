package com.abdownloadmanager.android.pages.downloaderror


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderror.DownloadErrorComponent
import com.abdownloadmanager.shared.ui.widget.*
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun ResponsiveDialogScope.DownloadErrorDialog(
    component: DownloadErrorComponent,
    onDismiss: () -> Unit,
) {
    DownloadErrorDialog(
        link = component.downloadItem.link,
        errorReason = component.reason,
        onClose = onDismiss,
        onRequestCopyToClipboard = {
            component.onRequestCopyToClipboard()
        }
    )
}

@Composable
private fun ResponsiveDialogScope.DownloadErrorDialog(
    link: String,
    errorReason: DownloadErrorReason,
    onClose: () -> Unit,
    onRequestCopyToClipboard: () -> Unit,
) {
    SheetUI(
        header = {
            SheetHeader(
                headerTitle = {
                    SheetTitle(
                        myStringResource(Res.string.download_error)
                    )
                },
                headerActions = {
                    IconActionButton(
                        contentDescription = Res.string.copy.asStringSource(),
                        onClick = onRequestCopyToClipboard,
                        icon = MyIcons.copy,
                    )
                    Spacer(Modifier.width(mySpacings.mediumSpace))
                    IconActionButton(
                        MyIcons.close,
                        contentDescription = Res.string.close.asStringSource(),
                        onClick = onClose,
                    )
                }
            )
        }
    ) {
        Column {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
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
                        Text(
                            link,
                            overflow = TextOverflow.MiddleEllipsis,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(myColors.onBackground / 0.1f)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = myStringResource(Res.string.ok),
                    modifier = Modifier.weight(1f),
                    onClick = onClose,
                )
            }
        }
    }
}
