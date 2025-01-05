package ir.amirab.util.desktop.systemtray

import androidx.compose.runtime.Composable
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.action.MenuItem
import java.util.ServiceLoader

interface IComposeSystemTray {
    @Composable
    fun ComposeSystemTray(
        icon: IconSource,
        title: StringSource,
        menu: List<MenuItem>,
        onClick: () -> Unit,
    )

    companion object {
        val Instance by lazy {
            requireNotNull(
                ServiceLoader
                    .load(IComposeSystemTray::class.java)
                    .firstOrNull()
            ) {
                "Implementation for IComposeSystemTray not found"
            }
        }
    }
}