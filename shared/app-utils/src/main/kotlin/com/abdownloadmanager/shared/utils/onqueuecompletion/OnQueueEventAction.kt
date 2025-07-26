package com.abdownloadmanager.shared.utils.onqueuecompletion

interface OnQueueEventAction {
    suspend fun onQueueCompleted(queueId: Long)
    suspend fun onQueueEndTimeReached(queueId: Long)
}
