package ir.amirab.util


interface ISingleEntryCache<Key, Value> {
    fun getOrCreate(key: Key, create: (Key) -> Value): Value

    fun getCached(): Pair<Key, Value>?

    fun getKey(): Key? = getCached()?.first
    fun getValue(): Value? = getCached()?.second

    fun clear()
}

private class SingleEntryCache<Key, Value>(
    initialCache: Pair<Key, Value>?,
    private val threadSafe: Boolean,
) : ISingleEntryCache<Key, Value> {
    private var cache: Pair<Key, Value>? = initialCache
    override fun getCached(): Pair<Key, Value>? = cache
    override fun clear() {
        maybeSynchronized(this) {
            cache = null
        }
    }

    override fun getOrCreate(key: Key, create: (Key) -> Value): Value {
        maybeSynchronized(this) {
            val cache = this.cache
            if (cache == null || cache.first != key) {
                return create(key).also {
                    this.cache = key to it
                }
            }
            // use cache.
            return cache.second
        }
    }

    private inline fun <T> maybeSynchronized(
        lock: Any,
        block: () -> T
    ): T {
        return if (threadSafe) {
            synchronized(lock) {
                block()
            }
        } else {
            block()
        }
    }
}

fun <Key, Value> singleEntryCache(
    initialCache: Pair<Key, Value>? = null,
    threadSafe: Boolean = true,
): ISingleEntryCache<Key, Value> {
    return SingleEntryCache(
        initialCache = initialCache,
        threadSafe = threadSafe,
    )
}
