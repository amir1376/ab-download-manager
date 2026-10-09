package com.abdownloadmanager.shared.util.systemusage

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.Structure
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.UInt64

@DBusInterfaceName("org.gnome.Mutter.IdleMonitor")
internal interface GnomeIdleMonitor : DBusInterface {
    fun GetIdletime(): UInt64
}

@DBusInterfaceName("org.freedesktop.ScreenSaver")
internal interface FreedesktopScreenSaver : DBusInterface {
    fun GetSessionIdleTime(): UInt32
}

internal interface X11Library : Library {
    fun XOpenDisplay(displayName: String?): Pointer?
    fun XCloseDisplay(display: Pointer?): Int
    fun XDefaultRootWindow(display: Pointer?): Pointer?

    companion object {
        val INSTANCE: X11Library? by lazy {
            runCatching { Native.load("X11", X11Library::class.java) }.getOrNull()
        }
    }
}

internal class XScreenSaverInfo : Structure() {
    @JvmField
    var window: Pointer? = null
    @JvmField
    var state: Int = 0
    @JvmField
    var kind: Int = 0
    @JvmField
    var til_or_since: NativeLong = NativeLong()
    @JvmField
    var idle: NativeLong = NativeLong()
    @JvmField
    var eventMask: NativeLong = NativeLong()

    override fun getFieldOrder(): List<String> = listOf("window", "state", "kind", "til_or_since", "idle", "eventMask")
}

internal interface XssLibrary : Library {
    fun XScreenSaverQueryInfo(display: Pointer?, drawable: Pointer?, saverInfo: XScreenSaverInfo): Int

    companion object {
        val INSTANCE: XssLibrary? by lazy {
            runCatching { Native.load("Xss", XssLibrary::class.java) }.getOrNull()
        }
    }
}

/**
 * Linux implementation of [DesktopIdleTimeProvider].
 *
 * Implements a multi-tier fallback mechanism:
 * 1. **GNOME Mutter IdleMonitor** via session D-Bus (modern GNOME Wayland & X11 sessions).
 * 2. **KDE / Freedesktop ScreenSaver** via session D-Bus (KDE Plasma sessions).
 * 3. **X11 XScreenSaver extension** via JNA (`libXss` / `libX11`) for traditional X11 desktop environments.
 */
internal class LinuxIdleTimeProvider : DesktopIdleTimeProvider, AutoCloseable {
    private var dbusConnection: DBusConnection? = null
    private var dbusFailed = false

    private var x11Display: Pointer? = null
    private var x11Failed = false
    private val xssInfo = XScreenSaverInfo()

    @Synchronized
    override fun getIdleTimeMillis(): Long? {
        // 1. Try D-Bus services (GNOME / KDE)
        if (!dbusFailed) {
            val dbusResult = getIdleTimeViaDbus()
            if (dbusResult != null) {
                return dbusResult
            }
        }

        // 2. Try X11 / XScreenSaver
        if (!x11Failed) {
            val x11Result = getIdleTimeViaX11()
            if (x11Result != null) {
                return x11Result
            }
        }

        return null
    }

    private fun getIdleTimeViaDbus(): Long? {
        return runCatching<Long?> {
            var conn = dbusConnection
            if (conn == null || !conn.isConnected) {
                conn = DBusConnectionBuilder.forSessionBus().build()
                dbusConnection = conn
            }

            // 1a. Try GNOME Mutter IdleMonitor
            val mutterTime = runCatching {
                val mutter = conn.getRemoteObject(
                    "org.gnome.Mutter.IdleMonitor",
                    "/org/gnome/Mutter/IdleMonitor/Core",
                    GnomeIdleMonitor::class.java,
                )
                mutter.GetIdletime().toLong()
            }.getOrNull()

            if (mutterTime != null) {
                return@runCatching mutterTime
            }

            // 1b. Try Freedesktop / KDE ScreenSaver
            val screensaverTime = runCatching {
                val screensaver = conn.getRemoteObject(
                    "org.freedesktop.ScreenSaver",
                    "/org/freedesktop/ScreenSaver",
                    FreedesktopScreenSaver::class.java,
                )
                screensaver.GetSessionIdleTime().toLong()
            }.getOrNull()

            screensaverTime
        }.getOrElse {
            // D-Bus session not available or failed
            dbusFailed = true
            null
        }
    }

    private fun getIdleTimeViaX11(): Long? {
        return runCatching {
            val x11 = X11Library.INSTANCE ?: run {
                x11Failed = true
                return null
            }
            val xss = XssLibrary.INSTANCE ?: run {
                x11Failed = true
                return null
            }

            var display = x11Display
            if (display == null) {
                display = x11.XOpenDisplay(null)
                if (display == null) {
                    x11Failed = true
                    return null
                }
                x11Display = display
            }

            val rootWindow = x11.XDefaultRootWindow(display) ?: return null
            val status = xss.XScreenSaverQueryInfo(display, rootWindow, xssInfo)
            if (status != 0) {
                xssInfo.idle.toLong()
            } else {
                null
            }
        }.getOrElse {
            x11Failed = true
            null
        }
    }

    @Synchronized
    override fun close() {
        runCatching { dbusConnection?.close() }
        dbusConnection = null

        val display = x11Display
        if (display != null) {
            runCatching { X11Library.INSTANCE?.XCloseDisplay(display) }
            x11Display = null
        }
    }
}
