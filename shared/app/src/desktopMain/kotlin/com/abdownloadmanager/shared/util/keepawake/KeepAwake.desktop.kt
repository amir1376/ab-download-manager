package com.abdownloadmanager.shared.util.keepawake

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

private val instance by lazy {
    when (Platform.asDesktop()) {
        Platform.Desktop.Windows -> WindowsKeepAwake()
        Platform.Desktop.MacOS -> MacKeepAwake()
        Platform.Desktop.Linux -> KeepAwake.NoOpKeepAwake()
    }
}

actual fun platformKeepAwake(): KeepAwake {
    return instance
}
