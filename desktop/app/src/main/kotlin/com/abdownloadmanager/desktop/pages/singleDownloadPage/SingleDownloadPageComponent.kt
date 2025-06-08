package com.abdownloadmanager.desktop.pages.singleDownloadPage

import androidx.compose.runtime.Immutable
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import arrow.optics.copy
import com.abdownloadmanager.desktop.pages.settings.ThreadCountLimitation
import com.abdownloadmanager.desktop.pages.settings.configurable.BooleanConfigurable
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.PageStatesStorage
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.*
import com.abdownloadmanager.shared.utils.FileIconProvider
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
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
    private val appScope: CoroutineScope by inject()
    private val downloadSystem: DownloadSystem by inject()
    private val appSettings: AppSettingsStorage by inject()
    private val appRepository: AppRepository by inject()
    private val applicationScope: CoroutineScope by inject()
    val fileIconProvider: FileIconProvider by inject()
    private val singleDownloadPageStateToPersist by lazy {
        get<PageStatesStorage>().downloadPage
    }
    private val downloadMonitor: IDownloadMonitor = downloadSystem.downloadMonitor
    private val downloadManager: DownloadManager = downloadSystem.downloadManager

    val itemStateFlow = MutableStateFlow<IDownloadItemState?>(null)
    private val globalShowCompletionDialog: StateFlow<Boolean> = appSettings.showDownloadCompletionDialog
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
            // downloadListFlow (combinedStateFlow { active + completed } downloads) emits null sometimes when download item removed from active downloads and also not exists in completed downloads yet (exactly at the moment that download finishes)
            // however if the download removed by user (item == null)  this component will be closed outside of this component we don't need to handle this case here
            // I explicitly filter nulls here to make onEach function predictable
            // if I fix downloadListFlow to not emit nulls I can remove this filter later
            .mapNotNull { it.firstOrNull { it.id == downloadId } }
            .distinctUntilChanged()
            .onEach {
                val item = it
                val previous = itemStateFlow.value
                if (previous is ProcessingDownloadItemState && item is CompletedDownloadItemState) {
                    // if It was opened to show progress
                    if (shouldShowCompletionDialog()) {
                        itemStateFlow.value = item
                    } else {
                        itemStateFlow.value = null
                        // app component tries to create this component if user want to auto open completion dialog and this component is not created yet
                        // so we keep this component active a while to prevent create new component
                        // this prevents opening this window if global [appSettings.showDownloadCompletionDialog] is true but user explicitly tells that he don't want to open completion dialog for this item
                        delay(100)
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
                        convertPositiveSizeToHumanReadable(it.contentLength, appRepository.sizeUnit.value)
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.download_page_downloaded_size.asStringSource(),
                        StringSource.CombinedStringSource(
                            buildList {
                                add(convertPositiveSizeToHumanReadable(it.progress, appRepository.sizeUnit.value))
                                if (it.percent != null) {
                                    add("(${it.percent}%)".asStringSource())
                                }
                            },
                            " "
                        )
                    )
                )
                add(
                    SingleDownloadPagePropertyItem(
                        Res.string.speed.asStringSource(),
                        convertPositiveSpeedToHumanReadable(it.speed, appRepository.speedUnit.value).asStringSource()
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
            is DownloadJobStatus.Retrying -> Res.string.retrying
        }.asStringSource()
    }

    fun openFolder() {
        val itemState = itemStateFlow.value
        appScope.launch {
            if (itemState is CompletedDownloadItemState) {
                downloadItemOpener.openDownloadItemFolder(downloadId)
            }
        }
        onDismiss()
    }

    fun openFile(alsoClose: Boolean = true) {
        val itemState = itemStateFlow.value
        appScope.launch {
            if (itemState is CompletedDownloadItemState) {
                runCatching {
                    downloadItemOpener.openDownloadItem(downloadId)
                }
            }
        }
        if (alsoClose) {
            onDismiss()
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

    fun cancel() {
        applicationScope.launch {
            val state = itemStateFlow.value as? ProcessingDownloadItemState
            if (state?.status is DownloadJobStatus.IsActive) {
                downloadSystem.manualPause(downloadId)
            }
        }
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
            .filter {
                it.downloadItem.id == dItem?.id
            }
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
                title = Res.string.download_item_settings_show_download_completion_dialog.asStringSource(),
                description = Res.string.download_item_settings_show_download_completion_dialog_description.asStringSource(),
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
                range = 0..ThreadCountLimitation.MAX_ALLOWED_THREAD_COUNT,
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
                        convertPositiveSpeedToHumanReadable(it, appRepository.speedUnit.value).asStringSource()
                    }
                },
            ),
        )
    }

    data class Config(
        val id: Long,
    )
}
