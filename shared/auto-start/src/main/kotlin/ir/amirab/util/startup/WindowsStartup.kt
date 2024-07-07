package ir.amirab.util.startup

class WindowsStartup(
    name: String,
    path: String,
    isJar: Boolean = false,
) : AbstractStartupManager(
    name = name,
    path = path,
    isJar = isJar
) {

    private fun getData(): String {

        return if (isJar) {
            val javaHome = System.getProperty("java.home") + "\\bin\\javaw.exe"
            "$javaHome -jar \"$path\""
        } else {
            super.path
        }
    }
    @Throws(Exception::class)
    override fun install() {
        val data=getData()

        Runtime.getRuntime().exec(
            arrayOf("reg", "add", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\", "/v", super.name, "/t",
                "REG_SZ", "/d", data, "/f"
            )
        )
    }

    override fun uninstall() {
        Runtime.getRuntime().exec(
            arrayOf(
                "reg", "delete", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\",
                "/v", super.name, "/f",
            )
        )
    }
}
