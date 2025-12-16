package com.abdownloadmanager.android.pages.onboarding.permissions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri


fun requestManageStoragePermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = ("package:" + context.packageName).toUri()
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
        }
    }
}
