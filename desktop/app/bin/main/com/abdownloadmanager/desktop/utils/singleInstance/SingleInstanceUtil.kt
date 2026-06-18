package com.abdownloadmanager.desktop.utils.singleInstance

import okio.Path

class SingleInstanceUtil(baseFolder: Path) {
    private val locker by lazy {
        SingleAppInstanceLocker(baseFolder / "app.lock")
    }
    private val server by lazy {
        SingleInstanceServer(baseFolder / "app.port")
    }

    fun <T:Any>sendToInstance(msg: Command<T>): CommandResult<T> {
        return server.sendMessage(msg).also {
//            println("server respond with ${it}")
        }
    }

    @Throws(AnotherInstanceIsRunning::class)
    fun lockInstance(
        createMessageHandler: () -> SingleInstanceServerHandler,
    ) {
        locker.tryLockInstance()

        // we are alone so we create the server
        server.start(createMessageHandler())
    }
}