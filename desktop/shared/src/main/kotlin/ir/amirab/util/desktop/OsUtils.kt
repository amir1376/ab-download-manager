package ir.amirab.util.desktop

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.FrameWindowScope
import java.awt.ComponentOrientation
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.util.*

val LocalFrameWindowScope = compositionLocalOf<FrameWindowScope> {
    error("LocalFrameWindowScope not provided yet")
}

val GlobalDensity
    get() = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice
        .defaultConfiguration
        .density
val GraphicsConfiguration.density: Density
    get() = Density(
        defaultTransform.scaleX.toFloat(),
        fontScale = 1f
    )

val GlobalLayoutDirection get() = Locale.getDefault().layoutDirection
val Locale.layoutDirection: LayoutDirection
    get() = ComponentOrientation.getOrientation(this).layoutDirection

val ComponentOrientation.layoutDirection: LayoutDirection
    get() = when {
        isLeftToRight -> LayoutDirection.Ltr
        isHorizontal -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }


val trayIconSize = when (DesktopPlatform.Current) {
    // https://doc.qt.io/qt-5/qtwidgets-desktop-systray-example.html (search 22x22)
    DesktopPlatform.Linux -> Size(22f, 22f)
    // https://doc.qt.io/qt-5/qtwidgets-desktop-systray-example.html (search 16x16)
    DesktopPlatform.Windows -> Size(16f, 16f)
    // https://medium.com/@acwrightdesign/creating-a-macos-menu-bar-application-using-swiftui-54572a5d5f87
    DesktopPlatform.MacOS -> Size(22f, 22f)
    DesktopPlatform.Unknown -> Size(32f, 32f)
}

enum class DesktopPlatform {
    Linux,
    Windows,
    MacOS,
    Unknown;

    companion object {
        /**
         * Identify OS on which the application is currently running.
         */
        val Current: DesktopPlatform by lazy {
            val name = System.getProperty("os.name")
            when {
                name?.startsWith("Linux") == true -> Linux
                name?.startsWith("Win") == true -> Windows
                name == "Mac OS X" -> MacOS
                else -> Unknown
            }
        }
    }
}
