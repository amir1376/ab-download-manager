package ir.amirab.util.config

class NestedMapCreator(
    private val separator: String = "."
) {
    private fun createNested(
        output: MutableMap<String, Any?>,
        segments: List<String>
    ): MutableMap<String, Any?> {
        if (segments.isEmpty()) {
            return output
        }
        val sub = segments.drop(1)
        val segment = segments.first()
        val maybeMap = output[segment]
        val map = if (maybeMap is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            maybeMap as MutableMap<String, Any?>
        } else {
            val createdMap = mutableMapOf<String, Any?>().apply {
                if (maybeMap != null) {
                    put("", maybeMap)
                }
            }
            createdMap
        }
        output[segment] = map
        return createNested(map, sub)
    }

    fun createdNested(
        flatten: Map<String, Any?>,
        output: MutableMap<String, Any?> = mutableMapOf()
    ): Map<String, Any?> {
        for ((key, value) in flatten) {
            val segments = key.split(separator)
            val map = createNested(output, segments.dropLast(1))
            val lastSegment = segments.last()
            val maybeMap = map[lastSegment]
            if (maybeMap is MutableMap<*, *>) {
                @Suppress("UNCHECKED_CAST")
                maybeMap as MutableMap<String, Any?>
                maybeMap.put("", value)
            } else {
                map[lastSegment] = value
            }


//            println(map)
        }
        return output
    }


    fun createFlatten(
        nested: Map<String, Any?>,
        prefixes: List<String> = emptyList(),
        output: MutableMap<String, Any?> = mutableMapOf()
    ): Map<String, Any?> {
        for ((key, value) in nested) {
            val flattenKeySegments = prefixes + key
//            println(flattenKeySegments.joinToString(separator))
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                createFlatten(value as Map<String, Any?>, flattenKeySegments, output)
            } else {
                val flattenKey = flattenKeySegments
                    .filter{ it.isNotEmpty() }
                    .joinToString(separator)
                output[flattenKey] = value
            }
        }
        return output
    }
}
