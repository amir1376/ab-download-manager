package com.abdownloadmanager.desktop.pages.queue

import com.abdownloadmanager.desktop.pages.settings.configurable.*
import com.abdownloadmanager.desktop.utils.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.utils.BaseComponent
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import com.abdownloadmanager.desktop.utils.newScopeBasedOn
import androidx.compose.runtime.toMutableStateList
import com.abdownloadmanager.desktop.storage.ExtraQueueSettingsStorage
import com.abdownloadmanager.resources.Res
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.createMutableStateFlowFromFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class QueueInfoComponent(
    ctx: ComponentContext,
    id: Long,
) : BaseComponent(ctx),
    KoinComponent {
    private val downloadMonitor: IDownloadMonitor by inject()
    private val queueManager: QueueManager by inject()
    val downloadQueue = queueManager.queues.value.find {
        it.id == id
    }!!


    val selectedListItems = MutableStateFlow(emptyList<Long>())

    val extraQueueSettingsStorage by inject<ExtraQueueSettingsStorage>()
    val extraDownloadItemSettingsFlow = createMutableStateFlowFromFlow(
        flow = extraQueueSettingsStorage.getExternalQueueSettingsAsFlow(
            id = id,
            initialEmit = false,
        ),
        initialValue = extraQueueSettingsStorage.getExtraQueueSettings(id),
        updater = {
            scope.launch {
                extraQueueSettingsStorage.setExtraQueueSettings(it)
            }
        },
        scope = scope,
    )

    init {
        downloadQueue.queueModel.map {
            it.queueItems
        }.onEach { l ->
            selectedListItems.value = selectedListItems.value.filter {
                it in l
            }
        }.launchIn(scope)
    }

    fun setSelectedItem(
        id: Long,
        selected: Boolean,
        singleSelect: Boolean,
    ) {
        selectedListItems.update {
            if (singleSelect) {
                if (selected) {
                    listOf(id)
                } else {
                    emptyList()
                }
            } else {
                it.toMutableStateList().also { mutableList ->
                    val contains = mutableList.contains(id)
                    if (contains && !selected) {
                        mutableList.remove(id)
                    } else if (!contains && selected) {
                        mutableList.add(id)
                    }
                }.toList()
            }

        }
    }


    val configurations: List<ConfigurableGroup> =
        createConfigurableList(downloadQueue, scope)


    private fun createConfigurableList(
        downloadQueue: DownloadQueue, parentScope: CoroutineScope,
    ): List<ConfigurableGroup> {
        val scope = newScopeBasedOn(parentScope)
        val enabledStartTimeFlow = downloadQueue.queueModel.mapStateFlow() {
            it.scheduledTimes.enabledStartTime
        }
        val enabledEndTimeFlow = downloadQueue.queueModel.mapStateFlow() {
            it.scheduledTimes.enabledEndTime
        }
        val enabledSchedulerFlow = combineStateFlows(enabledStartTimeFlow, enabledEndTimeFlow) { start, end ->
            start || end
        }
        return listOf(
            ConfigurableGroup(
                groupTitle = MutableStateFlow(Res.string.general.asStringSource()),
                nestedConfigurable = listOf(
                    StringConfigurable(
                        Res.string.name.asStringSource(),
                        Res.string.queue_name_help.asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.name
                            },
                            updater = { newValue ->
                                downloadQueue.setName(newValue)
                            },
                        ),
                        validate = {
                            it.length in 1..32
                        },
                        describe = {
                            Res.string.queue_name_describe
                                .asStringSourceWithARgs(
                                    Res.string.queue_name_describe_createArgs(
                                        value = it
                                    )
                                )
                        },
                    ),
                    IntConfigurable(
                        Res.string.queue_max_concurrent_download.asStringSource(),
                        Res.string.queue_max_concurrent_download_description.asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.maxConcurrent
                            },
                            updater = { newValue ->
                                downloadQueue.setMaxConcurrent(newValue)
                            },
                        ),
                        describe = { "$it".asStringSource() },
                        range = 1..32,
                        renderMode = IntConfigurable.RenderMode.TextField,
                    ),
                ),
            ),
            ConfigurableGroup(
                groupTitle = MutableStateFlow(Res.string.on_completion.asStringSource()),
                nestedConfigurable = listOf(
                    BooleanConfigurable(
                        Res.string.queue_automatic_stop.asStringSource(),
                        Res.string.queue_automatic_stop_description.asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.stopQueueOnEmpty
                            },
                            updater = { newValue ->
                                downloadQueue.setStopQueueOnEmpty(newValue)
                            },
                        ),
                        describe = {
                            if (it) Res.string.enabled.asStringSource()
                            else Res.string.disabled.asStringSource()
                        },
                    ),
                    BooleanConfigurable(
                        title = Res.string.queue_shutdown_on_completion.asStringSource(),
                        description = Res.string.queue_shutdown_on_completion_description.asStringSource(),
                        backedBy = extraDownloadItemSettingsFlow.mapTwoWayStateFlow(
                            map = {
                                it.powerActionTypeOnFinish != null
                            },
                            unMap = {
                                copy(
                                    powerActionTypeOnFinish = when (it) {
                                        true -> PowerActionConfig.Type.Shutdown
                                        false -> null
                                    },
                                )
                            },
                        ),
                        describe = {
                            if (it) Res.string.enabled.asStringSource()
                            else Res.string.disabled.asStringSource()
                        },
                    )
                ),
            ),
            ConfigurableGroup(
                groupTitle = MutableStateFlow(Res.string.queue_scheduler.asStringSource()),
                nestedVisible = enabledSchedulerFlow,
                mainConfigurable = BooleanConfigurable(
                    Res.string.queue_enable_scheduler.asStringSource(),
                    description = "".asStringSource(),
                    describe = { "".asStringSource() },
                    backedBy = createMutableStateFlowFromStateFlow(
                        flow = enabledSchedulerFlow,
                        scope = scope,
                        updater = { newValue ->
                            downloadQueue.setScheduledTimes {
                                copy(
                                    enabledStartTime = newValue,
                                    enabledEndTime = newValue,
                                )
                            }
                        }
                    ),
                ),
                nestedConfigurable = listOf(
                    DayOfWeekConfigurable(
                        Res.string.queue_active_days.asStringSource(),
                        Res.string.queue_active_days_description.asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.scheduledTimes.daysOfWeek
                            },
                            updater = { newValue ->
                                downloadQueue.setScheduledTimes {
                                    copy(daysOfWeek = newValue)
                                }
                            },
                        ),
                        validate = {
                            it.isNotEmpty()
                        },
                        describe = { "".asStringSource() },
                    ),
                    BooleanConfigurable(
                        Res.string.queue_scheduler_enable_auto_start_time.asStringSource(),
                        description = "".asStringSource(),
                        describe = { "".asStringSource() },
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = enabledStartTimeFlow,
                            updater = { newValue ->
                                downloadQueue.setScheduledTimes {
                                    copy(enabledStartTime = newValue)
                                }
                            },
                        ),
                    ),
                    TimeConfigurable(
                        Res.string.queue_scheduler_auto_start_time.asStringSource(),
                        "".asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.scheduledTimes.startTime
                            },
                            updater = {
                                downloadQueue.setScheduledTimes {
                                    copy(startTime = it)
                                }
                            },
                        ),
                        describe = { "".asStringSource() },
                        visible = enabledStartTimeFlow,
                    ),
                    BooleanConfigurable(
                        Res.string.queue_scheduler_enable_auto_stop_time.asStringSource(),
                        description = "".asStringSource(),
                        describe = { "".asStringSource() },
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = enabledEndTimeFlow,
                            updater = { newValue ->
                                downloadQueue.setScheduledTimes {
                                    copy(enabledEndTime = newValue)
                                }
                            },
                        ),
                    ),
                    TimeConfigurable(
                        Res.string.queue_scheduler_auto_stop_time.asStringSource(),
                        "".asStringSource(),
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.scheduledTimes.endTime
                            },
                            updater = { newValue ->
                                downloadQueue.setScheduledTimes {
                                    copy(endTime = newValue)
                                }
                            },
                        ),
                        describe = { "".asStringSource() },
                        visible = enabledEndTimeFlow,
                    ),
                )
            ),
        )
    }


    private val dls = downloadMonitor.downloadListFlow
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val downloadQueueItems = merge(
        downloadQueue.queueModel
            .map { it.queueItems }
            .distinctUntilChanged(),
        dls,
    ).map {
        getQueueItemsAsDownloadItem()
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private fun getQueueItemsAsDownloadItem(
    ): List<IDownloadItemState> {
        return downloadQueue.queueModel.value.queueItems.mapNotNull { dlId ->
            dls.value.find {
                it.id == dlId
            }
        }
    }

    fun deleteItems() {
        downloadQueue.removeFromQueue(selectedListItems.value)
    }

    fun moveDownItems() {
        downloadQueue.moveDown(selectedListItems.value)
    }

    fun moveUpItems() {
        downloadQueue.moveUp(selectedListItems.value)
    }

    fun swapItem(fromIndex: Int, toIndex: Int) {
        //maybe removed by queue itself during download completion
        val currentDraggingItem = runCatching {
            downloadQueue.getQueueItemFromOrder(fromIndex)
        }.getOrNull()
        val listOfIds = selectedListItems.value
            .let {
                if (currentDraggingItem != null && !it.contains(currentDraggingItem)) {
                    it.plus(currentDraggingItem)
                } else {
                    it
                }
            }


        downloadQueue.move(
            listOfIds, toIndex - fromIndex
        )
    }

}
