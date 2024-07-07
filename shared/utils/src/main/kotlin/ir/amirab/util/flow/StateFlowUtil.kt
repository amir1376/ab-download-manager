package ir.amirab.util.flow

import arrow.optics.Lens
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

class DerivedStateFlow<T>(
    private val getValue: () -> T,
    private val flow: Flow<T>,
) : StateFlow<T> {
    override val replayCache: List<T>
        get() = listOf(value)
    override val value: T
        get() = getValue()

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        coroutineScope {
            flow
                .distinctUntilChanged()
                .stateIn(this)
                .collect(collector)
        }
    }
}

@PublishedApi
internal class TwoWayDerivedStateFlow<T, R>(
    private val upStream: MutableStateFlow<T>,
    private val map: (T) -> R,
    private val unMap: (R) -> T,
) : MutableStateFlow<R> {

    override var value: R
        get() {
            return map(upStream.value)
        }
        set(v) {
            upStream.value = unMap(v)
        }

    override val replayCache: List<R>
        get() = listOf(value)

    private val _sc = MutableStateFlow(0)

    override val subscriptionCount: StateFlow<Int>
        get() = _sc.asStateFlow()

    private val _mappedStream = upStream.mapStateFlow(map)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        try {
            _sc.update { it + 1 }
            _mappedStream.collect(collector)
        } finally {
            _sc.update { it - 1 }
        }
    }

    override fun compareAndSet(expect: R, update: R): Boolean {
        return upStream.compareAndSet(
            expect = unMap(expect),
            update = unMap(update),
        )
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        upStream.resetReplayCache()
    }

    override fun tryEmit(value: R): Boolean {
        this.value = value
        return true
    }

    override suspend fun emit(value: R) {
        this.value = value
    }
}

/**
 * NOTE :
 * DON"T USE MutableStateFlow::update
 * If I use the map and unmap does not return equally it will cause to infinite loop
 *
 */
fun <T, R> MutableStateFlow<T>.mapTwoWayStateFlow(
    map: (T) -> R,
    unMap: T.(R) -> T,
): MutableStateFlow<R> {
    return TwoWayDerivedStateFlow(
        upStream = this,
        map = map,
        unMap = {
            unMap(value, it)
        },
    )
}
fun <T, R> MutableStateFlow<T>.mapTwoWayStateFlow(
    lens:Lens<T,R>
): MutableStateFlow<R> {
    return TwoWayDerivedStateFlow(
        upStream = this,
        map = lens::get,
        unMap = {
            lens.set(value,it)
        },
    )
}



fun <T, R> StateFlow<T>.mapStateFlow(
    transform: (T) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(value) },
        flow = this.map(transform)
    )
}

fun <T1, T2, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    transform: (a: T1, b: T2) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value)
        },
        flow = combine(a, b) { a, b ->
            transform(a, b)
        }
    )
}

fun <T1, T2, T3, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    c: StateFlow<T3>,
    transform: (a: T1, b: T2, c: T3) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value, c.value)
        },
        flow = combine(a, b, c) { a, b, c ->
            transform(a, b, c)
        }
    )
}

fun <T1, T2, T3, T4, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    c: StateFlow<T3>,
    d: StateFlow<T4>,
    transform: (a: T1, b: T2, c: T3, d: T4) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value, c.value, d.value)
        },
        flow = combine(a, b, c, d) { a, b, c, d ->
            transform(a, b, c, d)
        }
    )
}

fun <T1, T2, T3, T4, T5, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    c: StateFlow<T3>,
    d: StateFlow<T4>,
    e: StateFlow<T5>,
    transform: (a: T1, b: T2, c: T3, d: T4, e: T5) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value, c.value, d.value, e.value)
        },
        flow = combine(a, b, c, d, e) { a, b, c, d, e ->
            transform(a, b, c, d, e)
        }
    )
}

fun <T1, T2, T3, T4, T5, T6, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    c: StateFlow<T3>,
    d: StateFlow<T4>,
    e: StateFlow<T5>,
    f: StateFlow<T6>,
    transform: (a: T1, b: T2, c: T3, d: T4, e: T5, f: T6) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value, c.value, d.value, e.value, f.value)
        },
        flow = combine(a, b, c, d, e, f) { array ->
            @Suppress("UNCHECKED_CAST")
            transform(array[0] as T1, array[1] as T2, array[2] as T3, array[3] as T4, array[4] as T5, array[5] as T6)
        }
    )
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combineStateFlows(
    a: StateFlow<T1>,
    b: StateFlow<T2>,
    c: StateFlow<T3>,
    d: StateFlow<T4>,
    e: StateFlow<T5>,
    f: StateFlow<T6>,
    g: StateFlow<T7>,
    transform: (a: T1, b: T2, c: T3, d: T4, e: T5, f: T6, g: T7) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(a.value, b.value, c.value, d.value, e.value, f.value, g.value)
        },
        flow = combine(a, b, c, d, e, f) { array ->
            @Suppress("UNCHECKED_CAST")
            transform(
                array[0] as T1,
                array[1] as T2,
                array[2] as T3,
                array[3] as T4,
                array[4] as T5,
                array[5] as T6,
                array[6] as T7,
            )
        }
    )
}


inline fun <reified T, R> combineStateFlows(
    flows: Iterable<StateFlow<T>>,
    noinline transform: (list: Array<T>) -> R
): StateFlow<R> {
    return DerivedStateFlow(
        getValue = {
            transform(
                flows
                    .map { it.value }
                    .toTypedArray()
            )
        },
        flow = combine(flows) {
            transform(it)
        }
    )
}

inline fun <reified T, R> combineStateFlows(
    vararg flows: StateFlow<T>,
    noinline transform: (list: Array<T>) -> R
): StateFlow<R> {
    return combineStateFlows(listOf(*flows), transform)
}