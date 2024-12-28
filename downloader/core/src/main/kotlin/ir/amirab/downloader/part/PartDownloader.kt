package ir.amirab.downloader.part

import ir.amirab.downloader.anntation.HeavyCall
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.connection.Connection
import ir.amirab.downloader.connection.response.expectSuccess
import ir.amirab.downloader.destination.DestWriter
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.exception.DownloadValidationException
import ir.amirab.downloader.exception.PartTooManyErrorException
import ir.amirab.downloader.exception.ServerPartIsNotTheSameAsWeExpectException
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.downloader.utils.printStackIfNOtUsual
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okio.*
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext
import kotlin.math.min

val PART_MAX_TRIES = 10
const val RetryDelay = 1_000L

/**
 * @param strictMode
 *  `false` - this is not the purpose of its app, so we don't strict here
 *
 *  download part without checking for length validation
 *  this is where we want to only copy data arrived from server
 *  for example, a web page link that maybe get us different response length
 *  we only need to download it no matter what is inside
 *
 *  `true` - main purpose of this class
 *
 *  validate download size before trying to write to the filesystem
 */
class PartDownloader(
    val credentials: IDownloadCredentials,
    val getDestWriter: () -> DestWriter,
    val part: Part,
    val client: DownloaderClient,
    val speedLimiters: List<Throttler>,
    val strictMode: Boolean,
    private val partSplitLock: Any,
) {
    class ShouldNotHappened(msg: String?) : RuntimeException(msg)

    private var thread: Thread? = null

    private suspend fun establishConnection(
        from: Long,
        to: Long?,
    ): Connection {
        val connect = client.connect(credentials, from, to)
        // make sure this is a 2xx response
        kotlin.runCatching {
            connect.responseInfo.expectSuccess()
        }
            .onFailure {
                // close connection before throwing exception
                kotlin.runCatching {
                    connect.closeable.close()
                }
            }
            .getOrThrow()
        val source = speedLimiters.fold<Throttler, Source>(connect.source) { acc, throttler ->
            throttler.source(acc)
        }
        return connect.copy(
            source = source.buffer()
        )
    }

    private var scope: CoroutineScope? = null

    @Volatile
    internal var active = false

    private val partSplitSupport = PartSplitSupport(part, partSplitLock)

    //this method is invoked only in one thread for every instance
    private fun howMuchCanRead(expandToBufferSize: Long): Long {
        return partSplitSupport.howMuchCanRead(
            expandToBufferSize = expandToBufferSize,
            tryToExtendSafeZone = true
        )
    }

    fun canBeSplit(): Boolean {
        return partSplitSupport.canSplit()
    }

    @Volatile
    internal var tries = 0

    // make sure to not lake resource in this exception
    @Volatile
    private var lastCriticalException: Throwable? = null

    // make sure to not lake resource in this exception
    @Volatile
    private var lastException: Throwable? = null

    //just turn on (fast)
    fun start() {
        synchronized(this) {
            if (active) {
                return
            }
            stop = false
            active = true
        }
        val scope = CoroutineScope(SupervisorJob()).also {
            this.scope = it
        }
        scope.launch {
            tries = 0
            lastCriticalException = null
            lastException = null
            val result = kotlin.runCatching {
                while (coroutineContext.isActive || !stop) {
                    if (tries > 0) {
                        delay(RetryDelay)
//                        println("#${part.from}retrying $tries")
                    }
                    if (haveToManyErrors()) {
//                        println("tell them we have error!")
                        iCantRetryAnymore(
                            PartTooManyErrorException(
                                part,
                                lastException
                                    ?: Exception("BUG : if you see me please report it to the developer! when we encounter error so it have to be a least one last exception"),
                            )
                        )
                    }
                    if (part.isCompleted) {
                        println("WARNING $part is completed")
                    }
                    try {
                        download()
                    } catch (e: Exception) {
                        tries++
                        onCanceled(e)
                        when (canRetry(e)) {
                            CanRetryResult.Yes -> continue
                            CanRetryResult.No -> {}
                            CanRetryResult.NoAndStopDownloadJob -> iCantRetryAnymore(e)
                        }
                        break
                    }
                    //download progress started, but maybe we have errors
                    //wait for a finish/error event...
                    //await for cancel status to be emitted!
                    val status = withContext(NonCancellable) {
                        awaitFinishOrError()
                    }
                    when (status) {
                        is PartDownloadStatus.Canceled -> {
                            tries++
                            when (canRetry(status.e)) {
                                CanRetryResult.Yes -> continue
                                CanRetryResult.No -> {}
                                CanRetryResult.NoAndStopDownloadJob -> iCantRetryAnymore(status.e)
                            }
                            break
                        }

                        PartDownloadStatus.Completed -> break
                        else -> throw ShouldNotHappened("should not happened!")
                    }
                }
            }

            active = false
            if (!part.isCompleted) {
                part.statusFlow.value = PartDownloadStatus.IDLE
            }

            result.onFailure {
                if (it is ShouldNotHappened) {
                    throw it
                }
            }
        }
    }

    private sealed interface CanRetryResult {
        data object Yes : CanRetryResult
        data object No : CanRetryResult
        data object NoAndStopDownloadJob : CanRetryResult
    }

    private fun canRetry(e: Throwable): CanRetryResult {
        return when {
            ExceptionUtils.isNormalCancellation(e) -> {
                CanRetryResult.No
            }

            e is DownloadValidationException -> if (e.isCritical()) {
                //download validation occurs, and also it is critical,
                //so we can't proceed any further
                CanRetryResult.NoAndStopDownloadJob
            } else {
                CanRetryResult.Yes
            }

            else -> {
                CanRetryResult.Yes
            }
        }
    }

    lateinit var onTooManyErrors: ((Throwable) -> Unit)
    private fun iCantRetryAnymore(throwable: Throwable) {
        lastCriticalException = throwable
        GlobalScope.launch {
            onTooManyErrors(throwable)
        }
    }

    private fun haveToManyErrors(): Boolean {
        return tries >= PART_MAX_TRIES
    }

    private fun haveCriticalError(): Boolean {
        return lastCriticalException != null
    }

    internal fun injured(): Boolean {
        return haveToManyErrors() || haveCriticalError()
    }

    private suspend fun download() {
//        thisLogger().info("going to copy data to destination")
        onNewStatus(PartDownloadStatus.SendGet)
        //we copy part because maybe part::to property will change during part split,
        //so we make backup of current part to validate http response
        val partCopy = part.copy()
        val conn = establishConnection(partCopy.current, partCopy.to)
//        thisLogger().info("connection established")
        if (stop || !coroutineContext.isActive) {
            conn.closeable.close()
            throw CancellationException()
        }
        val contentLength = conn.contentLength.let {
            if (it == -1L) {
                //in case of no end is come from headers
                null
            } else {
                it
            }
        }
        if (contentLength != partCopy.remainingLength) {
            if (strictMode) {
                conn.closeable.close()
                throw ServerPartIsNotTheSameAsWeExpectException(
                    start = partCopy.current,
                    end = partCopy.to,
                    expectedLength = partCopy.remainingLength,
                    actualLength = contentLength
                )
            }
        }
        thread = thread {
            if (stop || Thread.currentThread().isInterrupted) {
                conn.closeable.close()
                onCanceled(CancellationException())
                return@thread
            }
//            thisLogger().info("going to copy data to destination $conn")
            try {
                conn.closeable.use {
                    conn.source.use { connectionStream ->
                        getDestWriter().use { writer ->
                            copyDataSync(connectionStream, writer, contentLength ?: Long.MAX_VALUE)
                        }
                    }
                }
            } catch (e: Exception) {
                onCanceled(e)
            } finally {
                conn.closeable.close()
                thread = null
            }
        }
    }

    @Volatile
    var stop = false
    suspend fun stop() {
        stop = true
        thread?.interrupt()
        scope?.coroutineContext?.job?.cancel()
    }

    suspend fun join() {
        withContext(Dispatchers.IO) {
            scope?.coroutineContext?.job?.join()
            thread?.join()
        }
    }

    @HeavyCall
    private fun copyDataSync(source: BufferedSource, destWriter: DestWriter, count: Long) {
//        println("copying range to file --- ${part.current}-${part.to}")
        val buffer = Buffer()
        var totalReadCount = 0L
        var firstLoop = true
        val bufferSize = DEFAULT_BUFFER_SIZE.toLong()
        while (true) {
            if (stop || Thread.currentThread().isInterrupted) {
                onCanceled(CancellationException())
                break
            }
            val howMuchICanReadAllowed = howMuchCanRead(bufferSize)
            val homMuchReadFromBuffer = min(bufferSize, howMuchICanReadAllowed)
//            require(part.current + homMuchReadFromBuffer <= part.maxAllowedCurrent) {
//                """$partSplitSupport
//                canRead:${homMuchReadFromBuffer}"""
//            }
//            require(part.current + homMuchReadFromBuffer <= partSplitSupport.safeZone + 1) {
//                """a
//                    part=${part} isCompleted =${part.isCompleted}
//                    split part $partSplitSupport
//                    howMuch:${homMuchReadFromBuffer}
//                    actual:${part.current + homMuchReadFromBuffer}
//                    expected:${partSplitSupport.safeZone}
//                """.trimIndent()
//            }
            if (howMuchICanReadAllowed <= 0) {
                if (part.isCompleted) {
                    onFinish()
                } else {
                    onCanceled(CancellationException("it seems our part was split so we are canceled $part"))
                }
                break
            }
            val readCount = source.read(buffer, homMuchReadFromBuffer)
//            require(readCount <= homMuchReadFromBuffer) {
//                "read count $readCount is bigger than homMuchReadFromBuffer $homMuchReadFromBuffer"
//            }
            if (readCount == -1L) {
                onFinish()
                break
            }
            destWriter.write(buffer, readCount)
            totalReadCount += readCount
            part.current += readCount
//                require (part.current-part.from == totalReadCount)
            if (firstLoop) {
                tries = 0
                onNewStatus(PartDownloadStatus.ReceivingData)
                firstLoop = false
            }
//                onProgress(totalReadCount, count)
        }
    }

    private fun onCanceled(e: Throwable) {
        lastException = e
        val canceled = PartDownloadStatus.Canceled(e)
        onNewStatus(canceled)
        e.printStackIfNOtUsual()
        if (!canceled.isNormalCancellation()) {
//            e.printStackTrace()
        } else {
//            println("part cancelled because of ${e.localizedMessage ?: e::class.simpleName}")
//            e.printStackTrace()
        }
    }

    private fun onFinish() {
        synchronized(partSplitSupport) {
            if (part.isBlind) {
                part.setBlindAsCompleted()
            }
        }
        onNewStatus(PartDownloadStatus.Completed)
    }

    fun onNewStatus(partDownloadStatus: PartDownloadStatus) {
        _statusFlow.value = partDownloadStatus
    }

//    fun onProgress(
//        progress: Long, total: Long,
//    ) {
//        _progressFlow.tryEmit(PartDownloadProgress(progress, total))
//    }

    //should be sync with part split lock
    fun splitPart(): Part? {
        return partSplitSupport.splitPart()
    }

    private val _statusFlow = part.statusFlow
    val statusFlow = _statusFlow.asStateFlow()

//    private val _progressFlow = MutableStateFlow<PartDownloadProgress>(
//        PartDownloadProgress(
//            part.howMuchProceed(),
//            part.partLength
//        )
//    )
//    val progressFlow = _progressFlow.asStateFlow()
}

