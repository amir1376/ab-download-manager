package ir.amirab.util

fun <T>MutableList<T>.swap(
    index: Int,toPosition: Int
): MutableList<T> =apply {
    val p=set(toPosition,this[index])
    set(index,p)
}

fun <T>List<T>.swapped(index:Int, toPosition: Int): List<T> {
    val l=toMutableList()
    l.swap(index,toPosition)
    return l.toList()
}
fun <T>Set<T>.swapped(a:T, b: T): Set<T> {
    val l=toMutableList()
    val indexA=indexOf(a)
    val indexB=indexOf(b)
    val tmp=l.set(indexB,l[indexA])
    l.set(indexA,tmp)
    return l.toList().toSet()
}