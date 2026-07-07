package com.abdownloadmanager.desktop.utils.singleInstance

import okio.Path

class SingleInstanceUtil(baseFolder: Path) {
    private val locker by lazy {
        SingleAppInstanceLocker(baseFolder / "app.lock")
    }
    private val server by lazy {
        SingleInstanceServer(baseFolder / "app.port")
    }

    fun singleInstanceService() = server.singleInstanceService()

    @Throws(AnotherInstanceIsRunning::class)
    fun lockInstance() {
        locker.tryLockInstance()

        // we are alone so we create the server
        server.start()
    }
}
