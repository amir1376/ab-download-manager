package ir.amirab.util.desktop.mac.event

import java.awt.Desktop
import java.awt.desktop.AppReopenedEvent
import java.awt.desktop.AppReopenedListener

object MacDockEventHandler {
    fun configure(onClickIcon: () -> Unit) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_EVENT_REOPENED)) {
            Desktop.getDesktop().addAppEventListener(object : AppReopenedListener {
                override fun appReopened(e: AppReopenedEvent?) {
                    onClickIcon.invoke()
                }
            })
        }
    }
}