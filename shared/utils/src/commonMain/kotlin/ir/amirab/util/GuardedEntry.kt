package ir.amirab.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BaseGuardedEntry {
    suspend fun awaitDone()
    fun isDone(): Boolean
}

interface GuardedEntry : BaseGuardedEntry {
    fun <T> action(block: () -> T): T?
}

interface SuspendGuardedEntry : BaseGuardedEntry {
    suspend fun <T> action(block: suspend () -> T): T?
}

private abstract class BaseGuardedEntryImpl : BaseGuardedEntry {
    private val _isBooted = MutableStateFlow(false)
    protected fun setIsDone() {
        _isBooted.value = true
    }

    override fun isDone(): Boolean {
        return _isBooted.value
    }

    override suspend fun awaitDone() {
        if (isDone()) return
        _isBooted.first { it }
    }
}


private class GuardedActionImpl : BaseGuardedEntryImpl(), GuardedEntry {
    private val mutex = Any()
    override fun <T> action(block: () -> T): T? {
        if (isDone()) {
            return null
        }
        return synchronized(mutex) {
            if (isDone()) {
                return null
            }
            val result = block()
            setIsDone()
            result
        }
    }
}

private class SuspendGuardedActionImpl : BaseGuardedEntryImpl(), SuspendGuardedEntry {
    private val mutex = Mutex()
    override suspend fun <T> action(block: suspend () -> T): T? {
        if (isDone()) {
            return null
        }
        return mutex.withLock {
            if (isDone()) {
                return null
            }
            val result = block()
            setIsDone()
            result
        }
    }
}

/**
prevent multiple threads call something. for example some object might require booting once. and calling boot again can lead to undefined behavior
```kt
val entry = guardedEntry()
thread {
entry.action { print("1") }
}
thread {
entry.action { print("2") }
}
```
only one of these prints will be printed!
 */

fun guardedEntry(): GuardedEntry = GuardedActionImpl()
fun suspendGuardedEntry(): SuspendGuardedEntry = SuspendGuardedActionImpl()
