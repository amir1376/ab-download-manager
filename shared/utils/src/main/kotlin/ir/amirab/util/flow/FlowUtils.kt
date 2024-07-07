package ir.amirab.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun Flow<Unit>.withStartEmit(): Flow<Unit> {
    return flow {
        emit(Unit)
        emitAll(this@withStartEmit)
    }
}

fun <T> createMutableStateFlowFromFlow(
    flow: Flow<T>,
    initialValue: T,
    updater: (T) -> Unit,
    scope: CoroutineScope,
): MutableStateFlow<T> {
    val downStream = MutableStateFlow(initialValue)
    flow.onEach { newFromUpStream ->
        downStream.update { newFromUpStream }
    }.launchIn(scope)
    downStream.onEach {
        updater(it)
    }.launchIn(scope)
    return downStream
}
fun <T> createMutableStateFlowFromStateFlow(
    flow: StateFlow<T>,
    updater: suspend (T) -> Unit,
    scope: CoroutineScope,
): MutableStateFlow<T> {
    val downStream = MutableStateFlow(flow.value)
    flow.onEach { newFromUpStream ->
        downStream.update { newFromUpStream }
    }.launchIn(scope)
    downStream.onEach {
        updater(it)
    }.launchIn(scope)
    return downStream
}