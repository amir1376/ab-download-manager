package com.abdownloadmanager.android.pages.singledownload

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import com.abdownloadmanager.android.storage.AndroidExtraDownloadItemSettings
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.android.util.AndroidDownloadItemOpener
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.android.util.activity.HandleActivityEffects
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.OnFullyDismissed
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.rememberResponsiveDialogState
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class SingleDownloadPageActivity : ABDMActivity() {
    private val downloadSystem: DownloadSystem by inject()
    private val downloadItemOpener: AndroidDownloadItemOpener by inject()
    private val iconProvider: FileIconProvider by inject()
    private val extraDownloadSettingsStorage: ExtraDownloadSettingsStorage<AndroidExtraDownloadItemSettings> by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadId = getDownloadId(intent)
        val myRetainedComponent = myRetainedComponent {
            val closeAddDownloadDialog = {
                this@myRetainedComponent.finishActivityAction()
            }
            AndroidSingleDownloadComponent(
                ctx = it,
                onDismiss = closeAddDownloadDialog,
                downloadId = downloadId,
                extraDownloadSettingsStorage = extraDownloadSettingsStorage,
                downloadSystem = downloadSystem,
                downloadItemOpener = downloadItemOpener,
                appSettings = appSettingsStorage,
                appRepository = appRepository,
                fileIconProvider = iconProvider,
                applicationScope = applicationScope,
            )
        }
        val singleDownloadComponent = myRetainedComponent.component
        setABDMContent {
            myRetainedComponent.HandleActivityEffects()
            ShowDownloadDialog(
                singleDownloadComponent,
            )
        }
    }

    private fun getDownloadId(intent: Intent): Long {
        return intent.getLongExtra(DOWNLOAD_ID, -1)
    }

    companion object {
        const val DOWNLOAD_ID = "downloadId"
        fun createIntent(
            context: Context,
            downloadId: Long,
        ): Intent {
            val intent = Intent(
                context,
                SingleDownloadPageActivity::class.java,
            )
            intent.putExtra(DOWNLOAD_ID, downloadId)
            return intent
        }
    }
}
