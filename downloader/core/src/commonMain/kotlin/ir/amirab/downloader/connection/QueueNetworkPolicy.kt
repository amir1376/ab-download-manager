package ir.amirab.downloader.connection

/**
 * Decides which network interface a download should use when it starts inside
 * a queue. This powers per-queue network binding: a queue configured with a
 * specific interface binds its downloads to that interface's local address.
 *
 * Implementations are platform specific (desktop only for now). When `null`
 * is returned the download uses the system default route.
 */
interface QueueNetworkPolicy {
    /**
     * Returns the interface identifier to bind for downloads in [queueId],
     * or `null` for the system default route. [activeIndex] is provided for
     * compatibility but is currently unused (a queue binds to a single
     * interface).
     */
    fun interfaceForActiveIndex(queueId: Long, activeIndex: Int): String?

    /**
     * Records the interface identifier for a download so the binder can resolve
     * it later when binding sockets. The [identifier] is an interface name or
     * local IP accepted by the binder implementation.
     */
    fun assignInterface(downloadId: Long, identifier: String)
}
