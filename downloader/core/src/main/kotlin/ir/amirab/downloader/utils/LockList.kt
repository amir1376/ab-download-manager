package ir.amirab.downloader.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


class LockList<I, T>(
    private val getId: (T) -> I,
) {
    private data class LockWithCounter(
        val lock: Any = Any(),
        val counter: AtomicInteger = AtomicInteger(),
    )

    private val locks: ConcurrentHashMap<I, LockWithCounter> = ConcurrentHashMap()
    fun <R> withLock(item: T, block: (T) -> R): R {
        val id = getId(item)
        val itemLock = locks.computeIfAbsent(id) { LockWithCounter() }
        itemLock.counter.incrementAndGet()
        return synchronized(itemLock.lock) {
            try {
                block(item)
            } finally {
                if (itemLock.counter.decrementAndGet() == 0) {
                    locks.remove(id)
                }
            }
        }
    }
}

class SuspendLockList<I, T>(
    private val getId: (T) -> I,
) {
    private data class LockWithCounter(
        val lock: Mutex = Mutex(),
        val counter: AtomicInteger = AtomicInteger(),
    )

    private val locks: ConcurrentHashMap<I, LockWithCounter> = ConcurrentHashMap()
    suspend fun <R> withLock(item: T, block: suspend (T) -> R): R {
        val id = getId(item)
        val itemLock = locks.computeIfAbsent(id) { LockWithCounter() }
        itemLock.counter.incrementAndGet()
        return itemLock.lock.withLock {
            try {
                block(item)
            } finally {
                if (itemLock.counter.decrementAndGet() == 0) {
                    locks.remove(id)
                }
            }
        }
    }
}