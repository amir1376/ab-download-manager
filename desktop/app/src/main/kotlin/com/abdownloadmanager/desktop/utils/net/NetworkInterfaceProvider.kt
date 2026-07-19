package com.abdownloadmanager.desktop.utils.net

import com.abdownloadmanager.desktop.storage.DesktopExtraQueueSettings
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import ir.amirab.downloader.connection.NetworkInterfaceBinder
import ir.amirab.downloader.queue.QueueManager
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket

/**
 * Provides information about the machine's network interfaces and resolves
 * which local address a given download queue should bind its sockets to.
 *
 * This is the desktop counterpart of `dispatch`'s interface enumeration
 * (`list.rs` / `net.rs`): instead of running a SOCKS proxy, we bind each
 * connection directly to the chosen interface's local address.
 */
class NetworkInterfaceProvider(
    private val extraQueueSettingsStorage: IExtraQueueSettingsStorage<DesktopExtraQueueSettings>,
    private val queueManager: QueueManager,
) : NetworkInterfaceBinder {
    /**
     * A usable (non-loopback, non-link-local, bindable) network interface.
     */
    data class NetworkInterfaceInfo(
        val name: String,
        val ipv4: List<String>,
        val ipv6: List<String>,
    ) {
        val allAddresses: List<String>
            get() = ipv4 + ipv6
    }

    /**
     * List all network interfaces that have at least one usable address.
     */
    fun listInterfaces(): List<NetworkInterfaceInfo> {
        return NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.inetAddresses.toList().isNotEmpty() }
            .mapNotNull { networkInterface ->
                val addrs = networkInterface.inetAddresses.toList()
                    .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
                    .filter { runCatching { bindable(it) }.getOrDefault(false) }
                if (addrs.isEmpty()) return@mapNotNull null
                NetworkInterfaceInfo(
                    name = networkInterface.name,
                    ipv4 = addrs.filter { it is java.net.Inet4Address }.map { it.hostAddress },
                    ipv6 = addrs.filter { it is java.net.Inet6Address }.map { it.hostAddress },
                )
            }
    }

    /**
     * Resolve the local [InetAddress] a queue's downloads should bind to.
     * Returns `null` when the queue has no interface override (system route).
     */
    fun getBoundAddressForQueue(queueId: Long): InetAddress? {
        val configured = extraQueueSettingsStorage.getExtraQueueSettings(queueId).networkInterface
            ?: return null
        return resolveAddress(configured)
    }

    override fun getBoundAddress(downloadId: Long): InetAddress? {
        val queueId = queueManager.findItemInQueue(downloadId) ?: return null
        return getBoundAddressForQueue(queueId)
    }

    /**
     * Resolve a configured value (interface name or raw IP) to a local [InetAddress].
     */
    fun resolveAddress(value: String): InetAddress? {
        // try as an interface name first
        runCatching {
            NetworkInterface.getByName(value)
        }.getOrNull()?.let { netIf ->
            return netIf.inetAddresses.toList()
                .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
        }
        // otherwise try as a literal IP
        return runCatching { InetAddress.getByName(value) }.getOrNull()
    }

    private fun bindable(address: InetAddress): Boolean {
        return runCatching {
            java.net.ServerSocket().use { socket ->
                socket.bind(java.net.InetSocketAddress(address, 0))
            }
        }.isSuccess
    }
}
