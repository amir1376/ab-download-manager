package com.abdownloadmanager.android.pages.downloaderror

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.android.util.AndroidDownloadItemOpener
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.android.util.activity.HandleActivityEffects
import com.abdownloadmanager.android.util.activity.getSerializedExtra
import com.abdownloadmanager.android.util.activity.putSerializedExtra
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderror.DownloadErrorComponent
import com.abdownloadmanager.shared.pagemanager.DownloadErrorDialogManager
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.shared.util.category.CategoryManager
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

class DownloadErrorActivity : ABDMActivity() {
    private val json: Json by inject()
    private val downloadSystem: DownloadSystem by inject()
    private val appManager: ABDMAppManager by inject()
    private val downloadItemOpener: AndroidDownloadItemOpener by inject()
    private val downloadErrorDialogManager: DownloadErrorDialogManager by inject()
    private val downloaderInUiRegistry: DownloaderInUiRegistry by inject()
    private val lastSavedLocationsStorage: ILastSavedLocationsStorage by inject()
    private val selectQueueStorage: ISelectQueueStorage by inject()
    private val queueManager: QueueManager by inject()
    private val categoryManager: CategoryManager by inject()
    private val iconProvider: FileIconProvider by inject()
    private val appContext: Context by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = getComponentConfig(intent)
        if (config == null) {
            finish()
            return
        }
        val myRetainedComponent = myRetainedComponent {
            // TODO consider use a factory to create DownloadErrorComponent
            // we may create memory leaks if we accidentally pass Activity::this into the component lambdas
            val closeDownloadErrorDialog = {
                this@myRetainedComponent.finishActivityAction()
            }
            DownloadErrorComponent(
                ctx = it,
                config = config,
                onClose = closeDownloadErrorDialog,
            )
        }
        val downloadErrorComponent = myRetainedComponent.component
        setABDMContent {
            myRetainedComponent.HandleActivityEffects()
            val dialogState = rememberResponsiveDialogState(false)
            dialogState.OnFullyDismissed {
                downloadErrorComponent.onClose()
            }
            LaunchedEffect(Unit) {
                // animate open after activity becomes fully open
                // is there a better way?
                delay(10.milliseconds)
                dialogState.show()
            }
            val onDismiss = { dialogState.hide() }
            ResponsiveDialog(
                dialogState,
                onDismiss
            ) {
                DownloadErrorDialog(downloadErrorComponent, onDismiss)
            }
        }
    }

    private fun getComponentConfig(intent: Intent): DownloadErrorComponent.DownloadErrorConfig? {
        return runCatching {
            with(json) {
                intent.getSerializedExtra<DownloadErrorComponent.DownloadErrorConfig>(COMPONENT_CONFIG_KEY)
            }
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

    companion object {
        const val COMPONENT_CONFIG_KEY = "ComponentConfig"
        fun createIntent(
            context: Context,
            config: DownloadErrorComponent.DownloadErrorConfig,
            json: Json,
        ): Intent {
            val intent = Intent(
                context,
                DownloadErrorActivity::class.java,
            )
            with(json) {
                intent.putSerializedExtra(COMPONENT_CONFIG_KEY, config)
            }
            return intent
        }
    }
}
