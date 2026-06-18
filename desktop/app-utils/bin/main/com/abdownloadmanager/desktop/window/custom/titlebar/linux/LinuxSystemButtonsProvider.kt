package com.abdownloadmanager.desktop.window.custom.titlebar.linux

import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonPositionProvider
import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonType
import com.abdownloadmanager.desktop.window.custom.titlebar.SystemButtonsPosition
import java.io.File

object LinuxSystemButtonsProvider : SystemButtonPositionProvider {
    override fun getPositions(): SystemButtonsPosition? {
        return runCatching {
            getSystemButtonLayout()
        }.getOrNull()
    }
}


private fun getSystemButtonLayout(): SystemButtonsPosition? {
    val desktop = System.getenv("XDG_CURRENT_DESKTOP")?.lowercase()
        ?: System.getenv("DESKTOP_SESSION")?.lowercase()
        ?: "unknown"

    return when {
        "gnome" in desktop -> parseColonLayout(
            runCommand(
                "gsettings",
                "get",
                "org.gnome.desktop.wm.preferences",
                "button-layout"
            )
        )

        "mate" in desktop -> parseColonLayout(
            runCommand(
                "gsettings",
                "get",
                "org.mate.Marco.general",
                "button-layout"
            )
        )

        "xfce" in desktop -> parsePipeLayout(
            runCommand(
                "xfconf-query",
                "-c",
                "xfwm4",
                "-p",
                "/general/button_layout"
            )
        )

        "kde" in desktop -> parseKDELayout(File(System.getProperty("user.home"), ".config/kwinrc"))
        else -> null
    }?.takeIf { it.buttons.isNotEmpty() }
}

// For GNOME / MATE → colon-separated layout
private fun parseColonLayout(layoutRaw: String?): SystemButtonsPosition? {
    val layout = layoutRaw?.removeSurrounding("'", "'")?.trim() ?: return null
    val (left, right) = layout.split(":", limit = 2).map { it.trim() + "," }.let {
        parseButtons(it.getOrNull(0).orEmpty()) to parseButtons(it.getOrNull(1).orEmpty())
    }

    return when {
        left.isNotEmpty() -> SystemButtonsPosition(left, isLeft = true)
        right.isNotEmpty() -> SystemButtonsPosition(right, isLeft = false)
        else -> null
    }
}

// For XFCE → pipe-separated layout
private fun parsePipeLayout(layout: String?): SystemButtonsPosition? {
    val parts = layout?.split("|")?.map { it.trim() } ?: return null
    return if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
        SystemButtonsPosition(parseButtons(parts[0]), isLeft = true)
    } else if (parts.size > 1 && parts[1].isNotEmpty()) {
        SystemButtonsPosition(parseButtons(parts[1]), isLeft = false)
    } else {
        null
    }
}

// For KDE → parse kwinrc file
private fun parseKDELayout(file: File): SystemButtonsPosition? {
    if (!file.exists()) return null
    val lines = file.readLines()
    val left = lines.find { it.startsWith("ButtonsOnLeft=") }?.substringAfter("=")?.trim()
    val right = lines.find { it.startsWith("ButtonsOnRight=") }?.substringAfter("=")?.trim()

    return when {
        !left.isNullOrEmpty() -> SystemButtonsPosition(parseButtons(left), isLeft = true)
        !right.isNullOrEmpty() -> SystemButtonsPosition(parseButtons(right), isLeft = false)
        else -> null
    }
}

private fun parseButtons(raw: String): List<SystemButtonType> {
    return raw.split(",", "|", " ")
        .mapNotNull {
            when (it.lowercase()) {
                "close" -> SystemButtonType.Close
                "maximize" -> SystemButtonType.Maximize
                "minimize" -> SystemButtonType.Minimize
                else -> null
            }
        }
}

private fun runCommand(vararg args: String): String? {
    return try {
        val process = ProcessBuilder(*args).redirectErrorStream(true).start()
        process.inputStream.reader().use { it.readText().trim() }
    } catch (_: Exception) {
        null
    }
}

