package com.abdownloadmanager.desktop.utils.singleInstance

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response

interface SingleInstanceServerHandler : HttpHandler {
    val handler: HttpHandler
    override fun invoke(request: Request): Response {
        return handler(request)
    }
}
