package com.abdownloadmanager.desktop.pages.queue

import com.abdownloadmanager.desktop.pages.settings.configurable.*
import com.abdownloadmanager.desktop.pages.settings.configurable.widgets.ConfigurableGroup
import com.abdownloadmanager.desktop.utils.BaseComponent
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import com.abdownloadmanager.desktop.utils.newScopeBasedOn
import androidx.compose.runtime.toMutableStateList
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class QueueInfoComponent(
    ctx: ComponentContext,
    id: Long,
) : BaseComponent(ctx),
    KoinComponent {
    private val downloadMonitor: DownloadMonitor by inject()
    private val queueManager: QueueManager by inject()
    val downloadQueue = queueManager.queues.value.find {
        it.id == id
    }!!


    val selectedListItems = MutableStateFlow(emptyList<Long>())

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
        val enabledEndTimeFlow = downloadQueue.queueModel.mapStateFlow() {
            it.scheduledTimes.enabledEndTime
        }
        val enabledSchedulerFlow = downloadQueue.queueModel.mapStateFlow() {
            it.scheduledTimes.enabledStartTime
        }
        return listOf(
            ConfigurableGroup(
                groupTitle = MutableStateFlow("General"),
                nestedConfigurable = listOf(
                    StringConfigurable(
                        "Name",
                        "Specify A name for this queue",
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
                        describe = { "Queue name is $it" },
                    ),
                    IntConfigurable(
                        "Max Concurrent",
                        "Max download for this queue",
                        backedBy = createMutableStateFlowFromStateFlow(
                            scope = scope,
                            flow = downloadQueue.queueModel.mapStateFlow() {
                                it.maxConcurrent
                            },
                            updater = { newValue ->
                                downloadQueue.setMaxConcurrent(newValue)
                            },
                        ),
                        describe = { "${it}" },
                        range = 1..32,
                        renderMode = IntConfigurable.RenderMode.TextField,
                    ),
                    BooleanConfigurable(
                        "Automatic stop",
                        "Automatic stop queue when there is no item in it",
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
                            if (it) "Enabled"
                            else "Disabled"
                        },
                    ),
                ),
            ),
            ConfigurableGroup(
                groupTitle = MutableStateFlow("Scheduler"),
                nestedVisible = enabledSchedulerFlow,
                mainConfigurable = BooleanConfigurable(
                    "Enable Scheduler",
                    description = "",
                    describe = { "" },
                    backedBy = createMutableStateFlowFromStateFlow(
                        flow = enabledSchedulerFlow,
                        scope = scope,
                        updater = { newValue ->
                            downloadQueue.setScheduledTimes {
                                copy(enabledStartTime = newValue)
                            }
                        }
                    ),
                ),
                nestedConfigurable = listOf(
                    DayOfWeekConfigurable(
                        "Active days",
                        "which days schedulers function ?",
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
                        describe = { "" },
                    ),
                    TimeConfigurable(
                        "Auto Start download",
                        "",
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
                        describe = { "" },
                    ),
                    BooleanConfigurable(
                        "Enable Auto Stop",
                        description = "",
                        describe = { "" },
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
                        "Auto Stop download",
                        "",
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
                        describe = { "" },
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