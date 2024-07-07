package ir.amirab.downloader.downloaditem

interface DownloadItemContext {
    operator fun <T : Element> get(key: Key<T>): T?

    fun <T> fold(initial: T, operation: (acc: T, element: Element) -> T): T

    fun minusKey(key: Key<*>): DownloadItemContext

    operator fun plus(context: DownloadItemContext): DownloadItemContext {
        if (context === EmptyContext) {
            return this
        }
        return context.fold(this) { acc, element ->
            //maybe same key alreadyExists
            val removed = acc.minusKey(element.getKey())
            if (removed === EmptyContext) {
                element
            } else {
                CombinedContext(removed, element)
            }
        }
    }

    interface Key<T : Element>
    interface Element : DownloadItemContext {

        fun getKey(): Key<*>
        override fun <T : Element> get(key: Key<T>): T? {
            @Suppress("UNCHECKED_CAST")
            return if (getKey() === key) return this as T
            else null
        }

        override fun minusKey(key: Key<*>): DownloadItemContext {
            return if (key === getKey()) {
                EmptyContext
            } else {
                this
            }
        }

        override fun <T> fold(initial: T, operation: (acc: T, element: Element) -> T): T {
            return operation(initial, this)
        }
    }
}

data object EmptyContext : DownloadItemContext {
    override fun <T : DownloadItemContext.Element> get(key: DownloadItemContext.Key<T>): T? {
        return null
    }

    override fun <T> fold(initial: T, operation: (acc: T, element: DownloadItemContext.Element) -> T): T {
        return initial
    }

    override fun plus(context: DownloadItemContext): DownloadItemContext {
        return context
    }

    override fun minusKey(key: DownloadItemContext.Key<*>): DownloadItemContext {
        return this
    }
}

private data class CombinedContext(
    val left: DownloadItemContext,
    val element: DownloadItemContext.Element,
) : DownloadItemContext {
    override fun <T : DownloadItemContext.Element> get(key: DownloadItemContext.Key<T>): T? {
        var cur = this
        while (true) {
            if (cur.element[key] != null) {
                @Suppress("UNCHECKED_CAST")
                return cur.element as T
            }
            val next = cur.left
            //make recursive function flatten
            if (next is CombinedContext) {
                cur = next
            } else {
                return next[key]
            }
        }
    }

    override fun <T> fold(initial: T, operation: (acc: T, element: DownloadItemContext.Element) -> T): T {
        return operation(left.fold(initial, operation), element)
    }

    override fun minusKey(key: DownloadItemContext.Key<*>): DownloadItemContext {
        if (element.getKey() === key) {
            return left
        }
        val newLeft = left.minusKey(key)
        return when {
            newLeft === EmptyContext -> element
            newLeft === left -> this
            else -> CombinedContext(newLeft, element)
        }
    }

    override fun toString(): String {
        return fold("") { acc, element ->
            if (acc.isEmpty()) {
                "$element"
            } else {
                "$acc , $element"
            }
        }.let { " { $it } " }
    }
}

//extensions
fun <T> DownloadItemContext.map(transform: (DownloadItemContext.Element) -> T): List<T> {
    return fold(mutableListOf()) { acc, element ->
        acc.apply { add(transform(element)) }
    }
}

fun DownloadItemContext.minusKeys(vararg keys: DownloadItemContext.Key<*>) {
    var cur = this
    for (key in keys) {
        cur = cur.minusKey(key)
    }
}

fun DownloadItemContext.toList() = map { it }

fun DownloadItemContext.keys() = map { it.getKey() }

fun DownloadItemContext.isEmpty(): Boolean {
    return this === EmptyContext
}

val DownloadItemContext.size: Int
    get() {
        return fold(0) { acc, _ -> acc + 1 }
    }

fun DownloadItemContext.iterator(): Iterator<DownloadItemContext.Element> {
    return toList().iterator()
}

fun DownloadItemContext.contains(element: DownloadItemContext.Element): Boolean {
    return contains(element.getKey())
}

fun DownloadItemContext.contains(key: DownloadItemContext.Key<*>): Boolean {
    return this[key] != null
}

fun DownloadItemContext.containsAll(elements: Collection<DownloadItemContext.Element>): Boolean {
    for (el in elements) {
        if (!contains(el)) {
            return false
        }
    }
    return true
}