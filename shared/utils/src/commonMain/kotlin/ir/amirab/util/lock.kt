@file:OptIn(ExperimentalContracts::class)

package ir.amirab.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.sync.Mutex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object MutexIsLocked

inline fun <T> Mutex.tryLocked(block: () -> T): Either<MutexIsLocked, T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    if (tryLock()) {
        try {
            return block().right()
        } finally {
            unlock()
        }
    }
    return MutexIsLocked.left()
}
