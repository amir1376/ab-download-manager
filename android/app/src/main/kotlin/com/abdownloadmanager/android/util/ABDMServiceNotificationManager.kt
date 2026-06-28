package com.abdownloadmanager.android.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abdownloadmanager.android.R
import com.abdownloadmanager.android.pages.singledownload.SingleDownloadPageActivity
import com.abdownloadmanager.android.service.KeepAliveServiceReason
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.android.ui.MainActivity
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.TimeNames
import com.abdownloadmanager.shared.util.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.shared.util.convertTimeRemainingToHumanReadable
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.StoppedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.util.compose.asStringSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ABDMServiceNotificationManager(
    private val context: Context,
    private val downloadMonitor: IDownloadMonitor,
    private val scope: CoroutineScope,
    private val downloadEvents: DownloadManagerMinimalControl,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
    private val appSettingsStorage: AppSettingsStorage,
) {
    private val _keepAliveServiceReason: MutableStateFlow<KeepAliveServiceReason?> = MutableStateFlow(null)

    fun setKeepAliveServiceReason(reason: KeepAliveServiceReason?) {
        _keepAliveServiceReason.value = reason
    }

    val notificationCreationTime = System.currentTimeMillis()
    private val notificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    init {
        registerReceiver()
    }

    fun registerReceiver() {
        ContextCompat.registerReceiver(
            context,
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        AndroidConstants.Intents.NOTIFICATION_DELETED -> {
                            onNotificationDismissed()
                        }
                    }
                }
            },
            IntentFilter().apply {
                this.addAction(AndroidConstants.Intents.NOTIFICATION_DELETED)
            },
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    fun updateNotificationWithDefaultValue() {
        notificationUpdateSignal.update {
            it + 1
        }
    }

    fun onNotificationDismissed() {
        if (notificationUpdateJob?.isActive == true) {
            updateNotificationWithDefaultValue()
        }
    }

    fun initNotificationChannel() {
        val notificationChanel = NotificationChannel(
            AndroidConstants.NOTIFICATION_DOWNLOAD_CHANEL_ID,
            AndroidConstants.NOTIFICATION_DOWNLOAD_CHANEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        )
        notificationChanel.setShowBadge(false)
        notificationManagerCompat.createNotificationChannel(notificationChanel)
    }

    private val notificationUpdateSignal = MutableStateFlow(0)

    val downloads = downloadMonitor
        .activeDownloadListFlow

    private var notificationUpdateJob: Job? = null
    fun startUpdatingNotifications() {
        synchronized(this) {
            notificationUpdateJob?.cancel()
            notificationUpdateJob = scope.launch {
                launch {
                    headlessComposeRuntime {
                        RenderNotifications(downloadMonitor)
                    }
                }
                launch {
                    postSystemNotificationForFinishedDownloads()
                }
            }

        }
    }

    private fun shouldShowDownloadCompletionNotifications(event: DownloadManagerEvents.OnJobCompleted): Boolean {
        val globallyEnabled = appSettingsStorage.showDownloadCompletionDialog.value
        if (!globallyEnabled) {
            return false
        }
        return event.context[ResumedBy]?.by == User
    }

    @SuppressLint("MissingPermission")
    suspend fun postSystemNotificationForFinishedDownloads() {
        coroutineScope {
            downloadEvents.listOfJobsEvents.onEach { event ->
                if (event is DownloadManagerEvents.OnJobCompleted) {
                    if (shouldShowDownloadCompletionNotifications(event)) {
                        launch {
                            // wait a while until we make sure download progress notification not sending anymore
                            delay(1000.milliseconds)
                            runCatching {
                                notificationManagerCompat.notify(
                                    getNotificationIdForDownloadItem(event.downloadItem.id),
                                    createFinishedDownloadItemNotification(event.downloadItem)
                                )
                            }.onFailure {
                                it.printStackTrace()
                            }
                        }
                    }
                }
            }.launchIn(this)
        }
    }

    fun stopUpdatingNotifications() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = null
    }

    private fun getNotificationIdForDownloadItem(downloadId: Long): Int {
        return AndroidConstants.SERVICE_NOTIFICATION_ID + 1 + downloadId.hashCode()
    }

    private fun dismissDownloadNotification(downloadId: Long) {
        notificationManagerCompat.cancel(getNotificationIdForDownloadItem(downloadId))
    }


    fun dismissNotification() {
        notificationManagerCompat.cancel(
            AndroidConstants.SERVICE_NOTIFICATION_ID,
        )
    }

    fun createMainNotification(): Notification {
        return createMainNotification(null, null)
    }

    fun createMainNotification(
        reason: KeepAliveServiceReason?,
        statusString: String?,
    ): Notification {
        val flagOfPendingIntent = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val serviceIsRunningText = Res.string.service_is_running.asStringSource().getString()
        val exit = Res.string.exit.asStringSource().getString()
        val stopAll = Res.string.stop_all.asStringSource().getString()
        val openMainActivityIntent = PendingIntent.getActivity(
            context,
            AndroidConstants.SERVICE_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java),
            flagOfPendingIntent
        )
        return NotificationCompat
            .Builder(context, AndroidConstants.NOTIFICATION_DOWNLOAD_CHANEL_ID)
            .setContentTitle(serviceIsRunningText)
            .setContentText(statusString)
            .setSmallIcon(R.drawable.ic_monochrome)
            // group
