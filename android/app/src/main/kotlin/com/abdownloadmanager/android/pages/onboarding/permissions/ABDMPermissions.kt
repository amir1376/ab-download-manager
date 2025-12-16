package com.abdownloadmanager.android.pages.onboarding.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.asStringSource

object ABDMPermissions {
    private fun getReadRightStorage(): AppPermission {

        return AppPermission(
            title = Res.string.permission_read_write_external_storage_title.asStringSource(),
            description = Res.string.permission_read_write_external_storage_reason.asStringSource(),
            icon = MyIcons.data,
            isOptional = false,
            permissions = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ),
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createManageStorage(): AppPermission {
        return AppPermission(
            title = Res.string.permissions_manage_storage_title.asStringSource(),
            description = Res.string.permissions_manage_storage_reason.asStringSource(),
            icon = MyIcons.data,
            isOptional = true,
            permissions = listOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            ),
            permissionRequestFactory = CustomPermissionActivityLauncher(::requestManageStoragePermission),
            permissionChecker = object : PermissionRequestChecker {
                override fun isGranted(
                    context: Context,
                    appPermission: AppPermission
                ) = Environment.isExternalStorageManager()
            }
        )
    }

    val StoragePermission = run {
        val isManageStorageAvailable = Build.VERSION.SDK_INT >= 30
        if (isManageStorageAvailable) {
            createManageStorage()
        } else {
            getReadRightStorage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createPostNotificationPermission(): AppPermission {
        return AppPermission(
            title = Res.string.permissions_post_notification_title.asStringSource(),
            description = Res.string.permissions_post_notification_reason.asStringSource(),
            icon = MyIcons.speaker,
            isOptional = false,
            permissions = listOf(
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    val all = buildList {
        add(StoragePermission)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(createPostNotificationPermission())
        }
    }
}
