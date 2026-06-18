package com.abdownloadmanager.desktop.window

import com.abdownloadmanager.shared.util.BrowserType
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop
import ir.amirab.util.toUpUntil
import java.io.File


abstract class Browser {
    abstract fun getPossibleExecutablePaths(): List<File>

    fun isInstalled(): Boolean {
        return getExecutablePath() != null
    }

    open fun openLink(url: String): Boolean {
        val executablePath = getExecutablePath()
        if (executablePath == null) {
            return false
        }
        val cmd = when (Platform.asDesktop()) {
            Platform.Desktop.Linux -> arrayOf(executablePath.path, url)
            Platform.Desktop.Windows -> arrayOf(executablePath.path, url)
            Platform.Desktop.MacOS -> {
                val appFolder = executablePath.toUpUntil {
                    // in macOS app folders are like /Applications/App Name.app/
                    it.name.endsWith(".app")
                }?.path
                if (appFolder == null) {
                    return false
                }
                arrayOf("open", "-a", appFolder, url)
            }
        }
        Runtime.getRuntime().exec(cmd)
        return true
    }

    fun getExecutablePath(): File? {
        return getPossibleExecutablePaths().firstOrNull { it.exists() }
    }

    companion object {
        fun getBrowserByType(type: BrowserType): Browser? {
            return when (type) {
                BrowserType.Chrome -> ChromeBrowser
                BrowserType.Firefox -> FirefoxBrowser
                BrowserType.Edge -> EdgeBrowser
                BrowserType.Opera -> OperaBrowser
            }
        }
    }
}

object FirefoxBrowser : Browser() {
    override fun getPossibleExecutablePaths(): List<File> {
        return when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> {
                val firefoxExe = "Mozilla Firefox\\firefox.exe"
                buildList {
                    listOf(
                        "ProgramW6432",
                        "ProgramFiles",
                        "ProgramFiles(x86)",
                        "LOCALAPPDATA",
                    ).forEach {
                        System.getenv(it)?.let { path ->
                            add(File(path, firefoxExe))
                        }
                    }
                }
            }

            Platform.Desktop.MacOS -> {
                listOf(
                    File("/Applications/Firefox.app"),
                )
            }

            Platform.Desktop.Linux -> {
                listOf(
                    File("/usr/bin/firefox"),
                    File("/usr/bin/firefox-bin"),
                )
            }
        }
    }
}

object ChromeBrowser : Browser() {
    override fun getPossibleExecutablePaths(): List<File> {
        return when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> {
                val chromeExe = "Google\\Chrome\\Application\\chrome.exe"
                buildList {
                    listOf(
                        "ProgramW6432",
                        "PROGRAMFILES",
                        "PROGRAMFILES(X86)",
                        "LOCALAPPDATA",
                    ).forEach {
                        System.getenv(it)?.let { path ->
                            add(File(path, chromeExe))
                        }
                    }
                }
            }

            Platform.Desktop.MacOS -> {
                listOf(
                    File("/Applications/Google Chrome.app")
                )
            }

            Platform.Desktop.Linux -> {
                listOf(
                    File("/usr/bin/google-chrome"),
                    File("/usr/bin/chromium-browser"),
                    File("/usr/bin/chromium")
                )
            }
        }
    }
}

object EdgeBrowser : Browser() {
    override fun getPossibleExecutablePaths(): List<File> {
        return when (Platform.asDesktop()) {
            Platform.Desktop.Linux -> {
                listOf(
                    File("/usr/bin/microsoft-edge"),
                )
            }

            Platform.Desktop.MacOS -> {
                listOf(
                    File("/Applications/Microsoft Edge.app"),
                )
            }

            Platform.Desktop.Windows -> {
                val child = "Microsoft\\Edge\\Application\\msedge.exe"
                buildList {
                    listOf(
                        "ProgramW6432",
                        "PROGRAMFILES",
                        "PROGRAMFILES(X86)",
                        "LOCALAPPDATA",
                    ).forEach {
                        System.getenv(it)?.let { path ->
                            add(File(path, child))
                        }
                    }
                }
            }
        }
    }
}

object OperaBrowser : Browser() {
    override fun getPossibleExecutablePaths(): List<File> {
        return when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> {
                val relativeExe = "Opera\\launcher.exe"
                buildList {
                    System.getenv("LOCALAPPDATA")?.let { path ->
                        add(File(path, "Programs\\$relativeExe"))
                    }
                    listOf(
                        "ProgramW6432",
                        "PROGRAMFILES",
                        "PROGRAMFILES(X86)",
                    ).forEach {
                        System.getenv(it)?.let { path ->
                            add(File(path, relativeExe))
                        }
                    }
                }
            }

            Platform.Desktop.MacOS -> {
                listOf(
                    File("/Applications/Opera.app"),
                )
            }

            Platform.Desktop.Linux -> {
                listOf(
                    File("/usr/bin/opera"),
                )
            }

        }

    }

}

