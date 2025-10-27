package ir.amirab.util.startup

import ir.amirab.util.platform.Platform

actual object Startup {
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
        packageName: String,
    ): AbstractDesktopStartupManager {
        if (path==null){
            //there is no installation path provided so we use no-op
            return noImplStartUpManager()
        }
        val os = Platform.getCurrentPlatform()
        val startup=when (os) {
            Platform.Desktop.Linux -> {
                if (Utils.isHeadless) {
                    HeadlessStartupDesktop(name, path, args)
                } else {
                    UnixXDGStartupDesktop(name, path, args, packageName)
                }
            }

            Platform.Desktop.MacOS -> MacOSStartupDesktop(name, path, args)
            Platform.Desktop.Windows -> WindowsStartupDesktop(name, path, args)
            Platform.Android -> error("this code should not be called in android")
        }
        return startup
    }

    private fun noImplStartUpManager(): HeadlessStartupDesktop {
        return HeadlessStartupDesktop("", "", emptyList())
    }
}
