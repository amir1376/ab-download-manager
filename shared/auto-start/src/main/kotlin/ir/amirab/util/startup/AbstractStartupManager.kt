package ir.amirab.util.startup

abstract class AbstractStartupManager(
    val name: String,
    val path: String,
    val args: List<String>,
) {
    @Throws(Exception::class)
    abstract fun install()
    abstract fun uninstall()

    fun getExecutableWithArgs(): String {
        return buildList {
            add(path.quoted())
            addAll(args)
        }.joinToString(" ")
    }

    private fun String.quoted(): String {
        return "\"$this\""
    }
}
