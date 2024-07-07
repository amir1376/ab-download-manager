package ir.amirab.util.startup

import ir.amirab.util.platform.Platform

object Startup {
    /**
     * Add file to startup
     * @param name Name of key/file
     * @param path Path to file
     * @param jar If file should be executed by the JVM
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getStartUpManagerForDesktop(name: String, path: String?, jar: Boolean = false): AbstractStartupManager {
        if (path==null){
            //there is no installation path provided so we use no-op
            return noImplStartUpManager()
        }
        val os = Platform.getCurrentPlatform()
        val startup=when (os) {
            Platform.Desktop.Linux -> {
                if (Utils.isHeadless) {
                    HeadlessStartup(name, path, jar)
                } else {
                    UnixXDGStartup(name, path, jar)
                }
            }
            Platform.Desktop.MacOS -> MacOSStartup(name, path, jar)
            Platform.Desktop.Windows -> WindowsStartup(name, path, jar)
            Platform.Android -> error("this code should not be called in android")
        }
        return startup
    }
    private fun noImplStartUpManager(): HeadlessStartup {
        return HeadlessStartup("","",false)
    }
}
