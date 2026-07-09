package com.abdownloadmanager.cli

import com.abdownloadmanager.cli.utils.DefaultDesktopBinaryProvider
import com.abdownloadmanager.integration.client.DesktopLauncher

/**
 * Shared CLI context holding the [DesktopLauncher] instance used by all commands.
 *
 * Uses [DefaultDesktopBinaryProvider] to locate the desktop app binary.
 */
object CliContext {
    val desktopLauncher = DesktopLauncher(binaryProvider = DefaultDesktopBinaryProvider)
}