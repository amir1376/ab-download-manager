package ir.amirab.util.startup

class HeadlessStartup(
    name: String,
    path: String,
    isJar: Boolean = false,
) : AbstractStartupManager(
    name = name,
    path = path,
    isJar = isJar
) {
    @Throws(Exception::class)
    override fun install() {
    }

    override fun uninstall() {
    }
}
