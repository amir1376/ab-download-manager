package ir.amirab.util.platform

import ir.amirab.util.platform.Platform.Android
import ir.amirab.util.platform.Platform.Desktop

sealed class Platform(val name: String) {
    data object Android : Platform("Android")
    sealed class Desktop(name: String) : DesktopPlatform,
        Platform(name) {
        data object Windows : Desktop("Windows")
        data object Linux : Desktop("Linux")
        data object MacOS : Desktop("Mac")
    }
    override fun toString(): String {
        return name
    }

    companion object : PlatformFInder by JvmPlatformFinder() {
        fun fromString(platformName: String): Platform? {
            return when (platformName.lowercase()) {
                "windows" -> Desktop.Windows
                "linux" -> Desktop.Linux
                "mac" -> Desktop.Linux
                "android" -> Android
                else -> null
            }
        }
        fun fromExecutableFileExtension(fileExtension:String): Platform?{
            return when(fileExtension.lowercase()){
                "exe","msi"-> Desktop.Windows
                "deb","rpm"-> Desktop.Linux
                "dmg", "pkg" -> Desktop.MacOS
                "apk"-> Android
                else -> null
            }
        }
    }
}

interface PlatformFInder {
    fun getCurrentPlatform(): Platform
}

private class JvmPlatformFinder : PlatformFInder {
    override fun getCurrentPlatform(): Platform {
        val osString = System.getProperty("os.name").lowercase()
        return when {
            osString.contains("android") -> Android
            osString.contains("windows") -> Desktop.Windows
            osString.contains("linux") -> Desktop.Linux
            osString.contains("darwin") -> Desktop.MacOS
            else -> error("unknown platform")
        }
    }
}

sealed interface DesktopPlatform



