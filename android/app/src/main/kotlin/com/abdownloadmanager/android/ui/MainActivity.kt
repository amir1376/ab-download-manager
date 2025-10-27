package com.abdownloadmanager.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.core.app.ActivityCompat
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.util.ABDMServiceNotificationManager
import com.abdownloadmanager.android.util.AndroidUi
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.util.DownloadItemOpener
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.category.DefaultCategories
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestNotificationPermissionIfNeeded()
        val mainComponent = retainedComponent {
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
        setABDMContent {
            MainContent(
                mainComponent = mainComponent,
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
    }
}
