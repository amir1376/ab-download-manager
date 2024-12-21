package ir.amirab.util.startup

class HeadlessStartup(
    name: String,
    path: String,
    args: List<String>,
) : AbstractStartupManager(
    name = name,
    path = path,
    args = args,
) {
    @Throws(Exception::class)
    override fun install() {
    }

    override fun uninstall() {
    }
}
