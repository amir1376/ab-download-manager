package ir.amirab.util.startup

import ir.amirab.util.platform.Platform

object Startup {
    /**
     * Add file to startup
     * @param name Name of key/file
     * @param path Path to file
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getStartUpManagerForDesktop(
        name: String,
        path: String?,
        args: List<String>,
    ): AbstractStartupManager {
        if (path==null){
            //there is no installation path provided so we use no-op
            return noImplStartUpManager()
        }
        val os = Platform.getCurrentPlatform()
        val startup=when (os) {
            Platform.Desktop.Linux -> {
                if (Utils.isHeadless) {
                    HeadlessStartup(name, path, args)
                } else {
                    UnixXDGStartup(name, path, args)
                }
            }

            Platform.Desktop.MacOS -> MacOSStartup(name, path, args)
            Platform.Desktop.Windows -> WindowsStartup(name, path, args)
            Platform.Android -> error("this code should not be called in android")
        }
        return startup
    }
    private fun noImplStartUpManager(): HeadlessStartup {
        return HeadlessStartup("", "", emptyList())
    }
}
