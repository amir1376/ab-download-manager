package ir.amirab.util.config.datastore

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

private class TransformedSerializer<T, R>(
    private val upstream: Serializer<T>,
    private val map: (T) -> R,
    private val unMap: (R) -> T,
) : Serializer<R> {
    override val defaultValue: R get() = map(upstream.defaultValue)
    override suspend fun readFrom(input: InputStream): R {
        return map(upstream.readFrom(input))
    }

    override suspend fun writeTo(t: R, output: OutputStream) {
        upstream.writeTo(unMap(t), output)
    }
}

@PublishedApi
internal fun <T, R> Serializer<T>.map(
    map: (T) -> R,
    unMap: (R) -> T,
): Serializer<R> {
    return TransformedSerializer(this, map, unMap)
}