package com.abdownloadmanager.android.pages.home.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.CopyDownloadAsQRCode
import com.abdownloadmanager.shared.ui.widget.PrimaryMainActionButton
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun DownloadQRDialog(
    data: CopyDownloadAsQRCode,
    onClose: () -> Unit,
) {
    val state = rememberResponsiveDialogState(false)
    LaunchedEffect(Unit) {
        state.show()
    }
    state.OnFullyDismissed(onClose)
    val closeDialog = state::hide
    ResponsiveDialog(
        rememberResponsiveDialogState(true),
        onDismiss = closeDialog
    ) {
        SheetUI(
            header = {
                SheetHeader(
                    headerTitle = {
                        SheetTitle(
                            data.title
                        )
                    },
                    headerActions = {
                        TransparentIconActionButton(
                            MyIcons.close,
                            contentDescription = Res.string.close.asStringSource(),
                            onClick = closeDialog,
                        )
                    }
                )
            },
            content = {
                DownloadQRPage(data.qrCode)
            }
        )
    }
}

@Composable
private fun DownloadQRPage(
    data: String,
) {
    val painter = rememberQrCodePainter(
        data = data,
        colors = QrColors(
            dark = QrBrush.solid(myColors.onBackground),
            light = QrBrush.solid(myColors.background),
        ),
    )
    Column(
        Modifier
            .padding(16.dp),
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(1f)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryMainActionButton(
            text = myStringResource(Res.string.copy),
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                ClipboardUtil.copy(data)
            }
        )
    }
}
