package ir.amirab.util.startup

import android.content.BroadcastReceiver
import android.content.Context

actual object Startup {
    fun getStartUpManager(
        context: Context,
        bootReceiver: Class<out BroadcastReceiver>,
    ): AndroidStartupManager {
        return AndroidStartupManager(context, bootReceiver)
    }
}
