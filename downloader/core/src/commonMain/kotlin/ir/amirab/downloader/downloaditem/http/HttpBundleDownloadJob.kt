package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.destination.HttpBundleDownloadDestination
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.part.PartDownloadStatus
import ir.amirab.downloader.utils.speedlimiter.SpeedLimiter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class HttpBundleDownloadJob(
    override val downloadItem: HttpDownloadItem,
    downloadManager: DownloadManager,
    private val client: HttpDownloaderClient,
) : DownloadJob(downloadManager) {
    private val bundle = requireNotNull(downloadItem.bundle)
    private val listDb by downloadManager::dlListDb
    private val parts = bundle.sources.mapIndexed { index, source ->
        HttpBundlePart(index.toLong(), source)
    }
    private lateinit var destination: HttpBundleDownloadDestination
    private val itemSaveLock = Mutex()
    private val partLoopLock = Mutex()
    private val jobThrottler = SpeedLimiter()
    private val partDownloaders = ConcurrentHashMap<Long, HttpBundlePartDownloader>()
    private val listenerJobs = ConcurrentHashMap<Long, Job>()
    private var lastSavedDownloadItem: HttpDownloadItem? = null

    override fun getDestination(): DownloadDestination = destination

    fun getParts(): List<HttpBundlePart> = parts.toList()

    override suspend fun actualBoot() {
        initializeDestination()
        destination.syncPartsWithFiles(parts)
        downloadItem.contentLength = bundle.totalContentLength
        applySpeedLimit()
    }

    override fun initializeDestination() {
        destination = HttpBundleDownloadDestination(
            outputFile = downloadManager.calculateOutputFile(downloadItem),
            tempDirectory = downloadManager.downloadDataFolder.resolve(id.toString()).resolve("http-bundle"),
            downloadId = id,
            appendExtensionToIncompleteDownloads = downloadManager.settings.appendExtensionToIncompleteDownloads,
            getAllParts = ::getParts,
        )
    }

    override suspend fun reset() {
        pause()
        clearPartDownloaderList()
        destination.cleanUpJunkFiles()
        destination.deleteOutputFile()
        parts.forEach(HttpBundlePart::resetCurrent)
        downloadItem.contentLength = bundle.totalContentLength
        downloadItem.status = DownloadStatus.Added
        downloadItem.startTime = null
        downloadItem.completeTime = null
        saveState()
        downloadManager.onDownloadItemChange(downloadItem)
    }

    override suspend fun resume() {
        if (isDownloadActive.value) {
            return
        }
        _isDownloadActive.update { true }
        resumeWithNewScope(createAndInitializeDownloadScope())
    }

    private fun createAndInitializeDownloadScope(): CoroutineScope {
        return newScopeBasedOn(scope).also { activeDownloadScope = it }
    }

    private suspend fun resumeWithNewScope(newActiveScope: CoroutineScope) {
        newActiveScope.launch {
            boot()
            destination.syncPartsWithFiles(parts)
            if (parts.all { it.isCompleted }) {
                onDownloadFinished()
                return@launch
            }
            onDownloadResuming()
            try {
                destination.prepareFile { _status.value = DownloadJobStatus.PreparingFile(it) }
                createPartDownloaderList()
                beginDownloadParts()
                startAutoSaver()
                downloadItem.status = DownloadStatus.Downloading
                if (downloadItem.startTime == null) {
                    downloadItem.startTime = System.currentTimeMillis()
                }
                saveState()
                onDownloadResumed()
            } catch (error: Exception) {
                scope.launch { pause(error) }
            }
        }.join()
    }

    override fun getDownloadedSize(): Long = parts.sumOf(HttpBundlePart::howMuchProceed)

    private fun getRequestedThreadCount(): Int {
        return (downloadItem.preferredConnectionCount
            ?: downloadManager.settings.defaultThreadCount).coerceAtLeast(1)
    }

    private fun beginDownloadParts() {
        if (partLoopLock.isLocked) {
            return
        }
        activeDownloadScope?.launch {
            if (!partLoopLock.tryLock()) {
                return@launch
            }
            try {
                val active = partDownloaders.values.filter { it.active }
                val countToStart = getRequestedThreadCount() - active.size
                if (countToStart > 0) {
                    partDownloaders.values
                        .filter { !it.active && !it.part.isCompleted }
                        .sortedBy { it.part.index }
                        .take(countToStart)
                        .forEach(HttpBundlePartDownloader::start)
                } else if (countToStart < 0) {
                    active.sortedByDescending { it.part.index }
                        .take(-countToStart)
                        .onEach(HttpBundlePartDownloader::stop)
                        .forEach {
                            it.join()
                            it.awaitIdle()
                        }
                }
            } finally {
                partLoopLock.unlock()
            }
        }
    }

    private fun createPartDownloaderList() {
        synchronized(partDownloaders) {
            parts.forEach(::getOrCreatePartDownloader)
        }
    }

    private fun getOrCreatePartDownloader(part: HttpBundlePart): HttpBundlePartDownloader {
        return partDownloaders.getOrPut(part.index) {
            HttpBundlePartDownloader(
                part = part,
                getDestWriter = { destination.getWriterFor(part) },
                credentials = part.source.asCredentials(downloadItem.downloadPage),
                client = client,
                speedLimiters = listOf(downloadManager.speedLimiter, jobThrottler),
            ).also { downloader ->
                downloader.onTooManyErrors = { error ->
                    scope.launch { pause(error) }
                }
                listenerJobs[part.index] = downloader.statusFlow.onEach { status ->
                    onPartStatusChanged(downloader, status)
                }.launchIn(scope)
            }
        }
    }

    private fun onPartStatusChanged(
        downloader: HttpBundlePartDownloader,
        status: PartDownloadStatus,
    ) {
        when (status) {
            is PartDownloadStatus.Canceled -> destination.onPartCancelled(downloader.part)
            PartDownloadStatus.Completed -> scope.launch {
                destination.onPartCancelled(downloader.part)
                if (parts.all { it.isCompleted }) {
                    onDownloadFinished()
                } else {
                    beginDownloadParts()
                }
            }
            PartDownloadStatus.ReceivingData,
            PartDownloadStatus.Connecting,
            PartDownloadStatus.IDLE -> Unit
        }
    }

    private fun clearPartDownloaderList() {
        listenerJobs.values.forEach(Job::cancel)
        listenerJobs.clear()
        partDownloaders.clear()
    }

    private suspend fun stopAllParts() {
        withContext(Dispatchers.IO) {
            partDownloaders.values.onEach(HttpBundlePartDownloader::stop).onEach {
                it.join()
                it.awaitIdle()
            }
        }
    }

    private suspend fun cancelDownloadScope() {
        activeDownloadScope?.coroutineContext?.job?.cancelAndJoin()
        activeDownloadScope = null
    }

    override suspend fun pause(throwable: Throwable) {
        boot()
        cancelDownloadScope()
        stopAllParts()
        onDownloadCanceled(throwable)
    }

    override suspend fun changeConfig(
        updater: (IDownloadItem) -> Unit,
        extraConfig: DownloadJobExtraConfig?,
    ): IDownloadItem {
        boot()
        val previousItem = downloadItem.copy()
        val newItem = previousItem.copy().apply(updater)
        val previousOutput = downloadManager.calculateOutputFile(previousItem)
        val newOutput = downloadManager.calculateOutputFile(newItem)
        val outputChanged = previousOutput != newOutput
        if (outputChanged) {
            if (isDownloadActive.value) {
                pause(CancellationException())
            }
            destination.moveOutput(newOutput)
        }
        downloadItem.applyFrom(newItem)
        if (outputChanged) {
            initializeDestination()
        }
        applySpeedLimit()
        saveState()
        return downloadItem
    }

    private fun applySpeedLimit() {
        jobThrottler.bytesPerSecond(downloadItem.speedLimit)
    }

    override suspend fun saveState() {
        destination.flush()
        itemSaveLock.withLock {
            val copy = downloadItem.copy()
            if (copy != lastSavedDownloadItem) {
                listDb.update(downloadItem)
                lastSavedDownloadItem = copy
            }
        }
    }

    override fun reloadSettings() {
        applySpeedLimit()
        beginDownloadParts()
    }

    override suspend fun extraConfigsReceived(config: DownloadJobExtraConfig) = Unit
}