//            .setGroupSummary(true)
//            .setGroup(DOWNLOAD_GROUP_NAME)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setWhen(notificationCreationTime)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // prevent delete by user until we are active
            .setDeleteIntent(
                PendingIntent.getBroadcast(
                    context,
                    AndroidConstants.SERVICE_NOTIFICATION_ID,
                    Intent(AndroidConstants.Intents.NOTIFICATION_DELETED),
                    flagOfPendingIntent,
                )
            )
            // actions
            .setContentIntent(openMainActivityIntent)
            .addAction(
                0, exit, PendingIntent.getBroadcast(
                    context,
                    AndroidConstants.SERVICE_NOTIFICATION_ID,
                    Intent(AndroidConstants.Intents.EXIT_ACTION),
                    flagOfPendingIntent,
                )
            )
            .addAction(
                0, stopAll, PendingIntent.getBroadcast(
                    context,
                    AndroidConstants.SERVICE_NOTIFICATION_ID,
                    Intent(AndroidConstants.Intents.STOP_ALL_ACTION),
                    flagOfPendingIntent,
                )
            )
            .build()
    }

    fun createDownloadItemNotification(
        downloadItemState: ProcessingDownloadItemState
    ): Notification {
        val flagOfPendingIntent = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val title = downloadItemState.name

        val speedUnit = sizeAndSpeedUnitProvider.speedUnit.value
        val percent = downloadItemState.percent?.let { "$it%" }
        val eta = downloadItemState.remainingTime?.let {
            convertTimeRemainingToHumanReadable(it, TimeNames.ShortNames)
        }
        val speed = convertPositiveSpeedToHumanReadable(downloadItemState.speed, speedUnit)
        val statusString = listOfNotNull(speed, eta)
            .joinToString(" - ")
            .takeIf { it.isNotEmpty() }


        val openSingleDownloadActivityIntent = PendingIntent.getActivity(
            context,
            AndroidConstants.SERVICE_NOTIFICATION_ID,
            SingleDownloadPageActivity.createIntent(
                context, downloadItemState.id, true
            ),
            flagOfPendingIntent
        )
        val status = downloadItemState.status
        return NotificationCompat
            .Builder(context, AndroidConstants.NOTIFICATION_DOWNLOAD_CHANEL_ID)
            .setContentTitle(title)
            .setContentText(statusString)
            .setSubText(percent)
            .setProgress(100, downloadItemState.percent ?: 0, downloadItemState.percent == null)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setSmallIcon(R.drawable.ic_monochrome)
//            .setGroup(DOWNLOAD_GROUP_NAME)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setWhen(notificationCreationTime)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {
                if (status is DownloadJobStatus.IsActive) {
                    addAction(
                        0,
                        Res.string.pause.asStringSource().getString(),
                        PendingIntent.getBroadcast(
                            context,
                            AndroidConstants.SERVICE_NOTIFICATION_ID,
                            Intent(AndroidConstants.Intents.STOP_ACTION).apply {
                                putExtra(
                                    AndroidConstants.Intents.TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID,
                                    downloadItemState.id
                                )
                            },
                            flagOfPendingIntent,
                        )
                    )
                } else if (status is DownloadJobStatus.CanBeResumed) {
                    addAction(
                        0,
                        Res.string.resume.asStringSource().getString(),
                        PendingIntent.getBroadcast(
                            context,
                            AndroidConstants.SERVICE_NOTIFICATION_ID,
                            Intent(AndroidConstants.Intents.RESUME_ACTION).apply {
                                putExtra(
                                    AndroidConstants.Intents.TOGGLE_DOWNLOAD_ACTION_DOWNLOAD_ID,
                                    downloadItemState.id
                                )
                            },
                            flagOfPendingIntent,
                        )
                    )
                }
            }
            .setContentIntent(openSingleDownloadActivityIntent)
            .build()
    }

    fun createFinishedDownloadItemNotification(
        downloadItemState: IDownloadItem
    ): Notification {
        val flagOfPendingIntent = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val title = downloadItemState.name

        val statusString = Res.string.download_page_download_completed.asStringSource().getString()
        val openSingleDownloadActivityIntent = PendingIntent.getActivity(
            context,
            AndroidConstants.SERVICE_NOTIFICATION_ID,
            SingleDownloadPageActivity.createIntent(
                context, downloadItemState.id, true
            ),
            flagOfPendingIntent
        )
        return NotificationCompat
            .Builder(context, AndroidConstants.NOTIFICATION_DOWNLOAD_CHANEL_ID)
            .setContentTitle(title)
            .setContentText(statusString)
            .setCategory(Notification.CATEGORY_EVENT)
            .setSmallIcon(R.drawable.ic_monochrome)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .apply {
                downloadItemState.completeTime?.let {
                    setWhen(it)
                }
            }
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openSingleDownloadActivityIntent)
            .setAutoCancel(true)
            .build()
    }

    @Composable
    fun RenderNotifications(
        downloadMonitor: IDownloadMonitor
    ) {
        val activeDownloadListFlow = remember(downloadMonitor) {
            downloadMonitor.activeDownloadListFlow
                // don't hit notification rate limit
                .sample(500.milliseconds)
        }
        val notFinishedDownloads by activeDownloadListFlow.collectAsState(emptyList())
        val keepAliveServiceReason by _keepAliveServiceReason.collectAsState()
        val notifyUpdate by notificationUpdateSignal.collectAsState()
        CompositionLocalProvider(
            LocalNotificationUpdateSignal provides notifyUpdate
        ) {
            RenderMainNotification(
                reason = keepAliveServiceReason
            )
            RenderDownloadItemNotifications(
                remember(notFinishedDownloads) {
                    notFinishedDownloads.filter {
                        it.status is DownloadJobStatus.IsActive
                    }
                }
            )
        }
    }


    @Composable
    fun RenderMainNotification(
        reason: KeepAliveServiceReason?,
    ) {
        val statusString = reason?.rememberReasonString()
        LaunchedEffect(reason, statusString, LocalNotificationUpdateSignal.current) {
            @SuppressLint("MissingPermission")
            runCatching {
                notificationManagerCompat.notify(
                    AndroidConstants.SERVICE_NOTIFICATION_ID,
                    createMainNotification(reason, statusString)
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                dismissNotification()
            }
        }
    }

    @Composable
    fun RenderDownloadItemNotifications(
        activeDownloads: List<ProcessingDownloadItemState>
    ) {
        for (downloadItemState in activeDownloads) {
            key(downloadItemState.id) {
                RenderDownloadItemNotification(
                    downloadItemState
                )
            }
        }
    }

    @Composable
    fun RenderDownloadItemNotification(
        iDownloadItemState: ProcessingDownloadItemState
    ) {
        LaunchedEffect(iDownloadItemState, LocalNotificationUpdateSignal.current) {
            @SuppressLint("MissingPermission")
            runCatching {
                notificationManagerCompat.notify(
                    getNotificationIdForDownloadItem(iDownloadItemState.id),
                    createDownloadItemNotification(iDownloadItemState)
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
        DisposableEffect(iDownloadItemState.id) {
            onDispose {
                dismissDownloadNotification(iDownloadItemState.id)
            }
        }
    }

    companion object {
        private const val DOWNLOAD_GROUP_NAME = "Downloads"
    }
}

private val LocalNotificationUpdateSignal = compositionLocalOf<Int> {
    error("LocalNotificationUpdateSignal not provided")
}
