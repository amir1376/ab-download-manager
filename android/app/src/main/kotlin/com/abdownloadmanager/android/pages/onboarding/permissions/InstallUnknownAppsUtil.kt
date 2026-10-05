package com.abdownloadmanager.android.pages.onboarding.permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import ir.amirab.util.ifThen

/**
 * android 8+ needs a per app grant (in addition to the REQUEST_INSTALL_PACKAGES manifest permission)
 * before an app is allowed to hand an apk over to the package installer
 */
fun canInstallUnknownApps(
    context: Context
): Boolean {
    return context.packageManager.canRequestPackageInstalls()
}

fun requestInstallUnknownAppsPermission(
    context: Context,
    startNewTask: Boolean = false,
) {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = ("package:" + context.packageName).toUri()
        }.ifThen(startNewTask) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .ifThen(startNewTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}
