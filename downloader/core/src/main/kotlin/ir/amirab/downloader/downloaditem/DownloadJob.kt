package ir.amirab.downloader.downloaditem

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.connection.response.isWebPage
import ir.amirab.downloader.destination.SimpleDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadItem.Companion.LENGTH_UNKNOWN
import ir.amirab.downloader.exception.DownloadValidationException
import ir.amirab.downloader.exception.PrepareDestinationFailedException
import ir.amirab.downloader.exception.FileChangedException
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.part.*
import ir.amirab.downloader.utils.*
import ir.amirab.util.tryLocked
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class DownloadJob(
    val downloadItem: DownloadItem,
    val downloadManager: DownloadManager,
    val client: DownloaderClient,
) {
    val scope = CoroutineScope(SupervisorJob())
    val id = downloadItem.id
    var activeDownloadScope: CoroutineScope? = null


    val listDb by downloadManager::dlListDb
    val partListDb by downloadManager::partListDb
    private val parts: MutableList<Part> = mutableListOf()
    lateinit var destination: SimpleDownloadDestination

    @Volatile
    private var booted = false


    var supportsConcurrent: Boolean? = null
        private set

    var serverLastModified: Long? = null
        private set

    private val _isDownloadActive = MutableStateFlow(false)
    val isDownloadActive = _isDownloadActive.asStateFlow()
    private fun initializeDestination() {
        val outFile = downloadManager.calculateOutputFile(downloadItem)
        destination = SimpleDownloadDestination(
            file = outFile,
            emptyFileCreator = downloadManager.emptyFileCreator,
            appendExtensionToIncompleteDownloads = downloadManager.settings.appendExtensionToIncompleteDownloads,
            downloadId = id
        )
    }

    suspend fun boot() {
        if (!booted) {
            initializeDestination()
            loadPartState()
            supportsConcurrent = when (getParts().size) {
                in 2..Int.MAX_VALUE -> true
                else -> null
            }
            applySpeedLimit()
            downloadedSizeBeforeRetry = getDownloadedSize()
            booted = true
//            thisLogger().info("job for dl_$id booted")
        }
    }

    private fun setParts(list: List<Part>) {
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
        setParts(partLock.withLock {
            partListDb.getParts(id)
        }.orEmpty())
    }


    // if strict mode is false part downloader going to download data without any validation of content length
    // this is only acceptable when resume is not supported and multiple get requests results multiple result
    @Volatile
    private var strictDownload = true

    private val _status = MutableStateFlow<DownloadJobStatus>(DownloadJobStatus.IDLE)
    val status = _status.asStateFlow()
    fun expectValid(size: Long, parts: List<LongRange>) {
        val parts = parts.sortedBy { it.first }
        require(parts.first().first == 0L)
        require(parts.last().last == size - 1)
        for (i in 1..<parts.size) {
            val a = parts[i - 1]
            val b = parts[i]
            require(a.last + 1 == b.first)
        }
    }

    suspend fun reset() {
        pause()
        clearPartDownloaderList()
        setParts(emptyList())
        downloadItem.contentLength = LENGTH_UNKNOWN
        downloadItem.serverETag = null
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        strictDownload = true
        downloadedSizeBeforeRetry = 0 // nothing
        saveState()
        downloadManager.onDownloadItemChange(downloadItem)
    }


    suspend fun resume() {
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
                createPartsIfNotCreated()
                prepareDestination {
                    _status.value = DownloadJobStatus.PreparingFile(it)
                }
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


    private suspend fun prepareDestination(
        onProgressUpdate: (Int?) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            destination.outputSize = downloadItem.contentLength
                .takeIf {
                    // reset size if we have a non-strict download (webpage etc.
                    strictDownload
                }
                ?.takeIf {
                    // reset output file if we can't support the file
                    supportsConcurrent != false
                }
                ?: LENGTH_UNKNOWN
            if (!destination.isDownloadedPartsIsValid()) {
                //file deleted or something!
                parts.forEach { it.resetCurrent() }
                saveState()
            }
//          thisLogger().info("preparing file")
            try {
                destination.prepareFile(onProgressUpdate)
            } catch (e: Exception) {
                e.throwIfCancelled()
                throw PrepareDestinationFailedException(e)
            }
            val lastModified = serverLastModified.takeIf { downloadManager.settings.useServerLastModifiedTime }
            destination.setLastModified(lastModified)
//            thisLogger().info("file prepared")
        }
    }

    fun getDownloadedSize(): Long {
        return getParts().sumOf {
            it.howMuchProceed()
        }
//        return partDownloaderList.values.sumOf {
//            it.progressFlow.value.value
//        }
    }

    private fun startAutoSaver() {
        activeDownloadScope?.launch(Dispatchers.IO) {
            while (true) {
                saveState()
                delay(1000)
            }
        }
    }

    fun onPreferredConnectionCountChanged() {
        activeDownloadScope?.launch {
            beginDownloadParts()
        }
    }

    suspend fun changeConfig(updater: (DownloadItem) -> Unit): DownloadItem {
        boot()
        val previousItem = downloadItem.copy()
        val newItem = previousItem.apply(updater)
        val previousDestination = downloadManager.calculateOutputFile(previousItem)
        val newDestination = downloadManager.calculateOutputFile(newItem)
        if (previousDestination != newDestination) {
            if (isDownloadActive.value) {
                pause()
            }
            destination.moveOutput(newDestination)
            // destination should be closed for now!
            initializeDestination()
        }
        // if there is no error update the actual download item
        downloadItem.applyFrom(newItem)
        if (previousItem.preferredConnectionCount != downloadItem.preferredConnectionCount) {
            onPreferredConnectionCountChanged()
        }
        if (previousItem.link != downloadItem.link) {
            onLinkChanged()
        }
        applySpeedLimit()
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

    fun getRequestedPartitionCount(): Int {
        return downloadItem.preferredConnectionCount
            ?: downloadManager.settings.defaultThreadCount
    }

    private suspend fun createPartsIfNotCreated() {
        if (parts.isNotEmpty()) {
            return
        }
        if (downloadItem.contentLength == LENGTH_UNKNOWN) {
            setParts(
                listOf(Part(0, null, 0))
            )
        } else {
            if (supportsConcurrent == true) {
                //split parts
                setParts(
                    splitToRange(
                        minPartSize = downloadManager.settings.minPartSize,
                        maxPartCount = getRequestedPartitionCount().toLong(),
                        size = downloadItem.contentLength,
                    ).map {
                        Part(it.first, it.last)
                    })
            } else {
                setParts(
                    listOf(Part(0, (downloadItem.contentLength - 1).takeIf { it >= 0 }, 0))
                )
            }

        }

//        thisLogger().info("dl_$id parts created $parts")

        saveState()
    }

    private val partSplitLock = Any()
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
//                    .also { println("values count " + it.size) }
                    .filter {
                        it.active
                    }.count()
                val howMuchCreate = getRequestedPartitionCount() - activeCount
                if (howMuchCreate > 0) {
                    val mutableInactivePartDownloaderList = getPartDownloaderList()
                        .filter { !it.active && !it.part.isCompleted }
                        .toMutableList()
//                    println(mutableInactivePartDownloaderList)

                    fun getPartDownloader(): PartDownloader? {
                        val inactivePart =
                            kotlin.runCatching { mutableInactivePartDownloaderList.removeAt(0) }.getOrNull()
                        if (inactivePart != null) return inactivePart
                        if (supportsConcurrent == true && downloadManager.settings.dynamicPartCreationMode) {
                            synchronized(partSplitLock) {
                                val candidates = getPartDownloaderList()
                                    .toList()
                                    .filter { it.canBeSplit() }
                                    .sortedByDescending {
                                        it.part.remainingLength
                                    }
                                for (i in candidates) {
                                    val newPart = i.splitPart()
                                    if (newPart != null) {
//                                        println("a part split")
                                        parts.add(newPart)
                                        parts.sortBy { it.from }
                                        return getOrCreatePartDownloader(newPart)
                                    }
                                }
                            }
                        }
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
                    partDownloaderList.values
                        .toList()
                        .filter { it.active }
                        .reversed()
                        .take(-howMuchCreate)
                        .onEach {
                            it.stop()
                        }.onEach {
                            it.join()
                            it.awaitIdle()
                        }
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
        partDownloader: PartDownloader,
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
            PartDownloadStatus.SendGet -> {}
            PartDownloadStatus.IDLE -> {}
        }
    }

    private fun onDownloadResuming() {
        _status.update {
            DownloadJobStatus.Resuming
        }
        downloadManager.onDownloadResuming(downloadItem)
    }

    private fun onDownloadResumed() {
        _status.update { DownloadJobStatus.Downloading }
        downloadManager.onDownloadResumed(downloadItem)
    }

    private suspend fun onDownloadCanceled(throwable: Throwable) {
        _status.update { DownloadJobStatus.Canceled(throwable) }
        if (ExceptionUtils.isNormalCancellation(throwable)) {
            downloadItem.status = DownloadStatus.Paused
        } else {
            downloadItem.status = DownloadStatus.Error
        }
        _isDownloadActive.update { false }
        saveState()
        downloadManager.onDownloadCanceled(downloadItem, throwable)
    }

    private fun onDownloadFinished() {
        scope.launch {
            try {
                destination.onAllPartsCompleted()
            } catch (e: Exception) {
                pause(e)
                return@launch
            }
            downloadItem.status = DownloadStatus.Completed
            if (downloadItem.contentLength == LENGTH_UNKNOWN) {
                //in case of blind part, update download item length
                if (parts.size == 1) {
                    downloadItem.contentLength = parts[0].howMuchProceed()
                }
            }
            downloadItem.completeTime = System.currentTimeMillis()
            _status.value = DownloadJobStatus.Finished
            _isDownloadActive.update { false }
            saveState()
            downloadManager.onDownloadFinished(downloadItem)
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

    private val partDownloaderList = ConcurrentHashMap<Long, PartDownloader>()
    private val listenerJobs: MutableMap<Long, Job> = ConcurrentHashMap<Long, Job>()
    private fun getPartDownloaderList(): List<PartDownloader> {
        synchronized(partDownloaderList) {
            return partDownloaderList.map { it.value }
        }
    }

    private fun getOrCreatePartDownloader(part: Part): PartDownloader {
        synchronized(partDownloaderList) {
            return partDownloaderList.getOrPut(part.from) {
                PartDownloader(
                    credentials = downloadItem,
                    part = part,
                    getDestWriter = {
                        destination.getWriterFor(part)
                    },
                    client = client,
                    speedLimiters = listOf(
                        downloadManager.throttler,
                        jobThrottler,
                    ),
                    strictMode = strictDownload,
                    partSplitLock = partSplitLock
                ).also { partDownloader: PartDownloader ->
                    partDownloader.onTooManyErrors = {
                        onPartHaveToManyError(it)
                    }
                    //we should close that scope after we don't need it anymore!
                    listenerJobs[part.from] = partDownloader.statusFlow.onEach { status ->
                        //TODO probably bug here
                        onPartStatusChanged(partDownloader, status)
                    }.launchIn(scope)
                }
            }
        }
    }

    private fun destroyPartDownloader(part: Part) {
        listenerJobs.remove(part.from)?.cancel()
        partDownloaderList.remove(part.from)
    }

    private fun isDownloadItemIsAWebpage(): Boolean {
        return downloadItem.name.endsWith(".html", true)
    }

    private suspend fun fetchDownloadInfoAndValidate(
    ) {
//        println("fetch download ")

//        thisLogger().info("fetchDownloadInfoAndValidate")
        val response = client.test(downloadItem).expectSuccess()
        supportsConcurrent = response.resumeSupport
        serverLastModified = kotlin.runCatching {
            response.lastModified?.let(TimeUtils::convertLastModifiedHeaderToTimestamp)
        }.getOrNull()
        if (response.isWebPage()) {
            if (isDownloadItemIsAWebpage()) {
                // don't strict if it's a webpage let it download without checks
                strictDownload = false

                // this makes the file not resume able
                // we don't want to page downloaded with multi connection
                // so the download will be restarted [@see prepareDestination]
                supportsConcurrent = false
                downloadItem.contentLength = LENGTH_UNKNOWN
                downloadItem.serverETag = null
            } else {
                // if download was not a webpage and now this is a webpage
                // it means maybe user have to change its download link
                // we should not restart download here!
                throw FileChangedException.GotAWebPage()
            }
        }
        val totalLength = response.totalLength
        val oldServerETag = downloadItem.serverETag
        val newServerETag = response.etag
        if (downloadItem.contentLength == LENGTH_UNKNOWN) {
            //new download / or restart
            downloadItem.contentLength = totalLength ?: -1
            downloadItem.serverETag = newServerETag
        } else {
            // check if we file not changed from remote
            if (totalLength != downloadItem.contentLength) {
                throw FileChangedException.LengthChangedException(downloadItem.contentLength, totalLength ?: -1)
            }
            if (oldServerETag != null && newServerETag != null) {
                // we already know that sizes are the same,
                // but we also have etag header
                // so, we have chance to compare file contents of local and server
                if (oldServerETag != newServerETag) {
                    throw FileChangedException.ETagChangedException(oldServerETag, newServerETag)
                }
            }
        }
//            thisLogger().info("fetchDownloadInfoAndValidate :${response.code},${response.headers} ")
        saveState()
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
        withContext(Dispatchers.Default) {
            partDownloaderList.values.onEach {
                it.stop()
            }.onEach {
                it.join()
                it.awaitIdle()
            }
        }
    }

    suspend fun pause(throwable: Throwable = CancellationException()) {
        boot()
        failedDownloadTries = 0
        cancelRetry()
        cancelDownloadScope()
        stopAllParts()
        onDownloadCanceled(throwable)
    }

    private var lastSavedDownloadItem: DownloadItem? = null
    private var lastSavedParts: List<Part>? = null

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
                partListDb.setParts(id, copy)
                lastSavedParts = copy
            }
        }
    }

    suspend fun saveState() {
        saveDownloadItem()
        saveParts()
    }

    fun getParts(): List<Part> {
        //Make a copy because of CMException
        return parts.toList()
    }

    fun close() {
        scope.cancel()
    }

    private fun ensureBooted() {
        require(booted) {
            "DownloadJob is not booted! Call boot() before using this object."
        }
    }

    fun downloadRemoved(
        removeOutputFile: Boolean = true,
    ) {
        ensureBooted()
        destination.cleanUpJunkFiles()
        if (removeOutputFile) {
            destination.deleteOutputFile()
        }
    }
}

sealed class DownloadJobStatus(
    val order: Int,
    private val downloadStatus: DownloadStatus
) {
    fun asDownloadStatus() = downloadStatus

    data object Downloading : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data class Retrying(val timeUntilRetry: Long) : DownloadJobStatus(0, DownloadStatus.Paused),
        IsActive

    data object Resuming : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data class PreparingFile(val percent: Int?) : DownloadJobStatus(1, DownloadStatus.Downloading),
        IsActive

    data class Canceled(val e: Throwable) : DownloadJobStatus(
        2,
        if (ExceptionUtils.isNormalCancellation(e)) DownloadStatus.Paused else DownloadStatus.Error
    ),
        CanBeResumed

    data object IDLE : DownloadJobStatus(2, DownloadStatus.Added),
        CanBeResumed

    data object Finished : DownloadJobStatus(3, DownloadStatus.Completed)

    interface IsActive
    interface CanBeResumed
}


private fun newScopeBasedOn(scope: CoroutineScope): CoroutineScope {
    return CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext.job))
}
