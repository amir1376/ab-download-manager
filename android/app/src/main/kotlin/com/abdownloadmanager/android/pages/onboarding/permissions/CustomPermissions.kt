package com.abdownloadmanager.android.pages.onboarding.permissions

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


class CustomPermissionActivityLauncher(
    private val openActivity: (Context) -> Unit,
) : PermissionRequestLauncherFactory {
    @Composable
    override fun rememberLauncher(
        appPermission: AppPermission,
        onNewResult: (Boolean) -> Unit
    ): PermissionRequestLauncher {
        val context = LocalContext.current
        return remember(context) {
            object : PermissionRequestLauncher {
                override fun launchPermissionRequest() {
                    openActivity(context)
                }
            }
        }
    }
}
