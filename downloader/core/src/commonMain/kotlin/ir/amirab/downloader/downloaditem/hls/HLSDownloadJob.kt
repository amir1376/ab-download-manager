package ir.amirab.downloader.downloaditem.hls

import io.lindstrom.m3u8.model.MediaPlaylist
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.destination.SegmentedDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.exception.DownloadValidationException
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.part.*
import ir.amirab.downloader.utils.*
import ir.amirab.util.tryLocked
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Throttler
import java.util.concurrent.ConcurrentHashMap


/**
 * alive object that responsible for download a file
 */

class HLSDownloadJob(
    override val downloadItem: HLSDownloadItem,
    downloadManager: DownloadManager,
    val client: HttpDownloaderClient,
) : DownloadJob(
    downloadManager = downloadManager,
) {
    val listDb by downloadManager::dlListDb
    val partListDb by downloadManager::partListDb
    private val parts: MutableList<MediaSegment> = mutableListOf()

    private lateinit var destination: SegmentedDownloadDestination

    override fun getDestination(): DownloadDestination {
        return destination
    }

    var serverLastModified: Long? = null
        private set

    override suspend fun actualBoot() {
        initializeDestination()
        loadPartState()
        applySpeedLimit()
        downloadedSizeBeforeRetry = getDownloadedSize()
    }

    override fun initializeDestination() {
        val outFile = downloadManager.calculateOutputFile(downloadItem)
        destination = SegmentedDownloadDestination(
            outputFile = outFile,
            getAllParts = {
                getParts()
            },
            tempDirectory = downloadManager.downloadDataFolder.resolve(id.toString()),
            getFileName = {
                it.getID().toString()
            },
            appendMode = false,
        )
    }

    private fun setParts(list: List<MediaSegment>) {
        this.parts.clear()
        list.forEach {
            if (it.isCompleted) {
                it.statusFlow.update { PartDownloadStatus.Completed }
            }
        }
        this.parts.addAll(list)
    }

    val itemSaveLock = Mutex()
    val partLock = Mutex()
    private suspend fun loadPartState() {
        val mediaSegments = partLock.withLock {
            partListDb.getParts(id)
        } as? MediaSegments
        setParts(mediaSegments?.list.orEmpty())
    }


    override suspend fun reset() {
        pause()
        clearPartDownloaderList()
        setParts(emptyList())
        downloadItem.contentLength = IDownloadItem.LENGTH_UNKNOWN
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        downloadItem.duration = null
        downloadedSizeBeforeRetry = 0 // nothing
        saveState()
        downloadManager.onDownloadItemChange(downloadItem)
    }


    override suspend fun resume() {
        if (isDownloadActive.value) {
            return
        }
        _isDownloadActive.update { true }
        resumeWithNewScope(
            newActiveScope = createAndInitializeDownloadScope(),
            isInFirstResume = true
        )
    }

    fun createAndInitializeDownloadScope(): CoroutineScope {
        val newActiveScope = newScopeBasedOn(scope)
            .also {
                activeDownloadScope = it
            }
        return newActiveScope
    }

    private suspend fun resumeWithNewScope(
        newActiveScope: CoroutineScope,
        isInFirstResume: Boolean,
    ) {

//        println(parts.filter { !it.isCompleted })
        return newActiveScope.launch {
            //boot download item from storage!
            boot()
            // if download item is booted and parts is not empty it means that we resumed that file in some point
            // but we should check if all parts are already downloaded to finish the job before hitting the server unnecessarily!
            if (parts.isNotEmpty() && parts.all { it.isCompleted }) {
                onDownloadFinished()
                return@launch
            }
            onDownloadResuming()
            try {
                fetchDownloadInfoAndValidate()
                createPartDownloaderList()
//                println("part downloaders created")
                beginDownloadParts()
                startAutoSaver()
                downloadItem.status = DownloadStatus.Downloading
                if (downloadItem.startTime == null) {
                    downloadItem.startTime = System.currentTimeMillis()
                }
                saveState()
                onDownloadResumed()
            } catch (e: Exception) {
                e.printStackIfNOtUsual()
                val shouldStop = when {
                    ExceptionUtils.isNormalCancellation(e) -> true
                    e is DownloadValidationException -> e.isCritical()
                    else -> false
                }
                if (shouldStop) {
                    // this function called from activeDownloadScope
                    // so we change the scope here to prevent cancel this suspend function
                    scope.launch {
                        pause(e)
                    }
                } else {
                    downloadFailedRetryOrPause(
                        e = e,
                        isInFirstResume = isInFirstResume,
                    )
                }
            }
        }.join()
    }


    override fun getDownloadedSize(): Long {
        return getParts().sumOf {
            it.howMuchProceed()
        }
    }


    fun onPreferredConnectionCountChanged() {
        activeDownloadScope?.launch {
            beginDownloadParts()
        }
    }

    override suspend fun changeConfig(
        updater: (IDownloadItem) -> Unit,
        extraConfig: DownloadJobExtraConfig?
    ): IDownloadItem {
        boot()
        val previousItem = downloadItem.copy()
        val newItem = previousItem.copy().apply(updater)
        val previousDestination = downloadManager.calculateOutputFile(previousItem)
        val newDestination = downloadManager.calculateOutputFile(newItem)
        val shouldUpdateDestination = previousDestination != newDestination
        if (shouldUpdateDestination) {
            if (isDownloadActive.value) {
                pause()
            }
            destination.moveOutput(newDestination)
        }
        // if there is no error update the actual download item
        downloadItem.applyFrom(newItem)
        if (shouldUpdateDestination) {
            // destination should be closed for now!
            initializeDestination()
        }
        if (previousItem.preferredConnectionCount != downloadItem.preferredConnectionCount) {
            onPreferredConnectionCountChanged()
        }
        if (previousItem.link != downloadItem.link) {
            onLinkChanged()
        }
        applySpeedLimit()
        extraConfig?.let {
            extraConfigsReceived(extraConfig)
        }
        saveDownloadItem()
        return downloadItem
    }

    private fun applySpeedLimit() {
        jobThrottler.bytesPerSecond(bytesPerSecond = downloadItem.speedLimit)
    }

    fun onLinkChanged() {
        scope.launch {
            if (activeDownloadScope?.isActive == true) {
                pause()
                resume()
            }

        }
    }

    fun getRequestedThreadCount(): Int {
        return downloadItem.preferredConnectionCount
            ?: downloadManager.settings.defaultThreadCount
    }


    private val partLoopLock = Mutex()

    //    private val c = AtomicInteger(0)
    private fun beginDownloadParts() {
        if (partLoopLock.isLocked) {
            return
        }
        activeDownloadScope?.launch {
            if (!partLoopLock.tryLock()) {
                return@launch
            }
//            c.incrementAndGet()
            try {
                val activeCount = getPartDownloaderList()
                    .count { it.active }
                val howMuchCreate = getRequestedThreadCount() - activeCount
                if (howMuchCreate > 0) {
                    val mutableInactivePartDownloaderList = getPartDownloaderList()
                        .filter { !it.active && !it.part.isCompleted }
                        .sortedBy { it.part.getID() }
                        .toMutableList()
//                    println(mutableInactivePartDownloaderList)

                    fun getPartDownloader(): HLSPartDownloader? {
                        val inactivePart =
                            runCatching { mutableInactivePartDownloaderList.removeAt(0) }.getOrNull()
                        if (inactivePart != null) return inactivePart
                        return null
                    }
                    for (i in 1..howMuchCreate) {
                        val partDownloader = getPartDownloader()
                        if (partDownloader == null) {
//                            println("part downloader is null")
                            break
                        }
                        if (partDownloader.part.isCompleted) {
//                            println("it seems part is downloaded!")
                            continue
                        }
//                        println("got new part downloader ${partDownloader.part}")
                        partDownloader.start()
                    }
                }
                if (howMuchCreate < 0) {
                    // as we restart the parts each time we don't pause the active ones
//                    partDownloaderList.values
//                        .toList()
//                        .filter { it.active }
//                        .sortedByDescending { it.part.getID() }
//                        .take(-howMuchCreate)
//                        .onEach {
//                            it.stop()
//                        }.onEach {
//                            it.join()
//                            it.awaitIdle()
//                        }
                }
            } catch (e: Exception) {
                throw e
            } finally {
//                println("C:" + c)
                partLoopLock.unlock()
//                c.decrementAndGet()
            }
        }
    }

    private fun onPartHaveToManyError(throwable: Throwable) {
        var paused = false
        if (throwable is DownloadValidationException) {
            if (throwable.isCritical()) {
                //stop the whole job! as we have big problem here
                paused = true
                scope.launch {
                    pause(throwable)
                }
            }
        }
        val allHaveError = partDownloaderList.values
            .filter { it.active }
            .all {
                it.injured()
            }
        if (allHaveError && !paused) {
//            println("all have error!")
            downloadFailedRetryOrPause(
                e = throwable,
                isInFirstResume = false,
            )
        }
    }

    // for this download job only, it has higher priority than download manager settings
    var _maxAllowedRetries: Int? = null
    fun getMaxAllowedRetries(): Int {
        return _maxAllowedRetries ?: downloadManager.settings.maxDownloadRetryCount
    }

    var failedDownloadTries = 0
    val delayForEachRetry = 3_000L
    private var downloadedSizeBeforeRetry = 0L

    private var retryJob: Job? = null

    private val retryLock = Mutex()

    // I have to improve this function to not allow accessing it concurrently
    private fun downloadFailedRetryOrPause(
        e: Throwable,
        isInFirstResume: Boolean,
    ) {
        //moving to the main scope and request to cancel activeDownload scope!
        scope.launch {
            if (isInFirstResume && failedDownloadTries == 0 && shouldRetryIfInitialFailed()) {
                if (ExceptionUtils.isNetworkError(e) || ExceptionUtils.isResponseError(e)) {
                    pause(e)
                    return@launch
                }
            }
            // can't proceed
            if (e is DownloadValidationException && e.isCritical()) {
                pause(e)
                return@launch
            }
            val downloadedSize = getDownloadedSize()
            if (downloadedSize > downloadedSizeBeforeRetry) {
                // download had progress! so we reset it
                failedDownloadTries = 0
            } else {
                failedDownloadTries++
            }
            downloadedSizeBeforeRetry = downloadedSize

            // we always have one try (the initial resume action), after that others are retries!
            val retriedCount = (failedDownloadTries - 1).coerceAtLeast(0)
            if (retriedCount < getMaxAllowedRetries()) {
                retry(isInFirstResume)
            } else {
                pause(TooManyErrorException(e))
            }
        }
    }

    fun retry(isInFirstResume: Boolean) {
        scope.launch {
            val newScopeResult = retryLock.tryLocked {
                val job = async {
                    saveState()
                    cancelDownloadScope()
                    stopAllParts()
                    _status.update { DownloadJobStatus.Retrying(delayForEachRetry) }
                    delay(delayForEachRetry)
                    createAndInitializeDownloadScope()
                }
                retryJob = job
                job.await()
            }
            newScopeResult.getOrNull()?.let {
                resumeWithNewScope(it, isInFirstResume)
            }
        }
    }

    fun shouldRetryIfInitialFailed(): Boolean {
        return true
    }


    private fun onPartStatusChanged(
        partDownloader: HLSPartDownloader,
        partStatus: PartDownloadStatus,
    ) {
        when (partStatus) {
            is PartDownloadStatus.Canceled -> {
                destination.onPartCancelled(partDownloader.part)
            }

            PartDownloadStatus.Completed -> {
                destination.onPartCancelled(partDownloader.part)
                if (getParts().all { it.isCompleted }) {
                    onDownloadFinished()
                } else {
                    scope.launch {
                        beginDownloadParts()
                    }
                }
            }

            PartDownloadStatus.ReceivingData -> {}
            PartDownloadStatus.Connecting -> {}
            PartDownloadStatus.IDLE -> {}
        }
    }


    @Synchronized
    private fun createPartDownloaderList() {
        synchronized(partDownloaderList) {
            //        thisLogger().info("create part downloaders")
            parts.forEach {
                getOrCreatePartDownloader(it)
            }
//            println("created N parts = " + partDownloaderList.values.size)
        }
    }

    private fun clearPartDownloaderList() {
//        thisLogger().info("create part downloaders")
        parts.forEach {
            destroyPartDownloader(it)
        }
    }

    private val jobThrottler = Throttler()

    private val partDownloaderList = ConcurrentHashMap<Long, HLSPartDownloader>()
    private val listenerJobs: MutableMap<Long, Job> = ConcurrentHashMap<Long, Job>()
    private fun getPartDownloaderList(): List<HLSPartDownloader> {
        synchronized(partDownloaderList) {
            return partDownloaderList.map { it.value }
        }
    }

    private fun getOrCreatePartDownloader(part: MediaSegment): HLSPartDownloader {
        synchronized(partDownloaderList) {
            return partDownloaderList.getOrPut(part.getID()) {
                HLSPartDownloader(
                    baseURL = downloadItem.link,
                    part = part,
                    getDestWriter = {
                        destination.getWriterFor(part)
                    },
                    client = client,
                    speedLimiters = listOf(
                        downloadManager.throttler,
                        jobThrottler,
                    ),
                ).also { partDownloader: HLSPartDownloader ->
                    partDownloader.onTooManyErrors = {
                        onPartHaveToManyError(it)
                    }
                    //we should close that scope after we don't need it anymore!
                    listenerJobs[part.getID()] = partDownloader.statusFlow.onEach { status ->
                        //TODO probably bug here
                        onPartStatusChanged(partDownloader, status)
                    }.launchIn(scope)
                }
            }
        }
    }

    private fun destroyPartDownloader(part: MediaSegment) {
        listenerJobs.remove(part.getID())?.cancel()
        partDownloaderList.remove(part.getID())
    }

    private suspend fun fetchDownloadInfoAndValidate(
    ) {
        if (parts.isEmpty()) {
            initialParts()
        }
        // at this point we have all the parts we need either by hitting remote or they are already in destination
        // before proceed we should check the downloaded parts and remove the "isCompleted" flag if they are missing
        parts
            .filter { it.isCompleted }
            .forEach {
                // redownload invalid parts
                if (!destination.isDownloadPartValid(it)) {
                    it.isCompleted = false
                }
            }

        saveState()
    }

    // new segments received
    // we should compare those and update them
    // we reset the whole if we found different duration
    fun updateParts(
        playlist: MediaPlaylist
    ) {
        val newParts = playlist.mediaSegments()
            .mapIndexed { index, segment ->
                MediaSegment(
                    segmentIndex = playlist.mediaSequence() + index,
                    link = segment.uri(),
                    duration = segment.duration()
                )
            }
        val currentParts = parts
        downloadItem.duration = newParts.sumOf { it.duration }
        if (currentParts.isEmpty()) {
            setParts(newParts)
            return
        }
        val oldPartsMap = currentParts.associateBy {
            it.segmentIndex
        }
        val newPartsToSave = ArrayList<MediaSegment>(newParts.size)
        for (newPart in newParts) {
            val oldPart = oldPartsMap[newPart.segmentIndex]
            var newPartToAdd = newPart
            if (oldPart != null) {
                if (oldPart.duration == newPart.duration) {
                    newPartToAdd = newPart.copy(
                        isCompleted = oldPart.isCompleted
                    )
                } else {
                    // inconsistencies found! reset the parts to the new one
                    setParts(newParts)
                    return
                }
            }
            newPartsToSave.add(newPartToAdd)
        }
    }

    private suspend fun initialParts(): HLSResponseInfo {
        val response = client.connect(
            downloadItem, null, null
        ).use {
            HLSResponseInfo.fromConnection(it)
        }
        updateParts(
            response.hlsManifest
        )
        return response
    }

    suspend fun cancelDownloadScope() {
        activeDownloadScope?.coroutineContext?.job?.cancelAndJoin()
        activeDownloadScope = null
    }

    suspend fun cancelRetry() {
        retryJob?.cancel()
        retryJob = null
    }

    suspend fun stopAllParts() {
        withContext(Dispatchers.IO) {
            partDownloaderList.values.onEach {
                it.stop()
            }.onEach {
                it.join()
                it.awaitIdle()
            }
        }
    }

    override suspend fun pause(throwable: Throwable) {
        boot()
        failedDownloadTries = 0
        cancelRetry()
        cancelDownloadScope()
        stopAllParts()
        onDownloadCanceled(throwable)
    }

    override fun onDownloadFinishedBeforeSave() {
        downloadItem.contentLength = destination.outputFile.length()
    }

    private var lastSavedDownloadItem: HLSDownloadItem? = null
    private var lastSavedParts: List<MediaSegment>? = null

    private suspend fun saveDownloadItem() {
        itemSaveLock.withLock {
            val copy = downloadItem.copy()
            if (lastSavedDownloadItem != downloadItem) {
                listDb.update(downloadItem)
                lastSavedDownloadItem = copy
            }
        }
    }

    private suspend fun saveParts() {
        partLock.withLock {
            val copy = getParts().map { it.copy() }
            if (lastSavedParts != copy) {
                destination.flush()
                partListDb.setParts(id, MediaSegments(copy))
                lastSavedParts = copy
            }
        }
    }

    override suspend fun saveState() {
        saveDownloadItem()
        saveParts()
    }

    fun getParts(): List<MediaSegment> {
        //Make a copy because of CMException
        return parts.toList()
    }

    override fun reloadSettings() {
        onPreferredConnectionCountChanged()
    }

    override suspend fun extraConfigsReceived(config: DownloadJobExtraConfig) {
        if (config !is HLSDownloadJobExtraConfig) return
        config.hlsManifest?.let {
            updateParts(it)
            saveParts()
        }
    }
}

