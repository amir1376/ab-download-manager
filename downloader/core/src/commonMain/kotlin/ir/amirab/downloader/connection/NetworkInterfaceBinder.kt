package ir.amirab.downloader.connection

import java.net.InetAddress

/**
 * Resolves which local address a download's connections should be bound to,
 * based on the queue it belongs to. This is the mechanism that lets different
 * queues use different network interfaces (per-queue multi-homing).
 *
 * Implementations are platform specific (desktop only for now).
 */
interface NetworkInterfaceBinder {
    /**
     * Returns the local address to bind connections of the given download to,
     * or `null` to use the system default route. The implementation looks up
     * the download's current queue and reads its network interface setting.
     */
    fun getBoundAddress(downloadId: Long): InetAddress?
}
