package com.abdownloadmanager.android.pages.add.multiple

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import com.abdownloadmanager.android.pages.category.CategorySheet
import com.abdownloadmanager.android.pages.newqueue.NewQueueSheet
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.android.util.activity.HandleActivityEffects
import com.abdownloadmanager.android.util.activity.getSerializedExtra
import com.abdownloadmanager.android.util.activity.putSerializedExtra
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadConfig
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.abdownloadmanager.shared.util.category.CategoryManager
import com.abdownloadmanager.shared.util.rememberChild
import ir.amirab.downloader.queue.QueueManager
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class AddMultiDownloadActivity : ABDMActivity() {
    private val json: Json by inject()
    private val downloadSystem: DownloadSystem by inject()
    private val appManager: ABDMAppManager by inject()
    private val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    private val lastSavedLocationsStorage: ILastSavedLocationsStorage by inject()
    private val selectQueueStorage: ISelectQueueStorage by inject()
    private val queueManager: QueueManager by inject()
    private val categoryManager: CategoryManager by inject()
    private val iconProvider: FileIconProvider by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myRetainedComponent = myRetainedComponent {
            val config = getComponentConfig(intent)
            val appManager = appManager
            val closeAddDownloadDialog = {
                this@myRetainedComponent.finishActivityAction()
            }
            AndroidAddMultiDownloadComponent(
                ctx = it,
                onRequestClose = closeAddDownloadDialog,
                lastSavedLocationsStorage = lastSavedLocationsStorage,
                selectQueueStorage = selectQueueStorage,
                id = config.id,
                queueManager = queueManager,
                categoryManager = categoryManager,
                downloadSystem = downloadSystem,
                onRequestAddMultipleItem = { items, queueId, categorySelectionMode ->
                    appManager.addDownloads(
                        items = items,
                        categorySelectionMode = categorySelectionMode,
                        queueId = queueId,
                    )
                },
                onRequestDownloadMultipleItem = { items, categorySelectionMode ->
                    appManager.startNewDownloads(
                        items = items,
                        categorySelectionMode = categorySelectionMode,
                    )
                },
                perHostSettingsManager = perHostSettingsManager,
                fileIconProvider = iconProvider,
                appRepository = appRepository,
                downloaderInUiRegistry = downloaderInUiRegistry,
            ).apply { addItems(config.newDownloads) }
        }
        val addDownloadComponent = myRetainedComponent.component
        setABDMContent {
            myRetainedComponent.HandleActivityEffects()
            AddMultiItemPage(addDownloadComponent)
            CategorySheet(
                categoryComponent = addDownloadComponent.categorySlot.rememberChild(),
                onDismiss = addDownloadComponent::closeCategoryDialog
            )
            NewQueueSheet(
                onQueueCreate = addDownloadComponent::createQueueWithName,
                isOpened = addDownloadComponent.showAddQueue.collectAsState().value,
                onCloseRequest = { addDownloadComponent.setShowAddQueue(false) },
            )
        }
    }

    private fun getComponentConfig(intent: Intent): AddDownloadConfig.MultipleAddConfig {
        runCatching {
            with(json) {
                intent.getSerializedExtra<AddDownloadConfig.MultipleAddConfig>(COMPONENT_CONFIG_KEY)
            }
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()?.let {
            return it
        }
        return AddDownloadConfig.MultipleAddConfig()
    }

    companion object {
        const val COMPONENT_CONFIG_KEY = "ComponentConfig"
        fun createIntent(
            context: Context,
            multipleAddConfig: AddDownloadConfig.MultipleAddConfig,
            json: Json,
        ): Intent {
            val intent = Intent(
                context,
                AddMultiDownloadActivity::class.java,
            )
            with(json) {
                intent.putSerializedExtra(COMPONENT_CONFIG_KEY, multipleAddConfig)
            }
            return intent
        }
    }
}
