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
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val prepared = mutableListOf<String>()
            val started = mutableListOf<String>()
            val controller = MultipleDownloadsController(scope, folder,
                prepare = { url, destination -> assertEquals(folder, destination); prepared.add(url); url },
                enqueue = { url -> started.add(url); url.substringAfterLast('/') })
            controller.setText((1..30).joinToString("\n", transform = ::link))
            controller.start()
            controller.awaitIdle()
            assertEquals((1..30).map(::link), prepared)
            assertEquals(prepared, started)
            assertEquals(30, controller.items.value.count { it.status == MultipleDownloadsController.Status.Queued })
            controller.start()
            assertEquals(30, started.size)
        } finally { scope.cancel() }
    }

    @Test
    fun parsesMixedMarkdownAndPlainLinksAndRemovesDuplicatesIncludingFragments() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val controller = MultipleDownloadsController(scope, folder, { url, _ -> url }, { it })
            controller.setText("${link(1)}#name\n[Second file](${link(2)})\n${link(1)}#other-name\nnot a link\n")
            assertEquals(listOf("${link(1)}#name", link(2)), controller.items.value.map { it.url })
        } finally { scope.cancel() }
    }

    @Test
    fun continuesAfterFailureAndRetryOnlyProcessesTheFailedFile() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var fail = true
            val started = mutableListOf<String>()
            val controller = MultipleDownloadsController(scope, folder,
                prepare = { url, _ -> if (url == link(2) && fail) error("File unavailable"); url },
                enqueue = { started.add(it); it })
            controller.setText((1..3).joinToString("\n", transform = ::link))
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(3)), started)
            assertEquals(MultipleDownloadsController.Status.Failed, controller.items.value[1].status)
            assertEquals("File unavailable", controller.items.value[1].detail)
            fail = false
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(3), link(2)), started)
        } finally { scope.cancel() }
    }

    @Test
    fun stoppingDuringResolutionLeavesTheCurrentLinkPendingAndKeepsAlreadyQueuedFiles() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var hold = true
            val blocked = CompletableDeferred<Unit>()
            val started = mutableListOf<String>()
            val controller = MultipleDownloadsController(scope, folder,
                prepare = { url, _ -> if (url == link(2) && hold) blocked.await(); url },
                enqueue = { started.add(it); it })
            controller.setText((1..3).joinToString("\n", transform = ::link))
            controller.start()
            assertEquals(MultipleDownloadsController.Status.Resolving, controller.items.value[1].status)
            controller.stop()
            controller.awaitIdle()
            assertEquals(listOf(link(1)), started)
            assertEquals(MultipleDownloadsController.Status.Pending, controller.items.value[1].status)
            hold = false
            controller.start()
            controller.awaitIdle()
            assertEquals((1..3).map(::link), started)
        } finally { scope.cancel() }
    }

    @Test
    fun stoppingDuringEnqueueRecordsThatFileOnceAndDoesNotStartTheNextLink() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val accepted = CompletableDeferred<Unit>()
            val started = mutableListOf<String>()
            val controller = MultipleDownloadsController(scope, folder,
                prepare = { url, _ -> url },
                enqueue = { started.add(it); if (it == link(1)) accepted.await(); it })
            controller.setText("${link(1)}\n${link(2)}")
            controller.start()
            controller.stop()
            accepted.complete(Unit)
            controller.awaitIdle()
            assertEquals(listOf(link(1)), started)
            assertEquals(MultipleDownloadsController.Status.Queued, controller.items.value[0].status)
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(2)), started)
        } finally { scope.cancel() }
    }

    @Test
    fun preventsDoubleStartAndInputChangesWhileProcessing() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val gate = CompletableDeferred<Unit>()
            var prepareCount = 0
            val controller = MultipleDownloadsController(scope, folder,
                prepare = { url, _ -> prepareCount++; gate.await(); url }, enqueue = { it })
            controller.setText(link(1))
            controller.start()
            controller.start()
            controller.setText(link(2))
            controller.setFolder("another-folder")
            assertEquals(link(1), controller.text.value)
            assertEquals(folder, controller.folder.value)
            gate.complete(Unit)
            controller.awaitIdle()
            assertEquals(1, prepareCount)
        } finally { scope.cancel() }
    }

    @Test
    fun addingMoreLinksAfterACompletedImportDoesNotDownloadPreviousLinksAgain() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val started = mutableListOf<String>()
            val controller = MultipleDownloadsController(scope, folder, { url, _ -> url }, { started.add(it); it })
            controller.setText(link(1))
            controller.start()
            controller.awaitIdle()
            controller.setText("${link(1)}\n${link(2)}")
            controller.start()
            controller.awaitIdle()
            assertEquals(listOf(link(1), link(2)), started)
        } finally { scope.cancel() }
    }

    @Test
    fun emptyOrInvalidInputAndBlankDestinationDoNotStartDownloads() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            var started = false
            val controller = MultipleDownloadsController(scope, folder, { url, _ -> started = true; url }, { it })
            controller.setText("not a URL")
            controller.start()
            assertFalse(started)
            controller.setText(link(1))
            controller.setFolder("   ")
            controller.start()
            assertFalse(started)
        } finally { scope.cancel() }
    }

    private suspend fun MultipleDownloadsController<*>.awaitIdle() {
        withTimeout(2000) { running.first { !it } }
    }
}
