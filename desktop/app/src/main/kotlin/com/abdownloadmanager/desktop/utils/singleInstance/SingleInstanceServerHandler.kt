package com.abdownloadmanager.desktop.utils.singleInstance

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RoutingHttpHandler

interface SingleInstanceServerHandler:HttpHandler {
    val definedRoutes:RoutingHttpHandler
    override fun invoke(request: Request): Response {
        return definedRoutes(request)
    }
}