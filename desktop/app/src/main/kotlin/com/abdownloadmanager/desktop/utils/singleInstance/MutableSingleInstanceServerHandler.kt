package com.abdownloadmanager.desktop.utils.singleInstance

import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

class MutableSingleInstanceServerHandler : SingleInstanceServerHandler {
    private val handlers = mutableListOf<Pair<Command<Any>, () -> Any>>()

    private var _routes = createRoutes()

    private fun createRoutes(): RoutingHttpHandler {
        return routes(
            handlers.map { (cmd, handler) ->
                cmd bindSafe {
                    handler()
                }
            }
        )
    }

    override val definedRoutes: RoutingHttpHandler
        get() = _routes

    fun updateRoutes(){
        _routes=createRoutes()
    }

    fun <T : Any> add(command: Command<T>, handle: () -> T) {
        synchronized(this){
            val element = (command to handle) as Pair<Command<Any>, () -> Any>
            handlers.add(element)
            updateRoutes()
        }
    }

    fun <T : Any> remove(command: Command<T>) {
        synchronized(this){
            handlers.removeIf {
                it.first == command
            }
            updateRoutes()
        }
    }
}