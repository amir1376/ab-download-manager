package ir.amirab.util.startup

abstract class AbstractStartupManager(
    val isJar: Boolean,
    val name: String,
    val path: String,
) {
    @Throws(Exception::class)
    abstract fun install()
    abstract fun uninstall()
}
