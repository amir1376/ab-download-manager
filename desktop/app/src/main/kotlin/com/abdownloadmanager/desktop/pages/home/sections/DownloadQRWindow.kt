package com.abdownloadmanager.desktop.pages.home.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v2.WindowBoundsProvider
import androidx.compose.ui.window.v2.WindowPositionProvider
import androidx.compose.ui.window.v2.WindowSizeProvider
import androidx.compose.ui.window.v2.rememberWindowState
import com.abdownloadmanager.desktop.window.custom.CustomWindow
import com.abdownloadmanager.desktop.window.custom.rememberWindowController
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.CopyDownloadAsQRCode
import com.abdownloadmanager.shared.ui.widget.PrimaryMainActionButton
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.LocalUiScale
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import ir.amirab.util.compose.resources.myStringResource
import ir.amirab.util.desktop.screen.applyUiScale


@Composable
fun DownloadQRWindow(
    data: CopyDownloadAsQRCode,
    onClose: () -> Unit,
) {
    CustomWindow(
        state = rememberWindowState(
            initialBoundsProvider = WindowBoundsProvider(
                sizeProvider = WindowSizeProvider.Fixed(
                    size = DpSize(width = 400.dp, height = 400.dp)
                        .applyUiScale(LocalUiScale.current),
                ),
                positionProvider = WindowPositionProvider.CenteredOnScreen
            )
        ),
        windowController = rememberWindowController(
            title = data.title,
        ),
        onCloseRequest = onClose,
        content = {
            DownloadQRPage(data.qrCode)
        },
    )
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
        Modifier.fillMaxSize().padding(16.dp),
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryMainActionButton(
            text = myStringResource(Res.string.copy),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                ClipboardUtil.copy(data)
            }
        )
    }
}
