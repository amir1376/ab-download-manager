package ir.amirab.util.coroutines

import ir.amirab.util.ValueHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CoroutineScope.debounce(
    fn: () -> Unit,
    delayMillis: Long,
): () -> Unit {
    var lastRun: Job? = null
    return {
        lastRun?.cancel()
        lastRun = launch {
            delay(delayMillis)
            fn.invoke()
        }
    }
}

fun <T> CoroutineScope.debounce(
    fn: (T) -> Unit,
    delayMillis: Long,
    previousValueMerge: ((previous: T, current: T) -> T)? = null,
): (T) -> Unit {
    var lastRun: Job? = null
    val previousValueHolder = ValueHolder(null as T?)
    return { v ->
        val previousValue = previousValueHolder.value
        val param = if (previousValueMerge != null && previousValue != null) {
            previousValueMerge(previousValue, v)
        } else {
            v
        }
        previousValueHolder.value = v
        lastRun?.cancel()
        lastRun = launch {
            delay(delayMillis)
            fn.invoke(param)
        }
    }
}
