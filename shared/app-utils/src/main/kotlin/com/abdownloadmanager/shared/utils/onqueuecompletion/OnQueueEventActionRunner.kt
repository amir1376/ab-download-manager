package com.abdownloadmanager.shared.utils.onqueuecompletion

import ir.amirab.downloader.queue.QueueEvent
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OnQueueEventActionRunner(
    private val queueManager: QueueManager,
    private val scope: CoroutineScope,
    private val onQueueCompletionActionProvider: OnQueueCompletionActionProvider,
) {
    private var job: Job? = null

    /**
     * Starts listening to queue events and executes the corresponding actions.
     */
    @Synchronized
    fun startListening() {
        job?.cancel()
        job = queueManager.queueEvents
            .onEach {
                when (it) {
                    is QueueEvent.OnQueueBecomesEmpty -> {
                        val actions = onQueueCompletionActionProvider.getOnQueueEventActions(it.queueId)
                        actions.forEach { action ->
                            action.onQueueCompleted(it.queueId)
                        }
                    }

                    is QueueEvent.QueueEndTimeReached -> {
                        if (!it.wasActive) {
                            return@onEach
                        }
                        val actions = onQueueCompletionActionProvider.getOnQueueEventActions(it.queueId)
                        actions.forEach { action ->
                            action.onQueueEndTimeReached(it.queueId)
                        }
                    }

                    is QueueEvent.OnQueueStartTimeReached -> {
                        // nothing
                    }
                }
            }
            .launchIn(scope)
    }

    fun stopListening() {
        job?.cancel()
        job = null
    }
}


