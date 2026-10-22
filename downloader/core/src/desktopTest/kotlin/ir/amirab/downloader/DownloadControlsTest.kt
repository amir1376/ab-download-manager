package ir.amirab.downloader

import ir.amirab.downloader.db.MemoryDownloadListDB
import ir.amirab.downloader.db.MemoryDownloadPartStatesDB
import ir.amirab.downloader.destination.DestWriter
import ir.amirab.downloader.destination.DownloadDestination
import ir.amirab.downloader.downloaditem.DownloadItemContext
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.part.DownloadPart
import ir.amirab.downloader.queue.ManualDownloadQueue
import ir.amirab.downloader.utils.EmptyFileCreator
import ir.amirab.downloader.utils.IDiskStat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Collections
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.reflect.KClass

/**
 * Covers the controls used by "Stop All" and "Delete": which jobs get paused,
 * that removal always completes, and that queues can't restart items mid-stop or mid-delete.
 */
class DownloadControlsTest {
    private val tempFolder: File = Files.createTempDirectory("abdm-controls-test").toFile()

    @After
    fun cleanUp() {
        tempFolder.deleteRecursively()
    }

    @Test
    fun stopAllPausesEveryActiveStateIncludingRetryingAndJustRequestedResumes() = runBlocking {
        val fixture = ManagerFixture(tempFolder)
        val statuses = mapOf(
            1L to DownloadJobStatus.Downloading,
            2L to DownloadJobStatus.Resuming,
            3L to DownloadJobStatus.Retrying(timeUntilRetry = 5_000),
            4L to DownloadJobStatus.PreparingFile(percent = null),
            5L to DownloadJobStatus.IDLE,
            6L to DownloadJobStatus.IDLE,
            7L to DownloadJobStatus.Canceled(CancellationException()),
        )
        statuses.keys.forEach { fixture.addItem(it) }
        fixture.manager.boot()
        statuses.forEach { (id, status) -> fixture.job(id).forceStatus(status) }
        // resume requested, but the job hasn't reported a status yet
        fixture.job(6).forceActive()

        fixture.manager.stopAll()

        val paused = statuses.keys.filter { fixture.job(it).pauseCount > 0 }
        assertEquals(listOf(1L, 2L, 3L, 4L, 6L), paused)
    }

    @Test
    fun deleteRemovesTheEntryEvenIfFileCleanupFails() = runBlocking {
        val fixture = ManagerFixture(tempFolder)
        fixture.addItem(1)
        fixture.manager.boot()
        fixture.job(1).destination.failOnDelete = true

        fixture.manager.deleteDownload(1, alsoRemoveFile = { true })

        assertNull(fixture.listDb.getById(1))
        assertTrue(fixture.manager.downloadJobs.isEmpty())
        assertEquals(1, fixture.job(1).destination.deleteAttempts)
    }

    @Test
    fun deleteRemovesTheEntryEvenIfTheJobCannotBoot() = runBlocking {
        val fixture = ManagerFixture(tempFolder)
        // completed downloads have no job until something needs one
        fixture.addItem(1, status = DownloadStatus.Completed)
        fixture.manager.boot()
        fixture.failBootFor += 1L

        fixture.manager.deleteDownload(1, alsoRemoveFile = { false })

        assertNull(fixture.listDb.getById(1))
        assertTrue(fixture.manager.downloadJobs.isEmpty())
    }

    @Test
    fun aDownloadBeingDeletedCannotBeActivatedOrResumed() = runBlocking {
        val fixture = ManagerFixture(tempFolder)
        fixture.addItem(1)
        fixture.manager.boot()
        val job = fixture.job(1)
        var couldActivateDuringDelete: Boolean? = null
        // the queue reacts to the pause that delete triggers. emulate it trying to restart the item
        job.onPause = {
            couldActivateDuringDelete = fixture.manager.canActivateJob(1)
            fixture.manager.resume(1)
        }

        fixture.manager.deleteDownload(1, alsoRemoveFile = { true })

        assertEquals(false, couldActivateDuringDelete)
        assertEquals(0, job.resumeCount)
        assertNull(fixture.listDb.getById(1))
    }

    @Test
    fun manualQueueDoesNotStartAnItemAfterItWasClearedByStopAll() {
        QueueFixture().use { fixture ->
            fixture.queue.resume(1)
            // the start is scheduled but has not run yet; stop all clears the queue now
            fixture.queue.clearQueue()
            fixture.runPendingTasks()

            assertTrue(fixture.started.isEmpty())
        }
    }

    @Test
    fun manualQueueStillStartsItemsNormally() {
        QueueFixture().use { fixture ->
            fixture.queue.resume(1)
            fixture.runPendingTasks()

            assertEquals(listOf(1L), fixture.started)
        }
    }

    private class ManagerFixture(folder: File) {
        val listDb = MemoryDownloadListDB()
        val failBootFor = mutableSetOf<Long>()
        private val jobs = mutableMapOf<Long, FakeJob>()
        private val itemsFolder = folder

