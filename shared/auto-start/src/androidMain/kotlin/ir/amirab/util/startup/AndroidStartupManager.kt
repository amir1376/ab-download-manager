package ir.amirab.util.startup

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
class AndroidStartupManager(
    private val context: Context,
    private val receiverClass: Class<out BroadcastReceiver>,
) : AbstractStartupManager() {
    override fun install() {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, receiverClass),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun uninstall() {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, receiverClass),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
