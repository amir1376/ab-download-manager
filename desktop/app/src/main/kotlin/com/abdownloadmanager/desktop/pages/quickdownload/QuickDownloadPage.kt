package com.abdownloadmanager.desktop.pages.quickdownload

import com.abdownloadmanager.desktop.pages.addDownload.shared.LocationTextField
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.ui.widget.ActionButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuickDownloadPage(
    component: QuickDownloadComponent,
) {
    val fileName by component.fileName.collectAsState()
    val saveFolder by component.saveFolder.collectAsState()
    val isFinalizing by component.isFinalizing.collectAsState()

    WithContentColor(myColors.onBackground) {
        Column(
            Modifier
                .background(myColors.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Title
            Text(
                text = "Quick Download",
                fontSize = myTextSizes.lg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // File name
            com.abdownloadmanager.shared.ui.widget.MyTextFieldWithIcons(
                text = fileName,
                onTextChange = { component.updateFileName(it) },
                placeholder = "File name",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Save location
            LocationTextField(
                modifier = Modifier.fillMaxWidth(),
                text = saveFolder,
                setText = { component.updateSaveFolder(it) },
                lastUsedLocations = emptyList(),
                onRequestRemoveSaveLocation = {},
            )

            Spacer(Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButton(
                    text = "Cancel",
                    modifier = Modifier,
                    enabled = !isFinalizing,
                    onClick = { component.cancel() },
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    text = "Confirm",
                    modifier = Modifier,
                    enabled = !isFinalizing,
                    onClick = { component.confirm() },
                )
            }
        }
    }
}