        val manager = DownloadManager(
            dlListDb = listDb,
            partListDb = MemoryDownloadPartStatesDB(),
            settings = DownloadSettings(),
            emptyFileCreator = EmptyFileCreator(
                diskStat = object : IDiskStat {
                    override fun getRemainingSpace(path: File): Long = Long.MAX_VALUE
                },
                useSparseFile = { false },
            ),
            downloaderRegistry = DownloaderRegistry().apply {
                add(FakeDownloader { item, manager ->
                    FakeJob(item, manager, FakeDestination(File(itemsFolder, item.name)))
                        .also {
                            it.failBoot = item.id in failBootFor
                            jobs[item.id] = it
                        }
                })
            },
            downloadDataFolder = folder,
        )

        suspend fun addItem(id: Long, status: DownloadStatus = DownloadStatus.Paused) {
            listDb.add(
                HttpDownloadItem(
                    link = "https://example.test/file-$id.bin",
                    id = id,
                    folder = itemsFolder.path,
                    name = "file-$id.bin",
                    status = status,
                )
            )
        }

        fun job(id: Long): FakeJob = requireNotNull(jobs[id]) { "no job was created for $id" }
    }

    private class FakeDownloader(
        private val create: (HttpDownloadItem, DownloadManager) -> FakeJob,
    ) : Downloader<HttpDownloadItem, FakeJob, HttpDownloadCredentials> {
        override fun createJob(item: HttpDownloadItem, downloadManager: DownloadManager) = create(item, downloadManager)
        override fun accept(item: IDownloadItem) = item is HttpDownloadItem
        override val downloadItemClass: KClass<HttpDownloadItem> = HttpDownloadItem::class
        override val downloadCredentialsClass: KClass<HttpDownloadCredentials> = HttpDownloadCredentials::class
        override val downloadJobClass: KClass<FakeJob> = FakeJob::class
        override val downloadItemSerializer: KSerializer<HttpDownloadItem> = HttpDownloadItem.serializer()
        override val downloadCredentialsSerializer: KSerializer<HttpDownloadCredentials> =
            HttpDownloadCredentials.serializer()
    }

    private class FakeJob(
        override val downloadItem: IDownloadItem,
        downloadManager: DownloadManager,
        val destination: FakeDestination,
    ) : DownloadJob(downloadManager) {
        var failBoot = false
        var pauseCount = 0
        var resumeCount = 0
        var onPause: suspend () -> Unit = {}

        fun forceStatus(status: DownloadJobStatus) {
            _status.value = status
        }

        fun forceActive() {
            _isDownloadActive.value = true
        }

        override fun getDestination(): DownloadDestination = destination
        override suspend fun actualBoot() {
            if (failBoot) throw IOException("corrupted part list")
        }

        override fun initializeDestination() {}
        override suspend fun reset() {}
        override suspend fun resume() {
            resumeCount++
            _isDownloadActive.value = true
            _status.value = DownloadJobStatus.Downloading
        }

        override suspend fun pause(throwable: Throwable) {
            pauseCount++
            onPause()
            _isDownloadActive.value = false
            _status.value = DownloadJobStatus.Canceled(throwable)
        }

        override suspend fun saveState() {}
        override fun getDownloadedSize(): Long = 0
        override fun reloadSettings() {}
        override suspend fun changeConfig(
            updater: (IDownloadItem) -> Unit,
            extraConfig: DownloadJobExtraConfig?,
        ): IDownloadItem = downloadItem

        override suspend fun extraConfigsReceived(config: DownloadJobExtraConfig) {}
    }

    private class FakeDestination(file: File) : DownloadDestination(file) {
        var failOnDelete = false
        var deleteAttempts = 0

        override fun deleteOutputFile() {
            deleteAttempts++
            if (failOnDelete) throw IOException("file is locked by another process")
            super.deleteOutputFile()
        }

        override fun getWriterFor(part: DownloadPart): DestWriter = error("not used")
        override fun canGetFileWriter(): Boolean = false
        override suspend fun prepareFile(onProgressUpdate: (Int?) -> Unit) {}
        override suspend fun isDownloadedPartsIsValid(): Boolean = true
        override fun flush() {}
    }

    /** Runs queue coroutines only when asked, so the test controls what happens before a scheduled start. */
    private class QueueFixture : AutoCloseable {
        private val tasks = ConcurrentLinkedQueue<Runnable>()
        private val scope = CoroutineScope(SupervisorJob() + Executor { tasks.add(it) }.asCoroutineDispatcher())
        val started: MutableList<Long> = Collections.synchronizedList(mutableListOf())
        val queue = ManualDownloadQueue(
            downloadEvents = object : DownloadManagerMinimalControl {
                override suspend fun startJob(id: Long, context: DownloadItemContext) {
                    started.add(id)
                }

                override suspend fun stopJob(id: Long, context: DownloadItemContext) {}
                override fun canActivateJob(id: Long): Boolean = true
                override val listOfJobsEvents: SharedFlow<DownloadManagerEvents> = MutableSharedFlow()
            },
            scope = scope,
        ).apply { boot() }

        fun runPendingTasks() {
            while (true) {
                (tasks.poll() ?: return).run()
            }
        }

        override fun close() = scope.cancel()
    }
}