suspend fun PartDownloader.awaitFinishOrError(): PartDownloadStatus {
    return statusFlow.filter {
        when (it) {
            PartDownloadStatus.Completed,
            is PartDownloadStatus.Canceled,
                -> true

            PartDownloadStatus.ReceivingData,
            PartDownloadStatus.SendGet,
            PartDownloadStatus.IDLE,
                -> false
        }
    }.first()
}

suspend fun PartDownloader.awaitToEnsureDataBeingTransferred(): Boolean {
    return withTimeoutOrNull(5_000) {
        val isThatOk = statusFlow.filter {
            when (it) {
                PartDownloadStatus.Completed,
                PartDownloadStatus.ReceivingData,
                    -> true

                is PartDownloadStatus.Canceled,
                PartDownloadStatus.SendGet,
                PartDownloadStatus.IDLE,
                    -> false
            }
        }.first().let {
            when (it) {
                is PartDownloadStatus.Canceled -> false
                PartDownloadStatus.Completed -> true
                PartDownloadStatus.ReceivingData -> true
                PartDownloadStatus.SendGet,
                PartDownloadStatus.IDLE,
                    -> error("should not happen")
            }
        }
        isThatOk
    } ?: false
}

suspend fun PartDownloader.awaitIdle() {
    val first = statusFlow.filter {
        when (it) {
            is PartDownloadStatus.Canceled,
            PartDownloadStatus.Completed,
            PartDownloadStatus.IDLE,
                -> true

            PartDownloadStatus.SendGet,
            PartDownloadStatus.ReceivingData,
                -> false
        }
    }.first()
}