package com.abdownloadmanager.desktop.utils.singleInstance

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.orElse
import org.http4k.routing.routes

class MutableSingleInstanceServerHandler : SingleInstanceServerHandler {
    private val handlers = mutableListOf<Pair<Command<Any>, () -> Any>>()

    private var mainHandler = createRoutes()

    private fun createRoutes(): HttpHandler {
        val handlersArray = handlers.map { (cmd, handler) ->
            cmd bindSafe {
                handler()
            }
        }.toTypedArray()
        return routes(
            *handlersArray,
            // add this since empty routes will crash
            orElse bind {
                Response(Status.NOT_FOUND)
            }
        )
    }

    override val handler: HttpHandler
        get() = mainHandler

    fun updateRoutes() {
        mainHandler = createRoutes()
    }

    fun <T : Any> add(command: Command<T>, handle: () -> T) {
        synchronized(this) {
            val element = (command to handle) as Pair<Command<Any>, () -> Any>
            handlers.add(element)
            updateRoutes()
        }
    }

    fun <T : Any> remove(command: Command<T>) {
        synchronized(this) {
            handlers.removeIf {
                it.first == command
            }
            updateRoutes()
        }
    }
}
