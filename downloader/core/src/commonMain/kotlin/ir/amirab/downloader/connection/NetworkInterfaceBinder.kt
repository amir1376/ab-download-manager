package ir.amirab.downloader.connection

import java.net.InetAddress

interface NetworkInterfaceBinder {
    /**
     * Returns the local address a [downloadId] should bind its sockets to,
     * or null to use the system default route.
     */
    fun getBoundAddress(downloadId: Long): InetAddress?

    /**
     * Records the resolved interface identifier for a download so that
     * [getBoundAddress] can resolve it later. The [identifier] is an
     * interface name or local IP accepted by the binder implementation.
     */
    fun assignInterface(downloadId: Long, identifier: String)
}
