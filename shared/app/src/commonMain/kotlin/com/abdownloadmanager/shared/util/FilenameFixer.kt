package com.abdownloadmanager.shared.util

import ir.amirab.util.ifThen
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isWindows

/**
 * This utility class removes characters that are not supported by the current OS.
 *
 * If additional modifications are required, it may be better to create a separate class for each platform.
 */
object FilenameFixer {
    private const val DEFAULT_REPLACEMENT_CHAR = "_"
    private val illegalChars by lazy {
        when (Platform.getCurrentPlatform()) {
            Platform.Desktop.Windows -> setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')
            Platform.Desktop.MacOS -> setOf(':')
            Platform.Desktop.Linux,
            Platform.Android,
                -> setOf('/')
        }
    }

    fun fix(name: String): String {
        return buildString {
            name.forEach { char ->
                append(
                    if (char in illegalChars) {
                        DEFAULT_REPLACEMENT_CHAR
                    } else {
                        char
                    }
                )
            }
        }
            .ifThen(Platform.isWindows()) {
                trimEnd(' ', '.')
            }
    }
}
