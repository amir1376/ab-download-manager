package ir.amirab.util.coroutines

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
