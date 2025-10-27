package com.abdownloadmanager.android.pages.onboarding.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager(
    val permissions: List<AppPermission>,
    private val context: Context,
) {
    fun isReady(): Boolean {
        return permissions.all {
            isReady(it)
        }
    }

    fun isGranted(appPermission: AppPermission): Boolean {
        return appPermission.permissionChecker.isGranted(context, appPermission)
    }
    fun isReady(appPermission: AppPermission): Boolean {
        return appPermission.isOptional || isGranted(appPermission)
    }
}
