package com.abdownloadmanager.android.pages.singledownload

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.LocalSizeUnit
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun CompletedDownloadPage(
    component: AndroidSingleDownloadComponent,
    completedDownloadItemState: CompletedDownloadItemState,
) {
    Column {
        Row(
            Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            RenderFileIconAndSize(
                modifier = Modifier.align(Alignment.CenterVertically),
                component = component,
                itemState = completedDownloadItemState,
            )
            Spacer(Modifier.width(16.dp))
            RenderName(
                Modifier.weight(1f),
                completedDownloadItemState.name,
            )
        }
        Actions(Modifier, component)
    }
}

@Composable
private fun Actions(
    modifier: Modifier,
    component: AndroidSingleDownloadComponent,
) {
    val iDownloadItemState by component.itemStateFlow.collectAsState()
    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onBackground / 0.15f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(myColors.surface / 0.5f)
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionButton(
                myStringResource(Res.string.open),
                modifier = Modifier.weight(1f),
                onClick = {
                    component.openFile()
                },
            )
        }
    }
}

@Composable
private fun RenderName(
    modifier: Modifier,
    name: String,
) {
    Column(
        modifier = modifier
    ) {
        WithContentColor(
            myColors.success
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MyIcon(
                    MyIcons.check, null,
                    Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    myStringResource(Res.string.download_page_download_completed),
                    fontWeight = FontWeight.Bold,
                    fontSize = myTextSizes.lg,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = name,
            maxLines = 1,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE
            )
        )
    }
}

@Composable
private fun RenderFileIconAndSize(
    modifier: Modifier,
    component: AndroidSingleDownloadComponent,
    itemState: CompletedDownloadItemState,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MyIcon(
            icon = component.fileIconProvider.rememberIcon(itemState.name),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = convertPositiveSizeToHumanReadable(
                itemState.contentLength,
                LocalSizeUnit.current,
            ).rememberString(),
        )
    }
}
