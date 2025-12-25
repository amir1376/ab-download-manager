package com.abdownloadmanager.android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.arkivanov.decompose.retainedComponent
import ir.amirab.downloader.queue.QueueManager
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class MainActivity : ABDMActivity() {

    private val downloadItemOpener: DownloadItemOpener by inject()
    private val downloadSystem: DownloadSystem by inject()
    private val categoryManager: CategoryManager by inject()
    private val queueManager: QueueManager by inject()
    private val defaultCategories: DefaultCategories by inject()
    private val fileIconProvider: FileIconProvider by inject()
    private val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    private val json: Json by inject()
    private val updateManager: UpdateManager by inject()
    private val permissionManager: PermissionManager by inject()
    val mainComponent by lazy {
        retainedComponent {
            // make sure to not pass any activity to retained component
            MainComponent(
                ctx = it,
                context = applicationContext,
                downloadItemOpener = downloadItemOpener,
                downloadSystem = downloadSystem,
                categoryManager = categoryManager,
                queueManager = queueManager,
                defaultCategories = defaultCategories,
                fileIconProvider = fileIconProvider,
                json = json,
                downloaderInUiRegistry = downloaderInUiRegistry,
                perHostSettingsManager = perHostSettingsManager,
                applicationScope = applicationScope,
                appRepository = appRepository,
                updateManager = updateManager,
                permissionManager = permissionManager,
                languageManager = languageManager,
                themeManager = themeManager,
                abdmAppManager = abdmAppManager,
                onBoardingStorage = onBoardingStorage,
                homePageStorage = homePageStorage,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setABDMContent {
            MainContent(
                mainComponent = mainComponent,
            )
        }
    }

    override fun handleIntent(intent: Intent) {
        if (intent.action == ACTION_REVEAL_DOWNLOAD_IN_LIST) {
            val downloadId = intent.getLongExtra(DOWNLOAD_ID_KEY, -1)
                .takeIf { it >= 0 } ?: return
            mainComponent.revealDownload(downloadId)
        }
    }

    companion object {
        private const val DOWNLOAD_ID_KEY = "downloadId"
        private const val ACTION_REVEAL_DOWNLOAD_IN_LIST = "revealDownloadList"
        fun createRevelDownloadIntent(
            context: Context,
            downloadId: Long,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = ACTION_REVEAL_DOWNLOAD_IN_LIST
                putExtra(DOWNLOAD_ID_KEY, downloadId)
            }
        }
    }
}
