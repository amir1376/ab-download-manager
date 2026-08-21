package ir.amirab.util.desktop.screen

import androidx.compose.ui.unit.*
import com.abdownloadmanager.shared.util.ui.theme.DEFAULT_UI_SCALE
import java.awt.GraphicsEnvironment
import java.io.File
import java.util.concurrent.TimeUnit

fun getGlobalScale(): Float {
    return cachedGlobalScale
}

/**
 * Cached result of scale detection. Computed once at first access since the display
 * scale doesn't change while the app is running (a restart is typically needed).
 */
private val cachedGlobalScale: Float by lazy {
    // On Linux Wayland, AWT often reports scale=1.0 even with fractional scaling.
    // This is a known JVM limitation — Java's AWT toolkit doesn't support the
    // Wayland fractional scaling protocols. We detect the real scale from the OS
    // and use it when AWT fails.
    val waylandScale = detectLinuxWaylandScale()
    if (waylandScale != null) return@lazy waylandScale

    val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val defaultScreenDevice = graphicsEnvironment.defaultScreenDevice
    val defaultTransform = defaultScreenDevice.defaultConfiguration.defaultTransform
    defaultTransform.scaleX.toFloat()
}

/**
 * Detects the display scale factor on Linux Wayland sessions where Java AWT
 * fails to report fractional scaling.
 *
 * This is a workaround for a JVM/Skiko limitation: Java's AWT toolkit does not
 * implement the wp_fractional_scale_v1 Wayland protocol, so it always reports
 * scale=1.0 and DPI=96 on Wayland, regardless of the compositor's actual setting.
 *
 * Detection methods (in order):
 * 1. Environment variables (QT_SCALE_FACTOR, GDK_SCALE) — explicit user override
 * 2. Xft.dpi from X resources via xrdb — most portable, works across DEs via XWayland
 * 3. KDE kwinrc [Xwayland] Scale — KDE Plasma specific, no subprocess needed
 * 4. Hyprland via hyprctl — for Hyprland WM
 * 5. Sway via swaymsg — for Sway WM
 *
 * Returns null if:
 * - Not on Linux or not on a Wayland session
 * - AWT already reports scale > 1.0 (no workaround needed, avoids double-scaling)
 * - All detection methods fail
 */
private fun detectLinuxWaylandScale(): Float? {
    if (System.getProperty("os.name")?.startsWith("Linux") != true) return null
    if (System.getenv("WAYLAND_DISPLAY").isNullOrEmpty()) return null

    // Guard: if AWT already reports scaling, trust it — avoids double-scaling
    // if the JVM/Skiko issue is fixed in a future version.
    try {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val awtScale = ge.defaultScreenDevice.defaultConfiguration.defaultTransform.scaleX
        if (awtScale > 1.0) return null
    } catch (_: Exception) {}

    // Method 1: Explicit environment variable override
    // Users or distros may set these to control scaling for toolkit apps.
    tryParseScaleFromEnv("QT_SCALE_FACTOR")?.let { return it }
    tryParseScaleFromEnv("GDK_SCALE")?.let { return it }

    // Method 2: Xft.dpi from X resources (most portable)
    // Wayland compositors set this for XWayland clients. KDE, GNOME, and most
    // compositors set Xft.dpi = 96 * scale_factor (e.g. 163 for 1.7x).
    tryDetectFromXftDpi()?.let { return it }

    // Method 3: KDE Plasma — read kwinrc directly (no subprocess)
    // The [Xwayland] section contains the scale applied to XWayland clients.
    tryDetectFromKwinrc()?.let { return it }

    // Method 4: Hyprland — query via hyprctl
    tryDetectFromHyprland()?.let { return it }

    // Method 5: Sway — query via swaymsg
    tryDetectFromSway()?.let { return it }

    return null
}

private fun tryParseScaleFromEnv(name: String): Float? {
    return try {
        val value = System.getenv(name) ?: return null
        val scale = value.toFloat()
        if (scale > 1f) scale else null
    } catch (_: Exception) {
        null
    }
}

private fun tryDetectFromXftDpi(): Float? {
    return try {
        val process = ProcessBuilder("xrdb", "-query")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return null
        }
        val match = Regex("""Xft\.dpi:\s*(\d+)""").find(output) ?: return null
        val dpi = match.groupValues[1].toFloat()
        if (dpi > 96f) dpi / 96f else null
    } catch (_: Exception) {
        null
    }
}

