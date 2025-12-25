package com.abdownloadmanager.android.pages.singledownload

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.abdownloadmanager.android.storage.AndroidExtraDownloadItemSettings
import com.abdownloadmanager.android.ui.MainActivity
import com.abdownloadmanager.android.util.AndroidDownloadItemOpener
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.android.util.activity.HandleActivityEffects
import com.abdownloadmanager.shared.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import org.koin.core.component.inject

class SingleDownloadPageActivity : ABDMActivity() {
    private val downloadSystem: DownloadSystem by inject()
    private val downloadItemOpener: AndroidDownloadItemOpener by inject()
    private val iconProvider: FileIconProvider by inject()
    private val extraDownloadSettingsStorage: ExtraDownloadSettingsStorage<AndroidExtraDownloadItemSettings> by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadId = getDownloadId(intent)
        val isComingFromOutside = isComingFromExternalApplication(intent)
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
                comesFromExternalApplication = isComingFromOutside,
            )
        }
        val singleDownloadComponent = myRetainedComponent.component
        setABDMContent {
            myRetainedComponent.HandleActivityEffects()
            ShowDownloadDialog(
                singleDownloadComponent = singleDownloadComponent,
                onRequestShowInDownloads = {
                    startActivity(
                        MainActivity.createRevelDownloadIntent(
                            context = this,
                            singleDownloadComponent.downloadId,
                        )
                    )
                    finish()
                },
            )
        }
    }

    private fun getDownloadId(intent: Intent): Long {
        return intent.getLongExtra(DOWNLOAD_ID, -1)
    }
    private fun isComingFromExternalApplication(intent: Intent): Boolean {
        return intent.getBooleanExtra(COMING_FROM_OUTSIDE, true)
    }

    companion object {
        const val DOWNLOAD_ID = "downloadId"

        /**
         * if we are inside app then there is no need to add app icon shortcut
         */
        const val COMING_FROM_OUTSIDE = "comeFromOutside"
        fun createIntent(
            context: Context,
            downloadId: Long,
            comingFromOutside: Boolean,
        ): Intent {
            val intent = Intent(
                context,
                SingleDownloadPageActivity::class.java,
            )
            intent.putExtra(DOWNLOAD_ID, downloadId)
            intent.putExtra(COMING_FROM_OUTSIDE, comingFromOutside)
            return intent
        }
    }
}
