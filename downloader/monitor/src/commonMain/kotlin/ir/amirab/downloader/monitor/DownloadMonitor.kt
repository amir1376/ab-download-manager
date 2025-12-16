package ir.amirab.downloader.monitor

import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.utils.intervalFlow
import ir.amirab.util.flow.saved
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.util.flow.combineStateFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DownloadMonitor(
    private val downloadManager: DownloadManager,
    downloadItemStateFactory: Lazy<DownloadItemStateFactory<IDownloadItem, DownloadJob>>
) : IDownloadMonitor {
    val downloadItemStateFactory by downloadItemStateFactory

    private val scope = CoroutineScope(SupervisorJob())

    private var avSpeedCollectorJob: Job? = null
    override var useAverageSpeed = false
        set(value) {
            if (value == field) return
            field = value
            //always cancel current job
            updateUseAverageSpeedFlow(value)
        }

    private fun updateUseAverageSpeedFlow(useAverageSpeed: Boolean) {
        avSpeedCollectorJob?.cancel()
        avSpeedCollectorJob = if (useAverageSpeed) {
            // enabling average speed calculator flow only if nececary
            // it will add a subscriber count into averageSpeedFlow and causes to start working
            scope.launch { averageDownloadSpeedFlow.collect() }
        } else {
            // disable average speed
            null
        }
    }

    override val activeDownloadListFlow = MutableStateFlow<List<ProcessingDownloadItemState>>(emptyList())
    override val completedDownloadListFlow = MutableStateFlow<List<CompletedDownloadItemState>>(emptyList())
    override val downloadListFlow: StateFlow<List<IDownloadItemState>> =
        combineStateFlows(activeDownloadListFlow, completedDownloadListFlow) { a, b -> a + b }

    init {
        activeDownloadListFlow
            .subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .onEach { isUsed ->
                if (isUsed) {
                    downloadManager.awaitBoot()
                    startUpdateActiveDownloadList()
                    startSpeedMeter()
                } else {
                    stopUpdateDownloadList()
                    stopSpeedMeter()
                }
            }
            .launchIn(scope)
        completedDownloadListFlow
            .subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .onEach { isUsed ->
                if (isUsed) {
                    //wait for boot to initialize part downloaders!
                    downloadManager.awaitBoot()
                    startUpdateCompletedList()
                } else {
                    stopUpdateCompletedList()
                }
            }.launchIn(scope)
    }


    private val downloadSpeedFlow = MutableStateFlow<SpeedAtTime>(SpeedAtTime.empty())

    private val averageDownloadSpeedFlow = downloadSpeedFlow
        .saved(5)
        .map { lastSpeedHistory ->
            val lastSpeeds = lastSpeedHistory.lastOrNull()?.speed ?: return@map SpeedAtTime.empty()
            SpeedAtTime(
                lastSpeeds
                    .mapValues { (id, _) ->
                        lastSpeedHistory
                            .mapNotNull { it.speed.getOrElse(id) { null } }
                            .average()
                            .toLong()
                    }
            )
        }.stateIn(scope, SharingStarted.WhileSubscribed(), SpeedAtTime.empty())

    private var speedMeterJob: Job? = null
    private fun startSpeedMeter() {
        speedMeterJob?.cancel()
        updateUseAverageSpeedFlow(useAverageSpeed)
        speedMeterJob = scope.launch {
            var lastWrites = mapOf<Long, Long>()
            while (isActive) {
                val newWrites = downloadManager.downloadJobs.associate {
                    it.id to it.getDownloadedSize()
                }
                downloadSpeedFlow.value = SpeedAtTime(
                    newWrites.mapValues { (id, newWrite) ->
                        val lastWrittenData = lastWrites.getOrElse(id) { null }
                        val newSpeed = when {
                            lastWrittenData != null -> {
                                if (newWrite < lastWrittenData) {
                                    // maybe download was restarted our lastWrittenData is not valid anymore
                                    newWrite
                                } else {
                                    newWrite - lastWrittenData
                                }
                            }

                            else -> {
                                // this item seen for the first time
                                0
                            }
                        }
                        newSpeed
                    }
                )
                lastWrites = newWrites
                delay(1_000)
            }
        }
    }

    private fun stopSpeedMeter() {
        speedMeterJob?.cancel()
        speedMeterJob = null
        avSpeedCollectorJob?.cancel()
        avSpeedCollectorJob = null
    }


    private fun getPreferedSpeedFlow(): StateFlow<SpeedAtTime> {
        return when {
            useAverageSpeed -> averageDownloadSpeedFlow
            else -> downloadSpeedFlow
        }

    }

    private fun getSpeedOf(id: Long): Long {
        val speed = getPreferedSpeedFlow().value.speed.getOrElse(id) { -1 }
//        println("speed of $id is $speed")
        return speed
    }

    private var completedDownloadListUpdaterJob: Job? = null
    private fun startUpdateCompletedList() {
        completedDownloadListUpdaterJob?.cancel()
        completedDownloadListUpdaterJob = scope.launch {
            val initialData = downloadManager.getDownloadList().filter {
                it.status == DownloadStatus.Completed
            }.map {
                downloadItemStateFactory.createCompletedDownloadItemStateFromDownloadItem(it)
            }
            completedDownloadListFlow.update { initialData }
            downloadManager.listOfJobsEvents
                .onEach { event ->

                    when (event) {
                        is DownloadManagerEvents.OnJobCompleted -> {
                            val item = downloadItemStateFactory
                                .createCompletedDownloadItemStateFromDownloadItem(event.downloadItem)
                            completedDownloadListFlow.update { current ->
                                //replace if this id is already in the completed list
                                // this is happened when we are creating a job from a completed download
                                val found = current.find { it.id == item.id }
                                if (found != null) {
                                    current.map {
                                        if (it.id == item.id) {
                                            item
                                        } else {
                                            it
                                        }
                                    }
                                } else {
                                    current + item
                                }
                            }
                        }

                        is DownloadManagerEvents.OnJobRemoved -> {
                            completedDownloadListFlow.update {
                                it.filter {
                                    it.id != event.downloadItem.id
                                }
                            }
                        }

                        is DownloadManagerEvents.OnJobChanged -> {
                            val shouldAdd = event.downloadItem.status == DownloadStatus.Completed
                            completedDownloadListFlow.update { current ->
                                if (shouldAdd) {
                                    val item =
                                        downloadItemStateFactory.createCompletedDownloadItemStateFromDownloadItem(event.downloadItem)
                                    val exists = current.find {
                                        it.id == item.id
                                    } != null
                                    if (exists) {
                                        //replace existing
                                        current.map {
                                            if (it.id == item.id) {
                                                item
                                            } else {
                                                it
                                            }
                                        }
                                    } else {
                                        current + item
                                    }
                                } else {
                                    current.filter {
                                        it.id != event.downloadItem.id
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
                .launchIn(this)
        }

    }

    private fun stopUpdateCompletedList() {
        completedDownloadListUpdaterJob?.cancel()
        completedDownloadListUpdaterJob = null
    }


    private var downloadListUpdaterJob: Job? = null
    private fun startUpdateActiveDownloadList() {
        downloadListUpdaterJob?.cancel()
        downloadListUpdaterJob = merge(
            downloadManager.listOfJobsEvents.map { },
            downloadSpeedFlow,
            intervalFlow(500)
        ).onEach {
            val newList = downloadManager.downloadJobs.filter {
                it.status.value != DownloadJobStatus.Finished
            }.map {
                val status = it.status.value
                val speed = if (status is DownloadJobStatus.IsActive) {
                    getSpeedOf(it.id)
                } else 0L
                downloadItemStateFactory.createProcessingDownloadItemStateFromDownloadJob(
                    it,
                    speed = speed
                )
            }
            activeDownloadListFlow.update { newList }
        }
            .launchIn(scope)
    }

    private fun stopUpdateDownloadList() {
        downloadListUpdaterJob?.cancel()
//        println("turn off list updater")
        downloadListUpdaterJob = null
    }


    override val activeDownloadCount = downloadManager.listOfJobsEvents.map {
        downloadManager.getActiveCount()
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        downloadManager.getActiveCount()
    )

    override suspend fun waitForDownloadToFinishOrCancel(
        id: Long,
    ) {
        val event = downloadManager
            .listOfJobsEvents
            .filter {
                it.downloadItem.id == id
            }
            .first {
                when (it) {
                    is DownloadManagerEvents.OnJobAdded -> false
                    is DownloadManagerEvents.OnJobCanceled -> true
                    is DownloadManagerEvents.OnJobChanged -> false
                    is DownloadManagerEvents.OnJobCompleted -> true
                    is DownloadManagerEvents.OnJobRemoved -> true
                    is DownloadManagerEvents.OnJobStarted -> false
                    is DownloadManagerEvents.OnJobStarting -> false
                }
            }
        if (event is DownloadManagerEvents.OnJobCanceled) {
            throw event.e
        }
    }
}

data class SpeedAtTime(
    val speed: Map<Long, Long>,
    val time: Long = System.currentTimeMillis(),
) {
    companion object {
        fun empty() = SpeedAtTime(emptyMap())
    }
}
