package com.abdownloadmanager.android.pages.installapk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.abdownloadmanager.android.pages.onboarding.permissions.canInstallUnknownApps
import com.abdownloadmanager.android.pages.onboarding.permissions.requestInstallUnknownAppsPermission
import com.abdownloadmanager.android.pages.singledownload.SingleDownloadPageActivity
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.util.osfileutil.FileUtils
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Hands a completed apk download over to the system package installer.
 *
 * It has no UI and finishes as soon as it did its job.
 * Every failure is caught and falls back to the download page,
 * so tapping the notification can never become a dead click.
 */
class InstallApkTrampolineActivity : ComponentActivity(), KoinComponent {
    private val downloadSystem: DownloadSystem by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadId = intent.getLongExtra(SingleDownloadPageActivity.DOWNLOAD_ID, -1L)
        lifecycleScope.launch {
            val handled = runCatching {
                installApk(downloadId)
            }.onFailure {
                it.printStackTrace()
            }.isSuccess
            if (!handled) {
                openDownloadPage(downloadId)
            }
            finish()
        }
    }

    private suspend fun installApk(downloadId: Long) {
        val downloadItem = downloadSystem.getDownloadItemById(downloadId)
            ?: error("download item $downloadId not found")
        val file = downloadSystem.getDownloadFile(downloadItem)
        check(file.exists()) { "$file not found" }
        if (!canInstallUnknownApps(this)) {
            // the installer would refuse to install anything without this per app permission
            requestInstallUnknownAppsPermission(this)
            return
        }
        // this throws when the file can't be exposed by our FileProvider (ex: secondary sd volume)
        check(FileUtils.openFile(file)) { "can't start an installer for $file" }
    }

    private fun openDownloadPage(downloadId: Long) {
        startActivity(
            SingleDownloadPageActivity.createIntent(this, downloadId, true)
        )
    }

    companion object {
        fun createIntent(
            context: Context,
            downloadId: Long,
        ): Intent {
            return Intent(context, InstallApkTrampolineActivity::class.java)
                .putExtra(SingleDownloadPageActivity.DOWNLOAD_ID, downloadId)
        }
    }
}
