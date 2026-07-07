package com.abdownloadmanager.desktop.utils.singleInstance

import com.abdownloadmanager.desktop.utils.AppInfo
import okio.Path

class SingleInstanceManager private constructor(baseFolder: Path) {
    private val locker by lazy {
        SingleAppInstanceLocker(baseFolder / "app.lock")
    }
    private val server by lazy {
        SingleInstanceServer(baseFolder / "app.port")
    }

    fun singleInstanceService() = server.singleInstanceService()
    fun appIPCService() = server.appIPCService()

    @Throws(AnotherInstanceIsRunning::class)
    fun lockInstance() {
        locker.tryLockInstance()

        // we are alone so we create the server
        server.start()
    }

    companion object {
        private val instance by lazy {
            SingleInstanceManager(AppInfo.definedPaths.configDir)
        }

        fun get(): SingleInstanceManager = instance
    }
}
