package ir.amirab.downloader.downloaditem

import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.suspendGuardedEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

abstract class DownloadJob(
    val downloadManager: DownloadManager,
) {
    protected val _isDownloadActive = MutableStateFlow(false)
    val isDownloadActive = _isDownloadActive.asStateFlow()

    abstract val downloadItem: IDownloadItem
    val id get() = downloadItem.id
    val scope = CoroutineScope(SupervisorJob())
    var activeDownloadScope: CoroutineScope? = null
    abstract fun getDestination(): DownloadDestination
    private val booted = suspendGuardedEntry()

    protected val _status = MutableStateFlow<DownloadJobStatus>(DownloadJobStatus.IDLE)
    val status = _status.asStateFlow()

    suspend fun boot() {
        booted.action {
            actualBoot()
        }
    }

    abstract suspend fun actualBoot()
    abstract fun initializeDestination()
    abstract suspend fun reset()
    abstract suspend fun resume()
    abstract suspend fun pause(throwable: Throwable = CancellationException())
    abstract suspend fun saveState()
    protected fun ensureBooted() {
        require(booted.isDone()) {
            "DownloadJob is not booted! Call boot() before using this object."
        }
    }

    protected fun startAutoSaver() {
        activeDownloadScope?.launch(Dispatchers.IO) {
            while (true) {
                saveState()
                delay(1000)
            }
        }
    }

    protected fun onDownloadResuming() {
        _status.update {
            DownloadJobStatus.Resuming
        }
        downloadManager.onDownloadResuming(downloadItem)
    }

    protected fun onDownloadResumed() {
        _status.update { DownloadJobStatus.Downloading }
        downloadManager.onDownloadResumed(downloadItem)
    }

    protected suspend fun onDownloadCanceled(throwable: Throwable) {
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

    protected fun onDownloadFinished() {
        scope.launch {
            try {
                getDestination().onAllPartsCompleted {
                    _status.value = DownloadJobStatus.PreparingFile(it)
                }
            } catch (e: Exception) {
                pause(e)
                return@launch
            }
            downloadItem.status = DownloadStatus.Completed
            downloadItem.completeTime = System.currentTimeMillis()
            _status.value = DownloadJobStatus.Finished
            _isDownloadActive.update { false }
            onDownloadFinishedBeforeSave()
            saveState()
            downloadManager.onDownloadFinished(downloadItem)
        }
    }

    open fun onDownloadFinishedBeforeSave() {}
    abstract fun getDownloadedSize(): Long

    fun downloadRemoved(
        removeOutputFile: Boolean = true,
    ) {
        ensureBooted()
        getDestination().cleanUpJunkFiles()
        if (removeOutputFile) {
            getDestination().deleteOutputFile()
        }
    }

    abstract fun reloadSettings()

    fun newScopeBasedOn(scope: CoroutineScope): CoroutineScope {
        return CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext.job))
    }

    fun close() {
        scope.cancel()
    }

    abstract suspend fun changeConfig(
        updater: (IDownloadItem) -> Unit,
        extraConfig: DownloadJobExtraConfig?
    ): IDownloadItem
    abstract suspend fun extraConfigsReceived(config: DownloadJobExtraConfig)
}
