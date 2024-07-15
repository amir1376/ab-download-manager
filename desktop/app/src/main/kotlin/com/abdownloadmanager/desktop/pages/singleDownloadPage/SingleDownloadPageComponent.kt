package com.abdownloadmanager.desktop.pages.singleDownloadPage

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.FileUtils
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.monitor.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

sealed interface SingleDownloadEffects {
    data object BringToFront : SingleDownloadEffects
}

@Immutable
data class SingleDownloadPagePropertyItem(
    val name: String,
    val value: String,
    val valueState: ValueType = ValueType.Normal,
) {
    enum class ValueType { Normal, Error, Success }
}
class SingleDownloadComponent(
    ctx: ComponentContext,
    val onDismiss: () -> Unit,
    val downloadId: Long,
) : BaseComponent(ctx),
    ContainsEffects<SingleDownloadEffects> by supportEffects(),
    KoinComponent {
    private val downloadSystem: DownloadSystem by inject()
    private val downloadMonitor: DownloadMonitor = downloadSystem.downloadMonitor
    private val downloadManager: DownloadManager = downloadSystem.downloadManager
    val itemStateFlow = downloadMonitor.downloadListFlow.map {
        it.firstOrNull { it.id == downloadId }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val showPartInfo = mutableStateOf(false)


    val extraDownloadInfo: StateFlow<List<SingleDownloadPagePropertyItem>> = itemStateFlow
        .filterNotNull()
        .map {
            buildList {
                add(SingleDownloadPagePropertyItem("Name", it.name))
                add(SingleDownloadPagePropertyItem("Status", createStatusString(it)))
                add(SingleDownloadPagePropertyItem("Size", convertSizeToHumanReadable(it.contentLength)))
                when (it) {
                    is CompletedDownloadItemState -> {
                    }

                    is ProcessingDownloadItemState -> {
                        add(SingleDownloadPagePropertyItem("Downloaded" , convertBytesToHumanReadable(it.progress).orEmpty()))
                        add(SingleDownloadPagePropertyItem("Speed" , convertSpeedToHumanReadable(it.speed)))
                        add(SingleDownloadPagePropertyItem("Remaining Time" , (it.remainingTime?.let { remainingTime ->
                            convertTimeRemainingToHumanReadable(remainingTime, TimeNames.ShortNames)
                        }.orEmpty())))
                        add(SingleDownloadPagePropertyItem(
                            "Resume Support",
                            when (it.supportResume) {
                                true -> "Yes"
                                false -> "No"
                                null -> "Unknown"
                            },
                            when (it.supportResume) {
                                true -> SingleDownloadPagePropertyItem.ValueType.Success
                                false -> SingleDownloadPagePropertyItem.ValueType.Error
                                null -> SingleDownloadPagePropertyItem.ValueType.Normal
                            }
                        ))
                    }
                }
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private fun createStatusString(it: IDownloadItemState): String {

        return when (val status = it.statusOrFinished()) {
            is DownloadJobStatus.Canceled -> {
                if (ExceptionUtils.isNormalCancellation(status.e)) {
                    "Paused"
                } else {
                    "Error"
                }
            }

            DownloadJobStatus.Downloading -> "Downloading"
            DownloadJobStatus.Finished -> "Finished"
            DownloadJobStatus.IDLE -> "IDLE"
            is DownloadJobStatus.PreparingFile -> "PreparingFile"
            DownloadJobStatus.Resuming -> "Resuming"
        }
    }

    fun openFolder() {
        val itemState = itemStateFlow.value
        scope.launch {
            if (itemState is CompletedDownloadItemState) {
                runCatching {
                    FileUtils.openFolderOfFile(File(itemState.folder, itemState.name))
                }
            }
            onDismiss()
        }
    }

    fun openFile() {
        val itemState = itemStateFlow.value
        scope.launch {
            if (itemState is CompletedDownloadItemState) {
                runCatching {
                    FileUtils.openFile(File(itemState.folder, itemState.name))
                }
            }
            onDismiss()
        }
    }

    fun toggle() {
        val state = itemStateFlow.value as ProcessingDownloadItemState ?: return
        scope.launch {
            if (state.status is DownloadJobStatus.IsActive) {
                downloadSystem.manualPause(downloadId)
            } else {
                downloadSystem.manualResume(downloadId)
            }
        }
    }

    fun close() {
        scope.launch {
            onDismiss()
        }
    }

    fun bringToFromt() {
        sendEffect(SingleDownloadEffects.BringToFront)
    }

    private val threadCount: MutableStateFlow<Int>
    private val speedLimit: MutableStateFlow<Long>

    init {
        val dItem = runBlocking {
            downloadManager.dlListDb.getById(downloadId)
        }
        threadCount = MutableStateFlow(
            dItem?.preferredConnectionCount ?:0
        )
        speedLimit = MutableStateFlow(dItem?.speedLimit ?: 0)
        downloadManager.listOfJobsEvents
            .filterIsInstance<DownloadManagerEvents.OnJobChanged>()
            .onEach { event ->
                threadCount.update {
                    event.downloadItem.preferredConnectionCount?:0
                }
                speedLimit.update {
                    event.downloadItem.speedLimit
                }
            }.launchIn(scope)


        threadCount
            .drop(1)
            .debounce(500)
            .onEach { count ->
                downloadManager.updateDownloadItem(downloadId) {
                    it.preferredConnectionCount = count.takeIf { it>0 }
                }
            }.launchIn(scope)
        speedLimit
            .drop(1)
            .debounce(500)
            .onEach { limit ->
                downloadManager.updateDownloadItem(downloadId) {
                    it.speedLimit = limit
                }
            }.launchIn(scope)
    }


    val settings by lazy {
        listOf(
            IntConfigurable(
                title = "Thread Count",
                description = "How much thread used to download this item 0 for default",
                backedBy = threadCount,
                describe = {
                    if (it==0){
                        "uses global setting"
                    }else{
                        "$it threads"
                    }
                },
                range = 0..32,
                renderMode = IntConfigurable.RenderMode.TextField,
            ),
            SpeedLimitConfigurable(
                title = "Speed limit",
                description = "speed limit for this download",
                backedBy = speedLimit,
                describe = {
                    if (it == 0L) {
                        "Unlimited"
                    } else {
                        convertSpeedToHumanReadable(it)
                    }
                },
            ),
        )
    }

    data class Config(
        val id: Long,
    )
}