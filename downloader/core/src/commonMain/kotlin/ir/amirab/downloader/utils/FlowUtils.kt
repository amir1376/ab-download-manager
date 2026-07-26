package ir.amirab.downloader.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration

fun intervalFlow(interval: Duration) = flow {
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(interval)
    }
}
