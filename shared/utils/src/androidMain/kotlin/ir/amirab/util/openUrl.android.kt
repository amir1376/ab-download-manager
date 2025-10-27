package ir.amirab.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object URLOpener : KoinComponent {
    val context: Context by inject()
    actual fun openUrl(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
        )
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        runCatching {
            context.startActivity(intent)
        }
    }
}
