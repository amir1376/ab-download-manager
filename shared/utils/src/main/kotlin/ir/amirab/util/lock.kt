package ir.amirab.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.sync.Mutex

object MutexIsLocked

inline fun <T> Mutex.tryLocked(block: () -> T): Either<MutexIsLocked, T> {
    if (tryLock()) {
        try {
            return block().right()
        } finally {
            unlock()
        }
    }
    return MutexIsLocked.left()
}
