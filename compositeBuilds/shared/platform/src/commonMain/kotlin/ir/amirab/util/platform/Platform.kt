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
                "mac" -> Desktop.MacOS
                "android" -> Android
                else -> null
            }
        }

        fun fromExecutableFileExtension(fileExtension: String): Platform? {
            return when (fileExtension.lowercase()) {
                "exe", "msi" -> Desktop.Windows
                "deb", "rpm" -> Desktop.Linux
                "dmg", "pkg" -> Desktop.MacOS
                "apk" -> Android
                else -> null
            }
        }
    }
}

interface PlatformFInder {
    fun getCurrentPlatform(): Platform
}

private class JvmPlatformFinder : PlatformFInder {
    private val _platform by lazy {
        getCurrentPlatformFromJVMProperty()
    }

    private fun isAndroid(): Boolean {
        val vm = System.getProperty("java.vm.name")?.lowercase().orEmpty()
        val vendor = System.getProperty("java.vendor")?.lowercase().orEmpty()
        val isAndroid = "android" in vendor || "dalvik" in vm
        return isAndroid
    }

    private fun getCurrentPlatformFromJVMProperty(): Platform {
        val osString = System.getProperty("os.name").orEmpty().lowercase()
        if (isAndroid()) {
            return Android
        }
        return when {
            osString.contains("windows") -> Desktop.Windows
            osString.contains("linux") -> Desktop.Linux
            osString.contains("mac") || osString.contains("darwin") -> Desktop.MacOS
            else -> error("this platform is not detected: $osString")
        }
    }

    override fun getCurrentPlatform(): Platform {
        return _platform
    }
}

sealed interface DesktopPlatform


/**
 * use this only in desktop environments
 */
fun PlatformFInder.asDesktop(): Desktop {
    val platform = getCurrentPlatform()
    if (platform is Desktop) {
        return platform
    } else {
        error("Current platform is not a desktop platform")
    }
}

fun PlatformFInder.isWindows(): Boolean {
    return getCurrentPlatform() == Desktop.Windows
}

fun PlatformFInder.isMac(): Boolean {
    return getCurrentPlatform() == Desktop.MacOS
}

fun PlatformFInder.isLinux(): Boolean {
    return getCurrentPlatform() == Desktop.Linux
}

fun PlatformFInder.isAndroid(): Boolean {
    return getCurrentPlatform() == Android
}
