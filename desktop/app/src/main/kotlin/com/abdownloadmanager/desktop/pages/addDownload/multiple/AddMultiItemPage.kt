package com.abdownloadmanager.desktop.pages.addDownload.multiple

import com.abdownloadmanager.desktop.pages.addDownload.shared.ShowAddToQueueDialog
import com.abdownloadmanager.desktop.pages.addDownload.shared.LocationTextField
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.desktop.ui.widget.ActionButton
import com.abdownloadmanager.desktop.ui.widget.Text
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddMultiItemPage(
    addMultiDownloadComponent: AddMultiDownloadComponent,
) {
    Column(Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 8.dp,bottom = 16.dp)
    ) {
        WithContentAlpha(1f) {
            Text(
                "Select Items you want to pick up for download",
                fontSize = myTextSizes.base
            )
        }
        Spacer(Modifier.height(8.dp))
        AddMultiDownloadTable(
            Modifier.weight(1f),
            addMultiDownloadComponent,
        )
        Footer(addMultiDownloadComponent)
    }
    if (addMultiDownloadComponent.showAddToQueue){
        ShowAddToQueueDialog(
            queueList = addMultiDownloadComponent.queueList.collectAsState().value,
            onQueueSelected = {
                addMultiDownloadComponent.requestAddDownloads(
                    it
                )
            },
            onClose = {
                addMultiDownloadComponent.closeAddToQueue()
            }
        )
    }
}

@Composable
fun Footer(component: AddMultiDownloadComponent) {
    Row(
        Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(
            text = "Add",
            onClick = {
                component.openAddToQueueDialog()
            },
            enabled = component.canClickAdd,
            modifier = Modifier,
        )
        Spacer(Modifier.width(8.dp))
        LocationTextField(
            text = component.folder.collectAsState().value,
            setText = {
                component.setFolder(it)
            },
            modifier = Modifier.weight(1f),
            lastUsedLocations = component.lastUsedLocations.collectAsState().value
        )
        Spacer(Modifier.width(8.dp))
        ActionButton(
            text = "Cancel",
            onClick = {
                component.requestClose()
            },
            modifier = Modifier,
        )
    }
}
