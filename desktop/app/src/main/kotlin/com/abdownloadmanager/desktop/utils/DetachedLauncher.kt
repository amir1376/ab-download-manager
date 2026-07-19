package com.abdownloadmanager.desktop.utils

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop
import ir.amirab.util.platform.isWindows
import ir.amirab.util.withoutJPackageEnvVariable
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.text.iterator

/**
 * Launch executables and detach them from the current process
 */
object DetachedLauncher {

    fun launch(
        executable: File,
        vararg args: String
    ) {
        val absExecutable = executable.absoluteFile
        try {
            when (Platform.asDesktop()) {
                Platform.Desktop.Windows -> execViaWmi(absExecutable, args.toList())
                Platform.Desktop.Linux,
                Platform.Desktop.MacOS -> execViaSetsid(absExecutable, args.toList())
            }
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to detach-launch '${absExecutable.path}' with args ${args.toList()}",
                e
            )
        }
    }

    // Windows: native messaging hosts run inside the browser's Job Object.
    // A normal child process (even ProcessBuilder) inherits that Job and
    // dies when the browser/port disconnects. WMI's Win32_Process.Create
    // spawns via the WMI provider host (WmiPrvSE.exe), which is NOT part
    // of the browser's Job, so the child survives independently.
    private fun execViaWmi(exePath: File, args: List<String>) {
        val fullCommand = buildString {
            append(exePath.path.winQuoteArg())
            for (a in args) {
                append(' ')
                append(a.winQuoteArg())
            }
        }

        val escapedCommand = fullCommand.psSingleQuoteEscape()
        val escapedDir = exePath.parentFile?.path?.psSingleQuoteEscape()
        val currentDirArg = if (escapedDir != null) "; CurrentDirectory = '$escapedDir'" else ""

        val wmiScript = $$"""
            $p = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{ CommandLine = '$$escapedCommand'$$currentDirArg }
            if ($p.ReturnValue -ne 0) { exit $p.ReturnValue }
        """.trimIndent()

        val process = ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", wmiScript)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            // this is not necessary here but new versions of jpackage might need this to be added
            .withoutJPackageEnvVariable()
            .start()
        val finished = process.waitFor(10, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw IOException("powershell did not respond within timeout while launching '${exePath.path}'")
        }
        val exitCode = process.exitValue()
        if (exitCode != 0) {
            throw IOException("Win32_Process.Create failed for '${exePath.path}' with WMI return code $exitCode")
        }
    }

    private fun String.winQuoteArg(): String {
        if (isNotEmpty() && none { it == ' ' || it == '\t' || it == '"' }) return this
        val sb = StringBuilder("\"")
        var backslashes = 0
        for (c in this) {
            when (c) {
                '\\' -> {
                    backslashes++; sb.append(c)
                }

                '"' -> {
                    sb.append("\\".repeat(backslashes + 1)); sb.append(c); backslashes = 0
                }

                else -> {
                    backslashes = 0; sb.append(c)
                }
            }
        }
        sb.append("\\".repeat(backslashes)).append("\"")
        return sb.toString()
    }

    private fun String.psSingleQuoteEscape(): String = replace("'", "''")

    // Linux/macOS: browsers sandbox native hosts with a process group /
    // session too. setsid detaches into a brand-new session so the child
    // isn't in the browser's session and survives it exiting/killing.
    // No shell involved -> no quoting needed, argv passed directly.
    private fun execViaSetsid(exePath: File, args: List<String>) {
        val command = buildList {
            add("setsid")
            add(exePath.path)
            addAll(args)
        }
        ProcessBuilder(command)
            .directory(exePath.parentFile)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectInput(redirectToNull())
            .withoutJPackageEnvVariable()
            .start()
    }

    private fun redirectToNull(): ProcessBuilder.Redirect? {
        val nullPath = if (Platform.isWindows()) "NUL" else "/dev/null"
        return ProcessBuilder.Redirect.from(File(nullPath))
    }
}
