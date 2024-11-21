package com.abdownloadmanager.desktop.pages.singleDownloadPage

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.mvi.ContainsEffects
import com.abdownloadmanager.desktop.utils.mvi.supportEffects
import arrow.optics.copy
import com.abdownloadmanager.desktop.pages.settings.configurable.BooleanConfigurable
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.monitor.*
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

sealed interface SingleDownloadEffects {
    data object BringToFront : SingleDownloadEffects
}

@Immutable
data class SingleDownloadPagePropertyItem(
    val name: StringSource,
    val value: StringSource,
    val valueState: ValueType = ValueType.Normal,
) {
    enum class ValueType { Normal, Error, Success }
}

class SingleDownloadComponent(
    ctx: ComponentContext,
    val downloadItemOpener: DownloadItemOpener,
    private val onDismiss: () -> Unit,
    val downloadId: Long,
) : BaseComponent(ctx),
    ContainsEffects<SingleDownloadEffects> by supportEffects(),
    KoinComponent {
    private val downloadSystem: DownloadSystem by inject()
    private val appSettings: AppSettingsStorage by inject()
    val fileIconProvider: FileIconProvider by inject()
    private val singleDownloadPageStateToPersist by lazy {
        get<PageStatesStorage>().downloadPage
    }
    private val downloadMonitor: IDownloadMonitor = downloadSystem.downloadMonitor
    private val downloadManager: DownloadManager = downloadSystem.downloadManager

    val itemStateFlow = MutableStateFlow<IDownloadItemState?>(null)
    private val globalShowCompletionDialog: StateFlow<Boolean> = appSettings.showCompletionDialog
    private val itemShouldShowCompletionDialog: MutableStateFlow<Boolean?> = MutableStateFlow(null as Boolean?)
    private val shouldShowCompletionDialog = combineStateFlows(
        globalShowCompletionDialog,
        itemShouldShowCompletionDialog,
    ) { global, item ->
        item ?: global
    }

    private fun shouldShowCompletionDialog(): Boolean {
        return shouldShowCompletionDialog.value
    }

    init {
        downloadMonitor
            .downloadListFlow
            .conflate()
            .onEach {
                val item = it.firstOrNull { it.id == downloadId }
                val previous = itemStateFlow.value
                if (previous is ProcessingDownloadItemState && item is CompletedDownloadItemState) {
                    // if It was opened to show progress
                    if (shouldShowCompletionDialog()) {
                        itemStateFlow.value = item
                    } else {
                        itemStateFlow.value = null
                        close()
                    }
                } else {
                    itemStateFlow.value = item
                }
            }.launchIn(scope)
    }

    private val _showPartInfo = MutableStateFlow(singleDownloadPageStateToPersist.value.showPartInfo)
    val showPartInfo = _showPartInfo.asStateFlow()
    fun setShowPartInfo(value: Boolean) {
        _showPartInfo.value = value
        singleDownloadPageStateToPersist.update {
            it.copy {
                SingleDownloadPageStateToPersist.showPartInfo.set(value)
            }
        }
    }

    // TODO this can be moved to a nested component to reduce system resource usage
    val extraDownloadProgressInfo: StateFlow<List<SingleDownloadPagePropertyItem>> = itemStateFlow
        .filterIsInstance<ProcessingDownloadItemState>()
        .map {
            buildList {
                add(SingleDownloadPagePropertyItem(Res.string.name.asStringSource(), it.name.asStringSource()))
                add(SingleDownloadPagePropertyItem(Res.string.status.asStringSource(), createStatusString(it)))
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.size.asStringSource(),
                        convertSizeToHumanReadable(it.contentLength)
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.download_page_downloaded_size.asStringSource(),
                        convertBytesToHumanReadable(it.progress).orEmpty().asStringSource()
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.speed.asStringSource(),
                        convertSpeedToHumanReadable(it.speed).asStringSource()
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.time_left.asStringSource(),
                        (it.remainingTime?.let { remainingTime ->
                            convertTimeRemainingToHumanReadable(remainingTime, TimeNames.ShortNames)
                        }.orEmpty()).asStringSource()
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.resume_support.asStringSource(),
                        when (it.supportResume) {
                            true -> Res.string.yes.asStringSource()
                            false -> Res.string.no.asStringSource()
                            null -> Res.string.unknown.asStringSource()
                        },
                        when (it.supportResume) {
                            true -> SingleDownloadPagePropertyItem.ValueType.Success
                            false -> SingleDownloadPagePropertyItem.ValueType.Error
                            null -> SingleDownloadPagePropertyItem.ValueType.Normal
                        }
                    )
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private fun createStatusString(it: IDownloadItemState): StringSource {

        return when (val status = it.statusOrFinished()) {
            is DownloadJobStatus.Canceled -> {
                if (ExceptionUtils.isNormalCancellation(status.e)) {
                    Res.string.paused
                } else {
                    Res.string.error
                }
            }

            DownloadJobStatus.Downloading -> Res.string.downloading
            DownloadJobStatus.Finished -> Res.string.finished
            DownloadJobStatus.IDLE -> Res.string.idle
            is DownloadJobStatus.PreparingFile -> Res.string.preparing_file
            DownloadJobStatus.Resuming -> Res.string.resuming
        }.asStringSource()
    }

    fun openFolder() {
        val itemState = itemStateFlow.value
        scope.launch {
            if (itemState is CompletedDownloadItemState) {
                downloadItemOpener.openDownloadItemFolder(downloadId)
            }
            onDismiss()
        }
    }

    fun openFile(alsoClose: Boolean = true) {
        val itemState = itemStateFlow.value
        scope.launch {
            if (itemState is CompletedDownloadItemState) {
                runCatching {
                    downloadItemOpener.openDownloadItem(downloadId)
                }
            }
            if (alsoClose) {
                onDismiss()
            }
        }
    }

    fun toggle() {
        val state = itemStateFlow.value as? ProcessingDownloadItemState ?: return
        scope.launch {
            if (state.status is DownloadJobStatus.IsActive) {
                downloadSystem.manualPause(downloadId)
            } else {
                downloadSystem.manualResume(downloadId)
            }
        }
    }

    fun resume() {
        val state = itemStateFlow.value as? ProcessingDownloadItemState ?: return
        scope.launch {
            if (state.status is DownloadJobStatus.CanBeResumed) {
                downloadSystem.manualResume(downloadId)
            }
        }
    }

    fun pause() {
        val state = itemStateFlow.value as? ProcessingDownloadItemState ?: return
        scope.launch {
            if (state.status is DownloadJobStatus.IsActive) {
                downloadSystem.manualPause(downloadId)
            }
        }
    }

    fun close() {
        scope.launch {
            onDismiss()
        }
    }

    fun bringToFront() {
        sendEffect(SingleDownloadEffects.BringToFront)
    }

    private val threadCount: MutableStateFlow<Int>
    private val speedLimit: MutableStateFlow<Long>

    init {
        val dItem = runBlocking {
            downloadManager.dlListDb.getById(downloadId)
        }
        threadCount = MutableStateFlow(
            dItem?.preferredConnectionCount ?: 0
        )
        speedLimit = MutableStateFlow(dItem?.speedLimit ?: 0)
        downloadManager.listOfJobsEvents
            .filterIsInstance<DownloadManagerEvents.OnJobChanged>()
            .onEach { event ->
                threadCount.update {
                    event.downloadItem.preferredConnectionCount ?: 0
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
                    it.preferredConnectionCount = count.takeIf { it > 0 }
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
            BooleanConfigurable(
                title = Res.string.download_item_settings_show_completion_dialog.asStringSource(),
                description = Res.string.download_item_settings_show_completion_dialog_description.asStringSource(),
                backedBy = itemShouldShowCompletionDialog.mapTwoWayStateFlow(
                    map = {
                        it ?: globalShowCompletionDialog.value
                    },
                    unMap = { it }
                ),
                describe = {
                    (if (it) Res.string.enabled
                    else Res.string.enabled)
                        .asStringSource()
                },
            ),
            IntConfigurable(
                title = Res.string.download_item_settings_thread_count.asStringSource(),
                description = Res.string.download_item_settings_thread_count_description.asStringSource(),
                backedBy = threadCount,
                describe = {
                    if (it == 0) {
                        Res.string.use_global_settings.asStringSource()
                    } else {
                        Res.string.download_item_settings_thread_count_describe
                            .asStringSourceWithARgs(
                                Res.string.download_item_settings_thread_count_describe_createArgs(
                                    count = it.toString()
                                )
                            )
                    }
                },
                range = 0..32,
                renderMode = IntConfigurable.RenderMode.TextField,
            ),
            SpeedLimitConfigurable(
                title = Res.string.download_item_settings_speed_limit.asStringSource(),
                description = Res.string.download_item_settings_speed_limit_description.asStringSource(),
                backedBy = speedLimit,
                describe = {
                    if (it == 0L) {
                        Res.string.unlimited.asStringSource()
                    } else {
                        convertSpeedToHumanReadable(it).asStringSource()
                    }
                },
            ),
        )
    }

    data class Config(
        val id: Long,
    )
}