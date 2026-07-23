package com.abdownloadmanager.desktop.utils.net

import com.abdownloadmanager.desktop.storage.DesktopExtraQueueSettings
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import ir.amirab.downloader.connection.NetworkInterfaceBinder
import ir.amirab.downloader.connection.QueueNetworkPolicy
import ir.amirab.util.logger.appLogger
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Provides information about the machine's network interfaces and resolves
 * which local address a given download should bind its sockets to.
 *
 * This is the desktop counterpart of `dispatch`'s interface enumeration
 * (`list.rs` / `net.rs`): instead of running a SOCKS proxy, we bind each
 * connection directly to the chosen interface's local address.
 */
class NetworkInterfaceProvider(
    private val extraQueueSettingsStorage: IExtraQueueSettingsStorage<DesktopExtraQueueSettings>,
) : NetworkInterfaceBinder, QueueNetworkPolicy {

    /**
     * Per-download assigned interface identifier (a local IP address).
     * Populated by [assignInterface] when a download starts (via the queue
     * policy), so the egress binding can be resolved at connect time.
     */
    private val assignment = mutableMapOf<Long, String>()

    /**
     * Discover the machine's usable network interfaces as `(identifier, label)`
     * pairs the user can pick from.
     *
     * Each interface yields a single option identified by its first usable
     * local IP address (loopback and link-local addresses are skipped), with a
     * friendly "Name (ip)" label. Using the IP as the identifier guarantees the
     * socket can be bound to the exact interface the user selected, and avoids
     * showing the same interface twice (once by name, once by address).
     */
    fun discoverInterfaces(): List<Pair<String, String>> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Pair<String, String>>()
        for (netIf in NetworkInterface.getNetworkInterfaces()) {
            val addr = netIf.inetAddresses
                .toList()
                .firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }
                ?: continue
            val ip = addr.hostAddress
            if (!seen.add(ip)) continue
            result.add(ip to "${netIf.name} (${ip})")
        }
        return result
    }

    override fun interfaceForActiveIndex(queueId: Long, activeIndex: Int): String? {
        return extraQueueSettingsStorage.getExtraQueueSettings(queueId).networkInterface
    }

    override fun assignInterface(downloadId: Long, identifier: String) {
        assignment[downloadId] = identifier
    }

    override fun getBoundAddress(downloadId: Long): InetAddress? {
        val identifier = assignment[downloadId] ?: return null
        return resolveAddress(identifier)
    }

    override fun getBoundInterface(downloadId: Long): NetworkInterface? {
        val identifier = assignment[downloadId] ?: return null
        val addr = resolveAddress(identifier) ?: return null
        return NetworkInterface.getByInetAddress(addr)
    }

    /**
     * Resolve a configured interface identifier (a local IP address) to a
     * local [InetAddress] to bind sockets to.
     */
    fun resolveAddress(value: String): InetAddress? {
        return runCatching { InetAddress.getByName(value) }.getOrNull()
    }
}
