package com.abdownloadmanager.android.pages.queue

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.ui.configurable.item.BooleanConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.DayOfWeekConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.IntConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.TimeConfigurable
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.newScopeBasedOn
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.queue.DownloadQueue
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalTime

class QueueConfigurationComponent(
    ctx: ComponentContext,
    id: Long,
    queueManager: QueueManager,
) : BaseComponent(ctx) {
    val downloadQueue = queueManager.queues.value.find {
        it.id == id
    }!!

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
                )
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
                        describe = { hourAndMinutesToString(it).asStringSource() },
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
                        describe = { hourAndMinutesToString(it).asStringSource() },
                        visible = enabledEndTimeFlow,
                    ),
                )
            ),
        )
    }
}

private fun hourAndMinutesToString(it: LocalTime): String {
    val hour = it.hour.toString().padStart(2, '0')
    val min = it.minute.toString().padStart(2, '0')
    return "$hour:$min"
}
