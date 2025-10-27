package ir.amirab.util.startup

class HeadlessStartupDesktop(
    name: String,
    path: String,
    args: List<String>,
) : AbstractDesktopStartupManager(
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
