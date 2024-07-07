package ir.amirab.downloader.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

fun intervalFlow(interval: Long) = flow {
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(interval)
    }
}