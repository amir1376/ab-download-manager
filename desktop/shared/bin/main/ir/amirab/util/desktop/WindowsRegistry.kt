package ir.amirab.util.desktop

object WindowsRegistry {

    /**
     * Set value in registry
     *
     * @param path full path including HKey and path
     * @param key key name or `null` for default
     */
    fun setValueInRegistry(
        path: String,
        key: String?,
        value: String,
    ) {
        val keySection = if (key == null) {
            arrayOf("/ve")
        } else {
            arrayOf("/v", quoted(key))
        }
        Runtime.getRuntime().exec(
            arrayOf(
                "reg", "add", quoted(path), *keySection,
                "/t", "REG_SZ",
                "/d", value,
                "/f",
            )
        )
    }


    /**
     * gets a value in Registry or null
     *
     * @param path full path including HKey and path
     * @param key key name or `null` for default
     *
     * @return the value or `null` on fail or not found
     */
    fun getValueInRegistry(
        path: String,
        key: String?,//null for default
    ): String? {
        val keySection = if (key == null) {
            arrayOf("/ve")
        } else {
            arrayOf("/v", quoted(key))
        }
        return try {
            val p = Runtime.getRuntime().exec(
                arrayOf(
                    "reg", "query", quoted(path), *keySection,
                )
            )
            p.inputStream.reader().use {
                val text = it.readText()
                val result = queryResultPattern.find(text)
                result?.groupValues?.getOrNull(1)
            }
        } catch (e: Throwable) {
            return null
        }
    }

    /**
     * remove entire path in registry.
     *
     * **BE CAREFUL** about this
     *
     * @param path full path including HKey and path
     */
    fun removePathInRegistry(path: String) {
        Runtime.getRuntime().exec(
            arrayOf(
                "reg", "delete", quoted(path),
                "/f",
            )
        )
    }

    /**
     * remove value in registry
     *
     * @param path full path including HKey and path
     * @param key key name or `null` for default
     */
    fun removeValueInRegistry(path: String, key: String?) {
        val keySection = if (key == null) {
            arrayOf("/ve")
        } else {
            arrayOf("/v", quoted(key))
        }
        Runtime.getRuntime().exec(
            arrayOf(
                "reg", "delete", quoted(path),
                *keySection, "/f",
            )
        )
    }

    // utils

    /**
     * wrap the [value] with quote name -> "name"
     */
    private fun quoted(value: String) = "\"$value\""


    /**
     * the correct result for
     * ```
     * reg query path [...params]
     * ```
     * @see [getValueInRegistry]
     *
     * would be
     *```
     * HKCU\path\to\destination
     *     type    name    value
     *```
     * if you use this so many times you may change it to `lazy` instead of `get`
     */
    private val queryResultPattern get() = """\n(?:\s+)\w+(?:\s)+\w+(?:\s)+(.+)""".toRegex()

}
