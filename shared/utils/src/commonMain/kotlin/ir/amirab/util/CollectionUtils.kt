package ir.amirab.util

fun <T> MutableList<T>.swap(
    index: Int, toPosition: Int
): MutableList<T> = apply {
    val p = set(toPosition, this[index])
    set(index, p)
}

fun <T> List<T>.swapped(index: Int, toPosition: Int): List<T> {
    val l = toMutableList()
    l.swap(index, toPosition)
    return l.toList()
}

fun <T> Set<T>.swapped(a: T, b: T): Set<T> {
    val l = toMutableList()
    val indexA = indexOf(a)
    val indexB = indexOf(b)
    val tmp = l.set(indexB, l[indexA])
    l.set(indexA, tmp)
    return l.toList().toSet()
}

fun <T> List<T>.shifted(index: Int, delta: Int): List<T> {
    val indices = indices
    require(index in indices)
    val newPosition = index + delta
    require(newPosition in indices)
    val l = toMutableList()
    l.add(newPosition, l.removeAt(index))
    return l.toList()
}
fun <T> MutableList<T>.shift(index: Int, delta: Int): List<T> {
    val indices = indices
    require(index in indices)
    val newPosition = index + delta
    require(newPosition in indices)
    add(newPosition, removeAt(index))
    return this
}

fun <T> MutableList<T>.shiftToLast(index: Int): List<T> {
    return shift(index, lastIndex - index)
}

fun <T> MutableList<T>.shiftToFirst(index: Int): List<T> {
    return shift(index, -index)
}
