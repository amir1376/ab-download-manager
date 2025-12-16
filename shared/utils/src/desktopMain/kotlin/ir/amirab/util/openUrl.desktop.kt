package ir.amirab.util

import java.awt.Desktop
import java.net.URI

actual object URLOpener {
    actual fun openUrl(url: String) {
        runCatching {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        }
    }
}
