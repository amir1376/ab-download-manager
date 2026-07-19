package com.abdownloadmanager.desktop.utils.net

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.DesktopExtraQueueSettings
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import ir.amirab.downloader.connection.NetworkInterfaceBinder
import ir.amirab.downloader.connection.QueueNetworkPolicy
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket

/**
 * Provides information about the machine's network interfaces and resolves
 * which local address a given download should bind its sockets to.
 *
 * This is the desktop counterpart of `dispatch`'s interface enumeration
 * (`list.rs` / `net.rs`): instead of running a SOCKS proxy, we bind each
 * connection directly to the chosen interface's local address.
 *
 * For per-queue multi-homing, a queue is configured with an ordered list of
 * interfaces. Concurrent downloads in that queue are spread across them
 * round-robin: the N-th active download uses the (N-1)-th interface in the
 * list, wrapping around.
 */
class NetworkInterfaceProvider(
    private val extraQueueSettingsStorage: IExtraQueueSettingsStorage<DesktopExtraQueueSettings>,
    private val appSettingsStorage: AppSettingsStorage,
) : NetworkInterfaceBinder, QueueNetworkPolicy {

    /**
     * Per-download assigned interface identifier (interface name or IP).
     * Populated by [assignInterface] when a download starts (via the queue
     * policy), so the egress binding can be resolved at connect time.
     */
    private val assignment = mutableMapOf<Long, String>()

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
     * Ordered list of interface identifiers for a queue, falling back to the
     * global default list when the queue has none configured.
     */
    private fun listForQueue(queueId: Long): List<String> {
        val configured = extraQueueSettingsStorage.getExtraQueueSettings(queueId).networkInterfaces
        if (configured.isNotEmpty()) return configured
        return parseList(appSettingsStorage.defaultNetworkInterfaces.value)
    }

    private fun parseList(raw: String): List<String> {
        return raw.split(',', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    override fun interfaceForActiveIndex(queueId: Long, activeIndex: Int): String? {
        val list = listForQueue(queueId)
        if (list.isEmpty()) return null
        return list[activeIndex % list.size]
    }

    override fun assignInterface(downloadId: Long, identifier: String) {
        assignment[downloadId] = identifier
    }

    override fun getBoundAddress(downloadId: Long): InetAddress? {
        val identifier = assignment[downloadId] ?: return null
        return resolveAddress(identifier)
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
            ServerSocket().use { socket ->
                socket.bind(java.net.InetSocketAddress(address, 0))
            }
        }.isSuccess
    }
}
