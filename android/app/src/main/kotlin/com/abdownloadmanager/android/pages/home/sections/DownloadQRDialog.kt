package com.abdownloadmanager.android.pages.home.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.android.ui.SheetHeader
import com.abdownloadmanager.android.ui.SheetTitle
import com.abdownloadmanager.android.ui.SheetUI
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pages.home.CopyDownloadAsQRCode
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.PrimaryMainActionButton
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ClipboardUtil
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.ResponsiveDialog
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.qrcode.QRCodeCreator
import com.abdownloadmanager.shared.util.qrcode.QRCodePlayer
import com.abdownloadmanager.shared.util.qrcode.QRCodeRenderController
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
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
                            data.title.rememberString()
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
                DownloadQRPage(data)
            }
        )
    }
}

@Composable
private fun DownloadQRPage(
    state: CopyDownloadAsQRCode,
) {
    val data = remember(state.qrCode) {
        QRCodeCreator.createQRCode(state.qrCode)
    }
    val qrCodeController = remember(data) {
        QRCodeRenderController(data.asList())
    }
    val scope = rememberCoroutineScope()
    val qrCodePlayer = remember(qrCodeController, scope) {
        QRCodePlayer(qrCodeController, scope)
    }
    val isPlaying = qrCodePlayer.isPlaying
    DisposableEffect(qrCodePlayer) {
        qrCodePlayer.play()
        onDispose {
            qrCodePlayer.close()
        }
    }
    val currentItem = qrCodeController.currentIndex
    val totalItems = qrCodeController.total
    val painter = rememberQrCodePainter(
        data = qrCodeController.currentQRCode,
        colors = QrColors(
            dark = QrBrush.solid(Color.Black),
            light = QrBrush.solid(Color.White),
        ),
    )
    Column {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .aspectRatio(1f)
                    .weight(1f, fill = false)
                    .clip(myShapes.defaultRounded)
                    .background(Color.White)
                    .padding(12.dp)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            if (!qrCodeController.isSingle()) {
                val shape = CircleShape
                RenderParts(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mySpacings.thumbSize / 2)
                        .clip(shape)
                        .border(Dp.Hairline, myColors.onSurface / 0.15f, shape)
                        .background(myColors.background, shape),
                    total = totalItems,
                    current = currentItem,
                    onItemSelected = { selectedIndex ->
                        qrCodeController.selectCurrentIndex(selectedIndex)
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            // player controls
            if (!qrCodeController.isSingle()) {
                Row(
                    Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconActionButton(
                        contentDescription = Res.string.back.asStringSource(),
                        icon = MyIcons.back,
                        onClick = {
                            qrCodeController.previous()
                        }
                    )
                    IconActionButton(
                        contentDescription = Res.string.pause.asStringSource(),
                        icon = if (isPlaying) {
                            MyIcons.pause
                        } else {
                            MyIcons.resume
                        },
                        onClick = {
                            qrCodePlayer.togglePlayback()
                        }
                    )
                    IconActionButton(
                        contentDescription = Res.string.next.asStringSource(),
                        icon = MyIcons.next,
                        onClick = {
                            qrCodeController.next()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            PrimaryMainActionButton(
                text = myStringResource(Res.string.copy),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    ClipboardUtil.copy(data.getFull())
                }
            )
        }
    }
}

@Composable
private fun RenderParts(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier,
    onItemSelected: (Int) -> Unit,
    selectedColor: Color = myColors.primary,
) {
    Row(modifier) {
        repeat(total) {
            key(it) {
                Box(
                    Modifier
                        .clickable {
                            onItemSelected(it)
                        }
                        .fillMaxHeight()
                        .weight(1f)
                        .background(
                            if (it == current) {
                                selectedColor
                            } else {
                                Color.Transparent
                            }
                        )
                )
            }
        }
    }
}
