package ir.amirab.downloader.queue

sealed interface QueueEvent {
    val queueId: Long

    data class QueueEndTimeReached(
        override val queueId: Long,
        val wasActive: Boolean,
    ) : QueueEvent

    data class OnQueueBecomesEmpty(
        override val queueId: Long,
    ) : QueueEvent

    data class OnQueueStartTimeReached(
        override val queueId: Long,
        val wasActive: Boolean,
    ) : QueueEvent
}
