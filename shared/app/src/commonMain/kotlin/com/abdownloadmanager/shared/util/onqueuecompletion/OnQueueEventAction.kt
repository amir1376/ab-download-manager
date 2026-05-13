package com.abdownloadmanager.shared.util.onqueuecompletion

interface OnQueueEventAction {
    suspend fun onQueueStarted(queueId: Long)
    suspend fun onQueueCompleted(queueId: Long)
    suspend fun onQueueEndTimeReached(queueId: Long)
    suspend fun onQueueStopped(queueId: Long)
}
