package ir.amirab.util.startup

abstract class AbstractDesktopStartupManager(
    val name: String,
    val path: String,
    val args: List<String>,
) : AbstractStartupManager() {
    protected fun getExecutableWithArgs(): String {
        return buildList {
            add(path.quoted())
            addAll(args)
        }.joinToString(" ")
    }


    private fun String.quoted(): String {
        return "\"$this\""
    }
}
