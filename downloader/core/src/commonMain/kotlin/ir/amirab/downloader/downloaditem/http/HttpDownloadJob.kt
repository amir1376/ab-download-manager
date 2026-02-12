package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.destination.SimpleDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.exception.DownloadValidationException
import ir.amirab.downloader.exception.PrepareDestinationFailedException
import ir.amirab.downloader.exception.FileChangedException
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

class HttpDownloadJob(
    override val downloadItem: HttpDownloadItem,
    downloadManager: DownloadManager,
    val client: HttpDownloaderClient,
) : DownloadJob(
    downloadManager = downloadManager,
) {
    val listDb by downloadManager::dlListDb
    val partListDb by downloadManager::partListDb
    private val parts: MutableList<RangedPart> = mutableListOf()
    private lateinit var destination: SimpleDownloadDestination
    override fun getDestination(): DownloadDestination {
        return destination
    }

    var supportsConcurrent: Boolean? = null
        private set

    var serverLastModified: Long? = null
        private set

    override suspend fun actualBoot() {
        initializeDestination()
        loadPartState()
        supportsConcurrent = when (getParts().size) {
            in 2..Int.MAX_VALUE -> true
            else -> null
        }
        applySpeedLimit()
        downloadedSizeBeforeRetry = getDownloadedSize()
    }

    override fun initializeDestination() {
        val outFile = downloadManager.calculateOutputFile(downloadItem)
        destination = SimpleDownloadDestination(
            file = outFile,
            emptyFileCreator = downloadManager.emptyFileCreator,
            appendExtensionToIncompleteDownloads = downloadManager.settings.appendExtensionToIncompleteDownloads,
            downloadId = id
        )
    }

    private fun setParts(list: List<RangedPart>) {
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
        val rangedParts = partLock.withLock {
            partListDb.getParts(id)
        } as? RangedParts
        setParts(rangedParts?.list.orEmpty())
    }


    // if strict mode is false part downloader going to download data without any validation of content length
    // this is only acceptable when resume is not supported and multiple get requests results multiple result
    @Volatile
    private var strictDownload = true

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

    override suspend fun reset() {
        pause()
        clearPartDownloaderList()
        setParts(emptyList())
        downloadItem.contentLength = IDownloadItem.LENGTH_UNKNOWN
        downloadItem.serverETag = null
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        strictDownload = true
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
                ?: IDownloadItem.LENGTH_UNKNOWN
            // first we try to create the folder
            // maybe the storage wasn't mounted yet, in that case we get an exception here
            // it should be here to prevent resetting the download
            try {
                destination.prepareDestinationFolder()
            } catch (e: Exception) {
                e.throwIfCancelled()
                throw PrepareDestinationFailedException(e)
            }
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

    override fun getDownloadedSize(): Long {
        return getParts().sumOf {
            it.howMuchProceed()
        }
//        return partDownloaderList.values.sumOf {
//            it.progressFlow.value.value
//        }
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
            extraConfigsReceived(it)
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

    fun getRequestedPartitionCount(): Int {
        return downloadItem.preferredConnectionCount
            ?: downloadManager.settings.defaultThreadCount
    }

    private suspend fun createPartsIfNotCreated() {
        if (parts.isNotEmpty()) {
            return
        }
        if (downloadItem.contentLength == IDownloadItem.LENGTH_UNKNOWN) {
            setParts(
                listOf(RangedPart(0, null, 0))
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
                        RangedPart(it.first, it.last)
                    })
            } else {
                setParts(
                    listOf(RangedPart(0, (downloadItem.contentLength - 1).takeIf { it >= 0 }, 0))
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
                    .count { it.active }
                val howMuchCreate = getRequestedPartitionCount() - activeCount
                if (howMuchCreate > 0) {
                    val mutableInactivePartDownloaderList = getPartDownloaderList()
                        .filter { !it.active && !it.part.isCompleted }
                        .sortedBy { it.part.from }
                        .toMutableList()
//                    println(mutableInactivePartDownloaderList)

                    fun getPartDownloader(): HttpPartDownloader? {
                        val inactivePart =
                            runCatching { mutableInactivePartDownloaderList.removeAt(0) }.getOrNull()
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
                        .sortedByDescending { it.part.from }
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
        partDownloader: HttpPartDownloader,
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

    private val partDownloaderList = ConcurrentHashMap<Long, HttpPartDownloader>()
    private val listenerJobs: MutableMap<Long, Job> = ConcurrentHashMap<Long, Job>()
    private fun getPartDownloaderList(): List<HttpPartDownloader> {
        synchronized(partDownloaderList) {
            return partDownloaderList.map { it.value }
        }
    }

    private fun getOrCreatePartDownloader(part: RangedPart): HttpPartDownloader {
        synchronized(partDownloaderList) {
            return partDownloaderList.getOrPut(part.from) {
                HttpPartDownloader(
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
                ).also { partDownloader: HttpPartDownloader ->
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

    private fun destroyPartDownloader(part: RangedPart) {
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
        serverLastModified = runCatching {
            response.lastModified?.let(TimeUtils::convertLastModifiedHeaderToTimestamp)
        }.getOrNull()
        if (response.isWebPage) {
            if (isDownloadItemIsAWebpage()) {
                // don't strict if it's a webpage let it download without checks
                strictDownload = false

                // this makes the file not resume able
                // we don't want to page downloaded with multi connection
                // so the download will be restarted [@see prepareDestination]
                supportsConcurrent = false
                downloadItem.contentLength = IDownloadItem.LENGTH_UNKNOWN
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
        if (downloadItem.contentLength == IDownloadItem.LENGTH_UNKNOWN) {
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
        if (downloadItem.contentLength == IDownloadItem.LENGTH_UNKNOWN) {
            //in case of blind part, update download item length
            if (parts.size == 1) {
                downloadItem.contentLength = parts[0].howMuchProceed()
            }
        }
    }
    private var lastSavedDownloadItem: HttpDownloadItem? = null
    private var lastSavedParts: List<RangedPart>? = null

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
                partListDb.setParts(id, RangedParts(copy))
                lastSavedParts = copy
            }
        }
    }

    override suspend fun saveState() {
        saveDownloadItem()
        saveParts()
    }

    fun getParts(): List<RangedPart> {
        //Make a copy because of CMException
        return parts.toList()
    }

    override fun reloadSettings() {
        onPreferredConnectionCountChanged()
    }

    override suspend fun extraConfigsReceived(config: DownloadJobExtraConfig) {
        // we don't have extra configs
    }
}
