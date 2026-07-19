package ir.amirab.downloader.connection

/**
 * Decides which network interface a download should use when it starts inside
 * a queue, based on how many downloads are already active. This powers
 * per-queue multi-homing: a queue configured with an ordered list of
 * interfaces (e.g. Wi-Fi, Ethernet, Ethernet2) spreads its concurrent
 * downloads across them round-robin.
 *
 * Implementations are platform specific (desktop only for now). When `null`
 * is returned the download uses the system default route.
 */
interface QueueNetworkPolicy {
    /**
     * Returns the interface identifier to bind for the download that is about
     * to become the [activeIndex]-th active download in [queueId], or `null`
     * for the system default route.
     */
    fun interfaceForActiveIndex(queueId: Long, activeIndex: Int): String?

    /**
     * Records the interface identifier for a download so the binder can resolve
     * it later when binding sockets. The [identifier] is an interface name or
     * local IP accepted by the binder implementation.
     */
    fun assignInterface(downloadId: Long, identifier: String)
}
