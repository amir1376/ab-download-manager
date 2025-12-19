package com.abdownloadmanager.android.pages.onboarding.permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import ir.amirab.util.ifThen

fun requestIgnoreBatteryOptimizationPermission(
    context: Context,
    startNewTask: Boolean = false,
) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = ("package:" + context.packageName).toUri()
        }.ifThen(startNewTask) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            .ifThen(startNewTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }
}
