package com.abdownloadmanager.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.service.DownloadSystemService
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.android.util.notification.playNotificationSoundIfAllowed
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.ui.widget.MessageDialogType
import com.abdownloadmanager.shared.ui.widget.NotificationManager
import com.abdownloadmanager.shared.ui.widget.NotificationType
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.category.CategorySelectionMode
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.NewDownloadItemProps
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.combineStringSources
import ir.amirab.util.coroutines.launchWithDeferred
import ir.amirab.util.guardedEntry
import ir.amirab.util.suspendGuardedEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.util.UUID
import kotlin.system.exitProcess

class ABDMAppManager(
    private val context: Context,
    private val scope: CoroutineScope,
    val downloadSystem: DownloadSystem,
    val permissionManager: PermissionManager,
    val notificationManager: NotificationManager,
    val serviceNotificationManager: ABDMServiceNotificationManager,
    private val appSettingsStorage: AppSettingsStorage,
) : KoinComponent, NotificationSender {
    private var booted = guardedEntry()
    private var downloadSystemBooted = suspendGuardedEntry()
    fun isSoundAllowed(): Boolean {
        return appSettingsStorage.notificationSound.value
    }

    fun boot() {
        booted.action {
            registerAsFallbackNotification()
        }
    }

    fun canStartDownloadEngine(): Boolean {
        return permissionManager.isReady()
    }

    suspend fun startDownloadSystem() {
        downloadSystemBooted.action {
            downloadSystem.boot()
            registerReceivers()
            registerDownloadEventNotifications()
        }
    }

    private var shouldShowToastsNotifications = MutableStateFlow(true)
    fun setNotificationsHandledInUi(shownInUi: Boolean) {
        shouldShowToastsNotifications.value = !shownInUi
    }

    private fun registerAsFallbackNotification(): () -> Unit {
        val context = context
        var lastNotificationSound = 0L
        val job = scope.headlessComposeRuntime {
            val scope = rememberCoroutineScope()
            val notifications by notificationManager.activeNotificationList.collectAsState()
            val shouldShowNotifications by shouldShowToastsNotifications.collectAsState()
            if (!shouldShowNotifications) {
                return@headlessComposeRuntime
            }
            notifications
                .firstOrNull()?.let { notification ->
                    DisposableEffect(notification) {
                        val title = notification.title.getString()
                        val description = notification.description.getString()
                        val iconText = when (notification.notificationType) {
                            NotificationType.Error -> "❌"
                            NotificationType.Info -> "ℹ\uFE0F"
                            is NotificationType.Loading -> "⏳"
                            NotificationType.Success -> "✔\uFE0F"
                            NotificationType.Warning -> "⚠\uFE0F"
                        }
                        val fullTitle = "$iconText $title - $description"
                        val toastJob = scope.launch(Dispatchers.Main) {
                            val toast = Toast.makeText(
                                context,
                                fullTitle,
                                Toast.LENGTH_LONG,
                            )
                            if (isSoundAllowed()) {
                                val now = System.currentTimeMillis()
                                val sinceLastSoundMillis = now - lastNotificationSound
                                // don't repeatedly play notification!
                                if (sinceLastSoundMillis > 5_000) {
                                    runCatching {
                                        playNotificationSoundIfAllowed(context)
                                        lastNotificationSound = now
                                    }.onFailure {
                                        it.printStackTrace()
                                    }
                                }
                            }
                            toast.show()
                            currentCoroutineContext().job.invokeOnCompletion {
                                it?.let {
                                    toast.cancel()
                                }
                            }
                        }
                        onDispose {
                            scope.launch(Dispatchers.Main) {
                                toastJob.cancel()
                            }
                        }
                    }
                }
        }
        return { job.cancel() }
    }

    private fun registerDownloadEventNotifications() {
        downloadSystem.downloadEvents.onEach {
            onNewDownloadEvent(it)
        }.launchIn(scope)
    }

    private fun onNewDownloadEvent(it: DownloadManagerEvents) {
        if (it.context[ResumedBy]?.by !is User) {
            //only notify events that is started by user
            return
        }
        if (it is DownloadManagerEvents.OnJobCanceled) {
            val exception = it.e
            if (ExceptionUtils.isNormalCancellation(exception)) {
                return
            }
            var isMaxTryReachedError = false
            val actualCause = if (exception is TooManyErrorException) {
                isMaxTryReachedError = true
                exception.findActualDownloadErrorCause()
            } else exception
            if (ExceptionUtils.isNormalCancellation(actualCause)) {
                return
            }
            val prefix = if (isMaxTryReachedError) {
                "Too Many Error: "
            } else {
                "Error: "
            }.asStringSource()
            val reason = actualCause.message?.asStringSource() ?: Res.string.unknown.asStringSource()
            sendNotification(
                "downloadId=${it.downloadItem.id}",
                description = it.downloadItem.name.asStringSource(),
                title = listOf(prefix, reason).combineStringSources(),
                type = NotificationType.Error,
            )
        }
        if (it is DownloadManagerEvents.OnJobCompleted) {
            sendNotification(
                tag = "downloadId=${it.downloadItem.id}",
                description = it.downloadItem.name.asStringSource(),
                title = Res.string.finished.asStringSource(),
                type = NotificationType.Success,
            )
        }
    }

    suspend fun awaitDownloadEngineBoot() {
        downloadSystemBooted.awaitDone()
    }

    private fun registerReceivers(): () -> Unit {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    AndroidConstants.Intents.STOP_ACTION -> {
                        intent
                            .getLongExtra(AndroidConstants.Intents.TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID, -1)
                            .takeIf { it > -1 }
                            ?.let {
                                scope.launch {
                                    downloadSystem.manualPause(it)
                                }
                            }
                    }

                    AndroidConstants.Intents.RESUME_ACTION -> {
                        intent
                            .getLongExtra(AndroidConstants.Intents.TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID, -1)
                            .takeIf { it > -1 }
                            ?.let {
                                scope.launch {
                                    downloadSystem.manualResume(it)
                                }
                            }
                    }

                    AndroidConstants.Intents.TOGGLE_ACTION -> {
                        intent
                            .getLongExtra(AndroidConstants.Intents.TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID, -1)
                            .takeIf { it > -1 }
                            ?.let {
                                scope.launch {
                                    TODO("Toggle action not implemented yet")
                                }
                            }
                    }

                    AndroidConstants.Intents.STOP_ALL_ACTION -> {
                        scope.launch {
                            downloadSystem.stopAnything()
                        }
                    }

                    AndroidConstants.Intents.EXIT_ACTION -> {
                        val job = scope.launch {
                            downloadSystem.stopAnything()
                            stopOurService()
                        }
                        job.invokeOnCompletion {
                            exitProcess(0)
                        }
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(AndroidConstants.Intents.TOGGLE_ACTION)
                addAction(AndroidConstants.Intents.RESUME_ACTION)
                addAction(AndroidConstants.Intents.STOP_ACTION)
                addAction(AndroidConstants.Intents.STOP_ALL_ACTION)
                addAction(AndroidConstants.Intents.EXIT_ACTION)
            },
            ContextCompat.RECEIVER_EXPORTED,
        )
        return {
            for (receiver in listOf(receiver)) {
                context.unregisterReceiver(receiver)
            }
        }
    }

    suspend fun startOurService() {
        awaitDownloadEngineBoot()
        val intent = Intent(context, DownloadSystemService::class.java)
        withContext(Dispatchers.Main) {
            ContextCompat.startForegroundService(context, intent)
        }
    }

    suspend fun stopOurService() {
        awaitDownloadEngineBoot()
        val intent = Intent(context, DownloadSystemService::class.java)
        withContext(Dispatchers.Main) {
            context.stopService(intent)
        }
    }

    fun startNewDownload(
        item: NewDownloadItemProps,
        categoryId: Long?,
    ): Deferred<Long> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newDownload = item,
                queueId = DefaultQueueInfo.ID,
                categoryId = categoryId,
            ).also {
                downloadSystem.manualResume(it)
            }
        }
    }

    fun addDownload(
        item: NewDownloadItemProps,
        queueId: Long?,
        categoryId: Long?,
    ): Deferred<Long> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newDownload = item,
                queueId = queueId,
                categoryId = categoryId,
            )
        }
    }

    fun addDownloads(
        items: List<NewDownloadItemProps>,
        categorySelectionMode: CategorySelectionMode?,
        queueId: Long?,
    ): Deferred<List<Long>> {
        return scope.launchWithDeferred {
            downloadSystem.addDownload(
                newItemsToAdd = items,
                queueId = queueId,
                categorySelectionMode = categorySelectionMode,
            )
        }
    }

    override fun sendDialogNotification(
        title: StringSource,
        description: StringSource,
        type: MessageDialogType
    ) {
        sendNotification(
            title = title,
            description = description,
            type = when (type) {
                MessageDialogType.Info -> NotificationType.Info
                MessageDialogType.Error -> NotificationType.Error
                MessageDialogType.Success -> NotificationType.Success
                MessageDialogType.Warning -> NotificationType.Warning
            },
            tag = UUID.randomUUID(),
        )
    }

    override fun sendNotification(
        tag: Any,
        title: StringSource,
        description: StringSource,
        type: NotificationType
    ) {
        scope.launch {
            notificationManager.showNotification(
                title,
                description,
                delay = 5_000,
                type = type,
            )
        }
    }

    /**
     * in case of the notification permission is granted recently
     * we ask service notification manager to repost the notification
     */
    fun repostServiceNotification() {
        serviceNotificationManager.updateNotificationWithDefaultValue()
    }
}
