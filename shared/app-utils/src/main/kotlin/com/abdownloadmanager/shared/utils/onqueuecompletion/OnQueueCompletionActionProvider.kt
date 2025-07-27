package com.abdownloadmanager.shared.utils.onqueuecompletion

interface OnQueueCompletionActionProvider {
    suspend fun getOnQueueEventActions(queueId: Long): List<OnQueueEventAction>
}

class NoopOnQueueCompletionActionProvider : OnQueueCompletionActionProvider {
    override suspend fun getOnQueueEventActions(queueId: Long): List<OnQueueEventAction> {
        return emptyList()
    }
}
