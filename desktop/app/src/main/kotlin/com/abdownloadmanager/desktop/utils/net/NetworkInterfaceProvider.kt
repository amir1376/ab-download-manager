package com.abdownloadmanager.desktop.utils.net

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.storage.DesktopExtraQueueSettings
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import ir.amirab.downloader.connection.NetworkInterfaceBinder
import ir.amirab.downloader.connection.QueueNetworkPolicy
import java.net.InetAddress
import java.net.NetworkInterface

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
     * Discover the machine's usable network interfaces as `(identifier, label)`
     * pairs the user can pick from. The identifier is either the interface name
     * or one of its local IP addresses (both are accepted by [resolveAddress]);
     * the label is a friendly "Name (ip)" string. Loopback and link-local
     * addresses are skipped.
     */
    fun discoverInterfaces(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        for (netIf in NetworkInterface.getNetworkInterfaces()) {
            val addrs = netIf.inetAddresses
                .toList()
                .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
            if (addrs.isEmpty()) continue
            result.add(netIf.name to netIf.name)
            for (addr in addrs) {
                result.add(addr.hostAddress to "${netIf.name} (${addr.hostAddress})")
            }
        }
        return result
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
}
