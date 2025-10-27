package ir.amirab.util.coroutines

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * a launch will be used for a suspend task and a deferred
 * the difference with [async] is that exceptions will be thrown immediately without calling [Deferred.await]
 */

fun <T> CoroutineScope.launchWithDeferred(
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val deferred = CompletableDeferred<T>()
    val job = launch {
        try {
            deferred.complete(block())
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        }
    }
    // cancell the job if caller request cancellation
    deferred.invokeOnCompletion {
        if (it is CancellationException) {
            job.cancel(it)
        }
    }
    return deferred
}