private fun tryDetectFromKwinrc(): Float? {
    return try {
        val kwinrc = File(System.getProperty("user.home"), ".config/kwinrc")
        if (!kwinrc.exists()) return null
        val content = kwinrc.readText()
        // Look for Scale= in the [Xwayland] section specifically
        val xwaylandSection = Regex("""\[Xwayland]\s*\n((?:[^\[].*)*)""").find(content)
        if (xwaylandSection != null) {
            val scaleMatch = Regex("""Scale=(\d+\.?\d*)""").find(xwaylandSection.groupValues[1])
            if (scaleMatch != null) {
                val scale = scaleMatch.groupValues[1].toFloat()
                if (scale > 1f) return scale
            }
        }
        // Fallback: any Scale= in the file (older kwinrc formats)
        val anyScale = Regex("""Scale=(\d+\.?\d*)""").find(content)
        if (anyScale != null) {
            val scale = anyScale.groupValues[1].toFloat()
            if (scale > 1f) return scale
        }
        null
    } catch (_: Exception) {
        null
    }
}

private fun tryDetectFromHyprland(): Float? {
    return try {
        if (System.getenv("HYPRLAND_INSTANCE_SIGNATURE").isNullOrEmpty()) return null
        val process = ProcessBuilder("hyprctl", "monitors", "-j")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return null
        }
        // Parse JSON: [{"scale":1.7, "focused":true, ...}, ...]
        // Use focused monitor's scale, or first monitor as fallback
        val scales = Regex(""""scale"\s*:\s*(\d+\.?\d*)""").findAll(output)
            .map { it.groupValues[1].toFloat() }
            .toList()
        val focusedScale = if (output.contains("\"focused\":true") || output.contains("\"focused\": true")) {
            // Find the scale that appears in the same monitor block as "focused":true
            val focusedBlock = Regex("""\{[^}]*"focused"\s*:\s*true[^}]*\}""").find(output)
            if (focusedBlock != null) {
                Regex(""""scale"\s*:\s*(\d+\.?\d*)""").find(focusedBlock.value)
                    ?.groupValues?.get(1)?.toFloat()
            } else null
        } else null
        val scale = focusedScale ?: scales.firstOrNull()
        if (scale != null && scale > 1f) scale else null
    } catch (_: Exception) {
        null
    }
}

private fun tryDetectFromSway(): Float? {
    return try {
        if (System.getenv("SWAYSOCK").isNullOrEmpty()) return null
        val process = ProcessBuilder("swaymsg", "-t", "get_outputs")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return null
        }
        // Parse JSON: [{"scale":1.5, "focused":true, ...}, ...]
        val focusedBlock = Regex("""\{[^}]*"focused"\s*:\s*true[^}]*\}""").find(output)
        if (focusedBlock != null) {
            val scaleMatch = Regex(""""scale"\s*:\s*(\d+\.?\d*)""").find(focusedBlock.value)
            val scale = scaleMatch?.groupValues?.get(1)?.toFloat()
            if (scale != null && scale > 1f) return scale
        }
        // Fallback to first monitor
        val firstScale = Regex(""""scale"\s*:\s*(\d+\.?\d*)""").find(output)
            ?.groupValues?.get(1)?.toFloat()
        if (firstScale != null && firstScale > 1f) firstScale else null
    } catch (_: Exception) {
        null
    }
}

fun Int.applyUiScale(
    userUiScale: Float,
): Int {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this * userUiScale).toInt()
}

fun Float.applyUiScale(
    userUiScale: Float,
): Float {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this * userUiScale)
}

fun Int.unApplyUiScale(
    userUiScale: Float,
): Int {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this / userUiScale).toInt()
}

fun Float.unApplyUiScale(
    userUiScale: Float,
): Float {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    return (this / userUiScale)
}

fun DpSize.applyUiScale(
    userUiScale: Float,
): DpSize {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
    )
}

fun DpSize.unApplyUiScale(
    userUiScale: Float,
): DpSize {
    if (userUiScale == DEFAULT_UI_SCALE) return this
    if (this == DpSize.Unspecified) return this
    return DpSize(
        width = width.let {
            if (isSpecified) it.value.toInt().unApplyUiScale(userUiScale).dp
            else it
        },
        height = height.let {
            if (isSpecified) it.value.toInt().applyUiScale(userUiScale).dp
            else it
        },
    )
}
