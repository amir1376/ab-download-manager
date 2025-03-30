package ir.amirab.util.desktop.mac.event

import java.awt.Desktop
import java.awt.desktop.AppReopenedEvent
import java.awt.desktop.AppReopenedListener

object MacDockEventHandler {
    fun configure(onClickIcon: () -> Unit, onAboutClick: () -> Unit) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_EVENT_REOPENED)) {
            Desktop.getDesktop().apply {
                addAppEventListener(object : AppReopenedListener {
                    override fun appReopened(e: AppReopenedEvent?) {
                        onClickIcon.invoke()
                    }
                })
                if (isSupported(Desktop.Action.APP_ABOUT)) {
                    setAboutHandler { onAboutClick.invoke() }
                }
            }
        }
    }
}