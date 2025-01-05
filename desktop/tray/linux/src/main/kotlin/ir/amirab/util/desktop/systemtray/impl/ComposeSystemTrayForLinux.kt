package ir.amirab.util.desktop.systemtray.impl

import com.google.auto.service.AutoService
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.desktop.systemtray.IComposeSystemTray

@AutoService(IComposeSystemTray::class)
class ComposeSystemTrayForLinux : IComposeSystemTray {
    override fun ComposeSystemTray(icon: IconSource, title: StringSource, menu: List<MenuItem>, onClick: () -> Unit) {
        TODO("Not yet implemented")
    }
}