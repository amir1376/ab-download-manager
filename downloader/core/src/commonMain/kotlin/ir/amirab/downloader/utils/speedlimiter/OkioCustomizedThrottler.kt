package ir.amirab.downloader.utils.speedlimiter

import okio.Buffer
import okio.ForwardingSink
import okio.ForwardingSource
import okio.Sink
import okio.Source
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// Customized version of the Okio implementation for use in the downloader.
// Every time the speed changes, it also resets [allocatedUntil]
// so the new speed takes effect immediately.

/**
 * Enables limiting of Source and Sink throughput. Attach to this throttler via [source] and [sink]
 * and set the desired throughput via [bytesPerSecond]. Multiple Sources and Sinks can be
 * attached to a single Throttler and they will be throttled as a group, where their combined
 * throughput will not exceed the desired throughput. The same Source or Sink can be attached to
 * multiple Throttlers and its throughput will not exceed the desired throughput of any of the
 * Throttlers.
 *
 * This class has these tuning parameters:
 *
 *  * `bytesPerSecond`: Maximum sustained throughput. Use 0 for no limit.
 *  * `waitByteCount`: When the requested byte count is greater than this many bytes and isn't
 *    immediately available, only wait until we can allocate at least this many bytes. Use this to
 *    set the ideal byte count during sustained throughput.
 *  * `maxByteCount`: Maximum number of bytes to allocate on any call. This is also the number of
 *    bytes that will be returned before any waiting.
 */
class OkioCustomizedThrottler internal constructor(
    /**
     * The nanoTime that we've consumed all bytes through. This is never greater than the current
     * nanoTime plus nanosForMaxByteCount.
     */
    private var allocatedUntil: Long,
) {
    private var bytesPerSecond: Long = 0L
    private var waitByteCount: Long = 8 * 1024 // 8 KiB.
    private var maxByteCount: Long = 256 * 1024 // 256 KiB.

    val lock: ReentrantLock = ReentrantLock()
    val condition: Condition = lock.newCondition()

    constructor() : this(allocatedUntil = System.nanoTime())

    /** Sets the rate at which bytes will be allocated. Use 0 for no limit. */
    @JvmOverloads
    fun bytesPerSecond(
        bytesPerSecond: Long,
        waitByteCount: Long = this.waitByteCount,
        maxByteCount: Long = this.maxByteCount,
        changeImmediately: Boolean = true,
    ) {
        lock.withLock {
            require(bytesPerSecond >= 0)
            require(waitByteCount > 0)
            require(maxByteCount >= waitByteCount)
            if (changeImmediately) {
                // quickly change speed by resetting [allocatedUntil]
                this.allocatedUntil = System.nanoTime()
            }
            this.bytesPerSecond = bytesPerSecond
            this.waitByteCount = waitByteCount
            this.maxByteCount = maxByteCount
            condition.signalAll()
        }
    }

    /**
     * Take up to `byteCount` bytes, waiting if necessary. Returns the number of bytes that were
     * taken.
     */
    internal fun take(byteCount: Long): Long {
        require(byteCount > 0)

        lock.withLock {
            while (true) {
                val now = System.nanoTime()
                val byteCountOrWaitNanos = byteCountOrWaitNanos(now, byteCount)
                if (byteCountOrWaitNanos >= 0) return byteCountOrWaitNanos
                condition.awaitNanos(-byteCountOrWaitNanos)
            }
        }
    }

    /**
     * Returns the byte count to take immediately or -1 times the number of nanos to wait until the
     * next attempt. If the returned value is negative it should be interpreted as a duration in
     * nanos; if it is positive it should be interpreted as a byte count.
     */
    internal fun byteCountOrWaitNanos(now: Long, byteCount: Long): Long {
        if (bytesPerSecond == 0L) return byteCount // No limits.

        val idleInNanos = maxOf(allocatedUntil - now, 0L)
        val immediateBytes = maxByteCount - idleInNanos.nanosToBytes()

        // Fulfill the entire request without waiting.
        if (immediateBytes >= byteCount) {
            allocatedUntil = now + idleInNanos + byteCount.bytesToNanos()
            return byteCount
        }

        // Fulfill a big-enough block without waiting.
        if (immediateBytes >= waitByteCount) {
            allocatedUntil = now + maxByteCount.bytesToNanos()
            return immediateBytes
        }

        // Looks like we'll need to wait until we can take the minimum required bytes.
        val minByteCount = minOf(waitByteCount, byteCount)
        val minWaitNanos = idleInNanos + (minByteCount - maxByteCount).bytesToNanos()

        // But if the wait duration truncates to zero nanos after division, don't wait.
        if (minWaitNanos == 0L) {
            allocatedUntil = now + maxByteCount.bytesToNanos()
            return minByteCount
        }

        return -minWaitNanos
    }

    private fun Long.nanosToBytes() = this * bytesPerSecond / 1_000_000_000L

    private fun Long.bytesToNanos() = this * 1_000_000_000L / bytesPerSecond

    /** Create a Source which honors this Throttler.  */
    fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                try {
                    val toRead = take(byteCount)
                    return super.read(sink, toRead)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw InterruptedIOException("interrupted")
                }
            }
        }
    }

    /** Create a Sink which honors this Throttler.  */
    fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                try {
                    var remaining = byteCount
                    while (remaining > 0L) {
                        val toWrite = take(remaining)
                        super.write(source, toWrite)
                        remaining -= toWrite
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw InterruptedIOException("interrupted")
                }
            }
        }
    }
}
