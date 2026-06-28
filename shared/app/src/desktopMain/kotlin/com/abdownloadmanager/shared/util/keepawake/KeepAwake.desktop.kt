package com.abdownloadmanager.shared.util.keepawake

import com.xeton.util.platform.Platform
import com.xeton.util.platform.asDesktop

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
