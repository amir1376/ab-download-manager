package com.abdownloadmanager.integration

typealias Header = Map<String, String>

data class MyRequest(
    val uri:String,
    val method:String,//: GET | POST
    val getBody:()->String?
)


sealed class MyResponse(
    val statusCode: Int,
    val headers: Header,
) {
    abstract fun getContent(): String

    class Json(val jsonData: String, headers: Header = emptyMap()) : MyResponse(statusCode = 200, headers = headers) {
        override fun getContent() = jsonData
    }

    class Text(
        val text: String,
        headers: Header = emptyMap(),
        statusCode: Int=200,
    ) : MyResponse(statusCode = statusCode, headers = headers) {
        override fun getContent() = text
    }

    class BadRequest(
        val errorText: String,
        headers: Header = emptyMap(),
        statusCode: Int=400,
    ) : MyResponse(
        statusCode = statusCode,
        headers = headers,
    ) {
        override fun getContent() = errorText
    }
}


typealias Handler = (MyRequest) -> MyResponse



class HandlerMap {
    private val handlers = mutableListOf<Pair<String, Handler>>()
    private fun add(uri: String, method: String, handler: Handler) {
        handlers.add(Pair(uri, handler))
    }
    fun get(uri: String,handler: Handler){
        add(uri,"GET",handler)
    }
    fun post(uri: String,handler: Handler){
        add(uri,"POST",handler)
    }


    fun findMatch(session: MyRequest): Handler? {
        val handler = handlers.find {
            session.uri == it.first
        }?.second
        return handler
    }
}
