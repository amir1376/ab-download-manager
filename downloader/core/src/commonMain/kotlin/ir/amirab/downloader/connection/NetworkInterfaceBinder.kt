package ir.amirab.downloader.connection

import java.net.InetAddress
import java.net.NetworkInterface

interface NetworkInterfaceBinder {
    /**
     * Returns the local address a [downloadId] should bind its sockets to,
     * or null to use the system default route.
     */
    fun getBoundAddress(downloadId: Long): InetAddress?

    /**
     * Returns the [NetworkInterface] a [downloadId] should use for egress,
     * or null to use the system default route. This pins egress by interface
     * index (via `IP_UNICAST_IF`) so the OS actually sends packets out the
     * chosen interface even when the routing table prefers another one.
     */
    fun getBoundInterface(downloadId: Long): NetworkInterface?

    /**
     * Records the resolved interface identifier for a download so that
     * [getBoundAddress] can resolve it later. The [identifier] is an
     * interface name or local IP accepted by the binder implementation.
     */
    fun assignInterface(downloadId: Long, identifier: String)
}
