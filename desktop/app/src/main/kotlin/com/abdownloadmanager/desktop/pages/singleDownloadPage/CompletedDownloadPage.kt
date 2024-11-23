package com.abdownloadmanager.desktop.pages.singleDownloadPage

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.pages.category.toCategoryImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.Check
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.desktop.utils.convertSizeToHumanReadable
import com.abdownloadmanager.desktop.utils.div
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.compose.WithContentColor
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.downloader.monitor.CompletedDownloadItemState
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun CompletedDownloadPage(
    component: SingleDownloadComponent,
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
        Spacer(Modifier.weight(1f))
        Actions(Modifier, component)
    }
}

@Composable
private fun Actions(
    modifier: Modifier,
    component: SingleDownloadComponent,
) {
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
        ) {
            ActionButton(
                myStringResource(Res.string.open),
                modifier = Modifier,
                onClick = {
                    component.openFile()
                },
            )
            Spacer(Modifier.width(8.dp))
            ActionButton(
                myStringResource(Res.string.open_folder),
                modifier = Modifier,
                onClick = {
                    component.openFolder()
                },
            )
            Spacer(Modifier.weight(1f))
            ActionButton(
                myStringResource(Res.string.close),
                modifier = Modifier,
                onClick = component::close,
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
                Icon(
                    imageVector = AbIcons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
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
            name,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
    }
}

@Composable
private fun RenderFileIconAndSize(
    modifier: Modifier,
    component: SingleDownloadComponent,
    itemState: CompletedDownloadItemState,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = component.fileIconProvider
                .rememberCategoryIcon(itemState.name)
                .toCategoryImageVector(),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = convertSizeToHumanReadable(itemState.contentLength)
                .rememberString(),
        )
    }
}
