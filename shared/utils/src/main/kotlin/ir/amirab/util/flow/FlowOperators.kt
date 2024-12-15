@file:Suppress("UNCHECKED_CAST", "unused")

package ir.amirab.util.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.time.Duration

private val NULL = Any()

/**
 * this is like simple but emits last emission
 * after last period
 */


fun <T> Flow<T>.rest(time: Long, emitLastEmissionWithoutRest: Boolean = false): Flow<T> {
    return channelFlow {
        var upStreamFinished = false
        var lastValue: Any? = NULL
        suspend fun pushValue() {
            val value = lastValue
            if (lastValue !== NULL) {
                lastValue = NULL
                send(value as T)
            }
        }

        val ticker = launch {
            while (isActive) {
                delay(time)
                pushValue()
                if (upStreamFinished) {
                    break
                }
            }
            close()
        }
        launch {
            collect {
                lastValue = it
            }
            if (emitLastEmissionWithoutRest) {
                pushValue()
                ticker.cancel()
            }
            upStreamFinished = true
        }

    }
}

fun <T, R> Flow<T>.concurrentMap(
    capacity: Int = Channel.BUFFERED,
    transformBlock: suspend (T) -> R
): Flow<R> {
    return flow {
        coroutineScope {
            map {
                async(start = CoroutineStart.LAZY) {
                    transformBlock(
                        it
                    )
                }
            }
                .buffer(capacity)
                .map {
                    it.start()
                    it.await()
                }
                .let {
                    emitAll(it)
                }
        }
    }
}

fun <T> Flow<T>.throttle(waitMillis: Int) = flow {
    coroutineScope {
        val context = coroutineContext
        var nextTime = 0L
        var delayPost: Deferred<Unit>? = null
        collect {
            val current = System.currentTimeMillis()
            if (nextTime < current) {
                nextTime = current + waitMillis
                emit(it)
                delayPost?.cancel()
            } else {
                val delayNext = nextTime
                delayPost?.cancel()
                delayPost = async(Dispatchers.Default) {
                    delay(nextTime - current)
                    if (delayNext == nextTime) {
                        nextTime = System.currentTimeMillis() + waitMillis
                        withContext(context) {
                            emit(it)
                        }
                    }
                }
            }
        }
    }
}


fun <T> Flow<T>.rateLimit(limit: Long, per: Duration): Flow<T> {
    return rateLimit(limit, per.inWholeMilliseconds)
}

fun <T> Flow<T>.rateLimit(limit: Long, per: Long) = flow<T> {
    coroutineScope {
        val context = coroutineContext
        var lastStartTime = System.currentTimeMillis()
        var remainingInDuration = limit
        val items = LinkedList<T>()
        var isDone = false
        launch(context) {
            collect {
                items.add(it)
            }
            isDone = true
        }
        launch(Dispatchers.Default) {
            while (isActive) {
                yield()
                if (remainingInDuration > 0) {
                    val removeFirst = items.removeFirstOrNull()
                    if (removeFirst != null) {
                        withContext(context) {
                            emit(removeFirst)
                        }
                        remainingInDuration--
                    } else {
                        if (isDone) {
                            break
                        }
                    }

                } else {
                    val waitUntil = lastStartTime + per
                    delay(waitUntil - System.currentTimeMillis())
                    lastStartTime = System.currentTimeMillis()
                    remainingInDuration = limit
                }
            }
        }
    }
}

fun <T> interval(time: Long, initialValue: T, newValue: (T) -> T): Flow<T> {
    var value = initialValue
    return interval(time)
        .map {
            value.apply {
                value = newValue(this)
            }
        }
}

fun interval(time: Long, timeOut: Long = time) = flow {
    if (timeOut > 0) {
        delay(timeOut)
    }
    emit(Unit)
    while (true) {
        delay(time)
        emit(Unit)
    }
}

fun <T> Flow<T>.saved(count: Int): Flow<List<T>> {
    require(count >= 0)
    return when (count) {
        0 -> emptyFlow()
        else -> scan<T, List<T>>(
            listOf()
        ) { l, v ->
            if (l.size < count) {
                l.plus(v)
            } else {
                l.drop(1).plus(v)
            }
        }.drop(1) // scan emits an initial value (emptyList)
    }
}

fun <T> Flow<List<T>>.pad(capacity: Int, fillAfter: Boolean) = map { actual ->
    val size = actual.size
    if (capacity > size) {
        val pad = List(capacity - size) { null }
        if (fillAfter) actual + pad
        else pad + actual
    } else actual
}

fun <T> Flow<T>.takeFirstEmitInEvery(millis: Long) = flow<T> {
    var lastEmitTime = 0L
    collect {
        val now = System.currentTimeMillis()
        if (now - lastEmitTime >= millis) {
            lastEmitTime = now
            emit(it)
        }
    }
}

fun <T> Flow<T>.chunked(count: Int): Flow<List<T>> = flow {
    val list = mutableListOf<T>()
    collect {
        if (list.size == count) {
            emit(list.toList())
            list.clear()
        } else {
            list.add(it)
        }
    }
    if (list.isNotEmpty()) {
        emit(list)
    }
}

fun <T> Flow<T>.onEachLatest(block:suspend (T)->Unit) = transformLatest {
    block(it)
    emit(it)
}

fun <T, R> Flow<T>.withPrevious(
    transform: (previous: T?, current: T) -> R,
): Flow<R> {
    return saved(2)
        .pad(2, false)
        .map {
            val previous = it[0]
            val current = it[1] as T
            transform(previous, current)
        }
}