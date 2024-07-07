package ir.amirab.util.desktop.systemtray

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import ir.amirab.util.desktop.GlobalDensity
import ir.amirab.util.desktop.GlobalLayoutDirection
import ir.amirab.util.desktop.trayIconSize
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

@Composable
fun AwtTray(
    tooltip: String,
    icon: Painter,
    state: TrayState = rememberTrayState(),
    onClick: () -> Unit,
    //it is a dp offset on the screen
    onRightClick: (DpOffset) -> Unit,
) {
    val density by rememberUpdatedState(GlobalDensity)
    if (!isTraySupported) {
        DisposableEffect(Unit) {
            // We should notify developer, but shouldn't throw an exception.
            // If we would throw an exception, some application wouldn't work on some platforms at
            // all, if developer doesn't check that application crashes.
            //
            // We can do this because we don't return anything in Tray function, and following
            // code doesn't depend on something that is created/calculated in this function.
            System.err.println(
                "Tray is not supported on the current platform. " +
                        "Use the global property `isTraySupported` to check."
            )
            onDispose {}
        }
        return
    }

    val currentOnAction by rememberUpdatedState(onClick)
    val currentRightClick by rememberUpdatedState(onRightClick)
    val awtIcon = remember(icon) {
        // We shouldn't use LocalDensity here because Tray's density doesn't equal it. It
        // equals to the density of the screen on which it shows. Currently Swing doesn't
        // provide us such information, it only requests an image with the desired width/height
        // (see MultiResolutionImage.getResolutionVariant). Resources like svg/xml should look okay
        // because they don't use absolute '.dp' values to draw, they use values which are
        // relative to their viewport.
        icon.toAwtImage(GlobalDensity, GlobalLayoutDirection, trayIconSize)
    }

    val tray = remember {
        TrayIcon(awtIcon)
    }
    DisposableEffect(tray) {
        with(tray) {
            val doubleClickListener = object : MouseListener {
                override fun mouseClicked(p0: MouseEvent?) {
                    when (p0?.button) {
                        1-> currentOnAction()
                        3 -> {
                            with(density){
                                currentRightClick(
                                    DpOffset(p0.x.toDp(),p0.y.toDp())
                                )
                            }
                        }
                    }
                }

                override fun mousePressed(p0: MouseEvent?) {}
                override fun mouseReleased(p0: MouseEvent?) {}
                override fun mouseEntered(p0: MouseEvent?) {}
                override fun mouseExited(p0: MouseEvent?) {}
            }
            val actionListener = ActionListener {
                currentOnAction()
            }
            isImageAutoSize = true
            addMouseListener(doubleClickListener)
            addActionListener(actionListener)
            onDispose {
                removeActionListener(actionListener)
                removeMouseListener(doubleClickListener)
            }
        }
    }
    SideEffect {
        if (tray.image != awtIcon) tray.image = awtIcon
        if (tray.toolTip != tooltip) tray.toolTip = tooltip
    }

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        SystemTray.getSystemTray().add(tray)

        state.notificationFlow
            .onEach(tray::displayMessage)
            .launchIn(coroutineScope)

        onDispose {
            SystemTray.getSystemTray().remove(tray)
        }
    }
}

private fun TrayIcon.displayMessage(notification: Notification) {
    val messageType = when (notification.type) {
        Notification.Type.None -> TrayIcon.MessageType.NONE
        Notification.Type.Info -> TrayIcon.MessageType.INFO
        Notification.Type.Warning -> TrayIcon.MessageType.WARNING
        Notification.Type.Error -> TrayIcon.MessageType.ERROR
    }

    displayMessage(notification.title, notification.message, messageType)
}