package ir.amirab.downloader.downloaditem

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.connection.response.isWebPage
import ir.amirab.downloader.destination.SimpleDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadItem.Companion.LENGTH_UNKNOWN
import ir.amirab.downloader.exception.DownloadValidationException
import ir.amirab.downloader.exception.FileChangedException
import ir.amirab.downloader.exception.TooManyErrorException
import ir.amirab.downloader.part.*
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.utils.printStackIfNOtUsual
import ir.amirab.downloader.utils.splitToRange
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
    private var booted = false


    var supportsConcurrent: Boolean = false

    private val _isDownloadActive = MutableStateFlow(false)
    val isDownloadActive = _isDownloadActive.asStateFlow()

    suspend fun boot() {
        if (!booted) {
            val outFile = downloadManager.calculateOutputFile(downloadItem)
            destination = SimpleDownloadDestination(outFile, downloadManager.diskStat)
            loadPartState()
            applySpeedLimit()
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
        downloadItem.contentLength = -1
        downloadItem.serverETag = null
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        strictDownload = true
        saveState()
        downloadManager.onDownloadItemChange(downloadItem)
    }


    suspend fun resume() {
        if (isDownloadActive.value) {
            return
        }
        _isDownloadActive.update { true }
        val newActiveScope = newScopeBasedOn(scope)
            .also {
                activeDownloadScope = it
            }
//        println(parts.filter { !it.isCompleted })
        newActiveScope.launch {
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
                enableProgressUpdater()
                startAutoSaver()
                downloadItem.status = DownloadStatus.Downloading
                if (downloadItem.startTime == null) {
                    downloadItem.startTime = System.currentTimeMillis()
                }
                saveState()
                onDownloadResumed()
            } catch (e: Exception) {
                e.printStackIfNOtUsual()
                scope.launch {
                    //moving to main scope and request to cancel this scope!
//                    println("error in resume ${e::class.qualifiedName}!")
                    pause(e)
                }
            }
        }.join()
    }


    private suspend fun prepareDestination(
        onProgressUpdate: (Int) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            destination.outputSize = downloadItem.contentLength
                .takeIf { strictDownload }
                    ?: LENGTH_UNKNOWN
            if (!destination.isDownloadedPartsIsValid()) {
                //file deleted or something!
                parts.forEach { it.resetCurrent() }
                saveState()
            }
//          thisLogger().info("preparing file")
            destination.prepareFile(onProgressUpdate)

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

    private val _downloadProgressFlow = MutableStateFlow(0L)
    val downloadProgressFlow = _downloadProgressFlow.asStateFlow()

    private fun enableProgressUpdater() {
        activeDownloadScope?.launch {
            while (isActive) {
                _downloadProgressFlow.value = getDownloadedSize()
                delay(100)
            }
        }
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
        val last = downloadItem.copy()
        downloadItem.apply(updater)
        if (last.preferredConnectionCount != downloadItem.preferredConnectionCount) {
            onPreferredConnectionCountChanged()
        }
        if (last.link != downloadItem.link) {
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
        if (downloadItem.contentLength == -1L) {
            setParts(
                listOf(Part(0, null, 0))
            )
        } else {
            if (supportsConcurrent){
                //split parts
                setParts(splitToRange(
                    minPartSize = downloadManager.settings.minPartSize,
                    maxPartCount = getRequestedPartitionCount().toLong(),
                    size = downloadItem.contentLength,
                ).map {
                    Part(it.first, it.last)
                })
            }else{
                setParts(
                    listOf(Part(0, (downloadItem.contentLength-1).takeIf { it>=0 }, 0))
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
                        if (downloadManager.settings.dynamicPartCreationMode) {
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
            if (throwable.isCritical()){
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
            scope.launch {
//                println("request pause send")
                pause(TooManyErrorException(throwable))
            }
        }
    }

//    var maxRetries = 3
//    var failTries = 0
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
            destination.onAllPartsCompleted()
            downloadItem.status = DownloadStatus.Completed
            if (downloadItem.contentLength == -1L) {
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

    private suspend fun fetchDownloadInfoAndValidate(
    ) {
//        println("fetch download ")

//        thisLogger().info("fetchDownloadInfoAndValidate")
        val response = client.head(downloadItem).expectSuccess()
        supportsConcurrent = response.resumeSupport
        val totalLength = response.totalLength
        val oldServerETag = downloadItem.serverETag
        val newServerETag = response.etag
        if (downloadItem.contentLength == -1L) {
            //new download
            downloadItem.contentLength = totalLength ?: -1
            downloadItem.serverETag=newServerETag
            // don't strict if it's a webpage let it download and not a link with resume support
            if (response.isWebPage() && !response.resumeSupport){
                strictDownload = false
            }
        } else {
            // at the beginning of download
            if (totalLength != downloadItem.contentLength) {
                throw FileChangedException.LengthChangedException(downloadItem.contentLength, totalLength ?: -1)
            }
            if (oldServerETag != null && newServerETag != null) {
                // we already know that sizes are the same,
                // but we also have etag header
                // so we have chance to compare file contents of local and server
                if (oldServerETag != newServerETag) {
                    throw FileChangedException.ETagChangedException(oldServerETag, newServerETag)
                }
            }
        }
//            thisLogger().info("fetchDownloadInfoAndValidate :${response.code},${response.headers} ")
        saveState()
    }

    suspend fun pause(throwable: Throwable = CancellationException()) {
        boot()
        activeDownloadScope?.coroutineContext?.job?.cancelAndJoin()
        activeDownloadScope = null
        withContext(Dispatchers.Default) {
            partDownloaderList.values.onEach {
                it.stop()
            }.onEach {
                it.join()
                it.awaitIdle()
            }
        }
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
}

sealed class DownloadJobStatus(
    val order: Int,
    private val downloadStatus: DownloadStatus
) {
    fun asDownloadStatus() = downloadStatus

    data object Downloading : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data object Resuming : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data class PreparingFile(val percent: Int) : DownloadJobStatus(1, DownloadStatus.Downloading),
        IsActive

    data class Canceled(val e: Throwable) : DownloadJobStatus(2,
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