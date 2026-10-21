package com.abdownloadmanager.desktop.pages.multipledownloads

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class MultipleDownloadsControllerTest {
    private val folder = "test-downloads"
    private fun link(index: Int) = "https://example.test/file-$index.zip"

    @Test
    fun processesThirtyLinksInOrderAndStartsEveryFileWithoutAnotherDialog() = runBlocking {
        QueueFixture().use { fixture ->
            val controller = fixture.controller
            controller.setText((1..30).joinToString("\n", transform = ::link))
            controller.start()
            controller.awaitIdle()
            assertEquals((1..30).map(::link), fixture.prepared)
            assertEquals(fixture.prepared, fixture.started)
            assertEquals(30, controller.items.value.count { it.status == MultipleDownloadsController.Status.Queued })
            controller.start()
            controller.awaitIdle()
            assertEquals(30, fixture.started.size)
            assertEquals(30, fixture.prepared.size)
        }
    }

    @Test
    fun parsesMixedMarkdownAndPlainLinksAndRemovesDuplicatesIncludingFragments() {
        QueueFixture().use { fixture ->
            fixture.controller.setText("${link(1)}#name\n[Second file](${link(2)})\n${link(1)}#other-name\nnot a link\n")
            assertEquals(listOf("${link(1)}#name", link(2)), fixture.controller.items.value.map { it.url })
        }
    }

    @Test
    fun continuesAfterFailureAndRetryOnlyProcessesTheFailedFile() = runBlocking {
        QueueFixture().use { fixture ->
            var fail = true
            fixture.beforePrepare = { url -> if (url == link(2) && fail) error("File unavailable") }
            val controller = fixture.controller
            controller.setText((1..3).joinToString("\n", transform = ::link))
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(3)), fixture.started)
            assertEquals(MultipleDownloadsController.Status.Failed, controller.items.value[1].status)
            assertEquals("File unavailable", controller.items.value[1].detail)
            fail = false
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(3), link(2)), fixture.started)
            assertEquals(listOf(link(1), link(2), link(3), link(2)), fixture.prepared)
        }
    }

    @Test
    fun stoppingDuringResolutionLeavesTheCurrentLinkPendingAndKeepsAlreadyQueuedFiles() = runBlocking {
        QueueFixture().use { fixture ->
            var hold = true
            val blocked = CompletableDeferred<Unit>()
            fixture.beforePrepare = { url -> if (url == link(2) && hold) blocked.await() }
            val controller = fixture.controller
            controller.setText((1..3).joinToString("\n", transform = ::link))
            controller.start()
            assertEquals(MultipleDownloadsController.Status.Resolving, controller.items.value[1].status)
            controller.stop()
            controller.awaitIdle()
            assertEquals(listOf(link(1)), fixture.started)
            assertEquals(MultipleDownloadsController.Status.Pending, controller.items.value[1].status)
            hold = false
            controller.start()
            controller.awaitIdle()
            assertEquals((1..3).map(::link), fixture.started)
        }
    }

    @Test
    fun stoppingDuringEnqueueRecordsThatFileOnceAndDoesNotStartTheNextLink() = runBlocking {
        QueueFixture().use { fixture ->
            val accepted = CompletableDeferred<Unit>()
            fixture.afterAccept = { transfer -> if (transfer.url == link(1)) accepted.await() }
            val controller = fixture.controller
            controller.setText("${link(1)}\n${link(2)}")
            controller.start()
            controller.stop()
            accepted.complete(Unit)
            controller.awaitIdle()
            assertEquals(listOf(link(1)), fixture.started)
            assertEquals(MultipleDownloadsController.Status.Queued, controller.items.value[0].status)
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(2)), fixture.started)
        }
    }

    @Test
    fun preventsDoubleStartAndInputChangesWhileProcessing() = runBlocking {
        QueueFixture().use { fixture ->
            val gate = CompletableDeferred<Unit>()
            fixture.beforePrepare = { gate.await() }
            val controller = fixture.controller
            controller.setText(link(1))
            controller.start()
            controller.start()
            controller.setText(link(2))
            controller.setFolder("another-folder")
            assertEquals(link(1), controller.text.value)
            assertEquals(folder, controller.folder.value)
            gate.complete(Unit)
            controller.awaitIdle()
            assertEquals(listOf(link(1)), fixture.prepared)
            assertEquals(listOf(link(1)), fixture.started)
        }
    }

    @Test
    fun addingMoreLinksSkipsPreviousLinksThatAreStillActive() = runBlocking {
        QueueFixture().use { fixture ->
            val controller = fixture.controller
            controller.setText(link(1))
            controller.start()
            controller.awaitIdle()
            controller.setText("${link(1)}\n${link(2)}")
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(2)), fixture.started)
            assertEquals(listOf(link(1), link(2)), fixture.prepared)
        }
    }

    @Test
    fun emptyOrInvalidInputAndBlankDestinationDoNotStartDownloads() {
        QueueFixture().use { fixture ->
            val controller = fixture.controller
            controller.setText("not a URL")
            controller.start()
            controller.setText(link(1))
            controller.setFolder("   ")
            controller.start()
            assertTrue(fixture.prepared.isEmpty())
            assertTrue(fixture.started.isEmpty())
        }
    }

    @Test
    fun completedFailedPausedAndDeletedDownloadsCanBeDownloadedAgain() = runBlocking {
        for (state in listOf(TransferState.Finished, TransferState.Failed, TransferState.Paused, TransferState.Deleted)) {
            QueueFixture().use { fixture ->
                val controller = fixture.controller
                controller.setText(link(1))
                repeat(3) { attempt ->
                    controller.start()
                    controller.awaitIdle()
                    assertEquals("Retry $attempt after $state", attempt + 1, fixture.started.size)
                    val latest = fixture.transfers.last()
                    if (state == TransferState.Deleted) fixture.transfers.remove(latest) else latest.state = state
                }
                assertEquals(listOf(link(1), link(1), link(1)), fixture.prepared)
            }
        }
    }

    @Test
    fun refreshMakesAFormerlyActiveLinkReadyForAnotherDownload() = runBlocking {
        QueueFixture().use { fixture ->
            val controller = fixture.controller
            controller.setText(link(1))
            controller.start()
            controller.awaitIdle()
            fixture.transfers.single().state = TransferState.Finished
            controller.refreshActiveDownloads()
            assertEquals(MultipleDownloadsController.Status.Ready, controller.items.value.single().status)
            assertEquals(listOf(link(1)), fixture.started)
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(1)), fixture.started)
        }
    }

    @Test
    fun reopenedWindowRecognizesAnActiveDownloadWithoutResolvingItAgain() = runBlocking {
        QueueFixture().use { fixture ->
            fixture.addTransfer(link(1), "already-running.zip")
            val reopened = fixture.newController()
            reopened.setText(link(1))
            reopened.start()
            reopened.awaitIdle()
            assertEquals(MultipleDownloadsController.Status.Queued, reopened.items.value.single().status)
            assertEquals("already-running.zip", reopened.items.value.single().detail)
            assertTrue(fixture.prepared.isEmpty())
            assertTrue(fixture.started.isEmpty())
        }
    }

    @Test
    fun activeDownloadWithADifferentFragmentDoesNotCreateAnotherCopy() = runBlocking {
        QueueFixture().use { fixture ->
            fixture.addTransfer("${link(1)}#old-name", "running.zip")
            val controller = fixture.controller
            controller.setText("${link(1)}#new-name\n${link(1)}#another-name")
            controller.start()
            controller.awaitIdle()
            assertEquals(1, controller.items.value.size)
            assertEquals(MultipleDownloadsController.Status.Queued, controller.items.value.single().status)
            assertEquals("running.zip", controller.items.value.single().detail)
            assertTrue(fixture.prepared.isEmpty())
            assertTrue(fixture.started.isEmpty())
        }
    }

    @Test
    fun downloadStartedElsewhereDuringResolutionIsNotEnqueuedAgain() = runBlocking {
        QueueFixture().use { fixture ->
            val metadata = CompletableDeferred<Unit>()
            fixture.beforePrepare = { metadata.await() }
            val controller = fixture.controller
            controller.setText(link(1))
            controller.start()
            assertEquals(MultipleDownloadsController.Status.Resolving, controller.items.value.single().status)
            fixture.addTransfer(link(1), "started-in-another-window.zip")
            metadata.complete(Unit)
            controller.awaitIdle()
            assertEquals(listOf(link(1)), fixture.prepared)
            assertTrue(fixture.started.isEmpty())
            assertEquals(MultipleDownloadsController.Status.Queued, controller.items.value.single().status)
            assertEquals("started-in-another-window.zip", controller.items.value.single().detail)
        }
    }

    @Test
    fun aDownloadThatFinishesBeforeTheNextRefreshCanBeRetriedImmediately() = runBlocking {
        QueueFixture().use { fixture ->
            fixture.afterAccept = { it.state = TransferState.Finished }
            val controller = fixture.controller
            controller.setText(link(1))
            repeat(2) {
                controller.start()
                controller.awaitIdle()
            }
            assertEquals(listOf(link(1), link(1)), fixture.started)
            assertEquals(listOf(link(1), link(1)), fixture.prepared)
        }
    }

    @Test
    fun startChecksTheQueueEvenWhenTheDisplayedStateIsOutOfDate() = runBlocking {
        QueueFixture().use { fixture ->
            val controller = fixture.controller
            controller.setText(link(1))
            assertEquals(MultipleDownloadsController.Status.Pending, controller.items.value.single().status)
            fixture.addTransfer(link(1), "external.zip")
            controller.start()
            controller.awaitIdle()
            assertEquals(MultipleDownloadsController.Status.Queued, controller.items.value.single().status)
            assertTrue(fixture.prepared.isEmpty())
            assertTrue(fixture.started.isEmpty())

            fixture.transfers.single().state = TransferState.Finished
            // No UI refresh occurs before the user clicks Download again.
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1)), fixture.started)
        }
    }

    private enum class TransferState { Running, Finished, Failed, Paused, Deleted }

    private data class Transfer(val url: String, val name: String, var state: TransferState = TransferState.Running)

    /** Models queue membership independently from the popup's remembered import results. */
    private inner class QueueFixture : AutoCloseable {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val transfers = mutableListOf<Transfer>()
        val prepared = mutableListOf<String>()
        val started = mutableListOf<String>()
        var beforePrepare: suspend (String) -> Unit = {}
        var afterAccept: suspend (Transfer) -> Unit = {}
        val controller = newController()

        fun newController() = MultipleDownloadsController(scope, folder,
            prepare = { url, destination ->
                assertEquals(folder, destination)
                prepared.add(url)
                beforePrepare(url)
                url
            },
            enqueue = { url ->
                started.add(url)
                val transfer = addTransfer(url)
                afterAccept(transfer)
                transfer.name
            },
            activeDownloads = {
                transfers.filter { it.state == TransferState.Running }.associate { it.url to it.name }
            },
        )

        fun addTransfer(url: String, name: String = url.substringAfterLast('/').substringBefore('#')): Transfer {
            return Transfer(url, name).also { transfers.add(it) }
        }

        override fun close() = scope.cancel()
    }

    private suspend fun MultipleDownloadsController<*>.awaitIdle() {
        withTimeout(2000) { running.first { !it } }
    }
}
