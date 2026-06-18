package com.abdownloadmanager.desktop.utils.singleInstance

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.lens.BiDiBodyLensSpec
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun<reified T:Any> Command(name: String):Command<T>{
    return Command(name, typeOf<T>())
}
class Command<T : Any>(val name: String, val type: KType)
sealed class CommandResult<T : Any> {
    override fun toString(): String {
        return this::class.simpleName!!
    }
    fun orElse(block:(Error<T>)->T):T{
        return when(this){
            is Success->value
            else ->block(this as Error<T>)
        }
    }

    fun <R>fold(
        onError:(Error<T>)->R,
        onSuccess:(Success<T>)->R,
    ):R{
        return when(this){
            is Error -> {
                onError(this)
            }
            is Success -> {
                onSuccess(this)
            }
        }
    }



    data class Success<T : Any>(val value: T) : CommandResult<T>()
    open class Error<T : Any>(val value: String) : CommandResult<T>(){
        override fun toString(): String {
            val name= super.toString()
            return """
                $name , $value
            """.trimIndent()
        }
    }
    class ServerNotExists<T : Any> : Error<T>("server not exists")
    class ClientError<T : Any>(value: String) : Error<T>("client error $value")
    class ServerError<T : Any>(code: Int, message: String, body: String) : Error<T>("""
server error
    statusCode: $code , message: $message
    body: $body
    """.trimIndent()
    )
}
infix fun <T : Any> Command<T>.bindSafe(
    handle: (Request) -> T,
): RoutingHttpHandler {
    return name bind {
        Response(Status.OK)
            .with(json(handle(it),type))
    }
}

private val client by lazy {
    OkHttp()
}


internal fun <T : Any> typeSafeRequest(
    port:Int,
    command: Command<T>
): CommandResult<T> {
    val autoBody = Body.auto<T>(command.type).toLens()
    val request = Request(
        method = Method.GET,
        uri = Uri.of("http://localhost:$port/${command.name}"),
    )
    val response = try {
        client(request)
    } catch (e: Exception) {
        return CommandResult.ClientError(e.localizedMessage)
    }
    return if (response.status.successful) {
        try {
            CommandResult.Success(autoBody(response))
        } catch (e: Exception) {
            CommandResult.Error(e.localizedMessage)
        }
    } else {
        CommandResult.ServerError(
            response.status.code,
            response.status.description,
            response.bodyString(),
        )
    }
}

private val json by lazy {
    Json
}
fun <T> toJson(data: T, serializer: KSerializer<T>): String {
    return json.encodeToString(serializer, data)
}

fun <T> fromJson(string: String, serializer: KSerializer<T>): T {
    return json.decodeFromString(serializer, string)
}

inline fun <reified T> toJson(data: T): String {
    return toJson(data, serializer())
}

inline fun <reified T> fromJson(string: String): T {
    return fromJson(string, serializer())
}

inline fun <reified T> Body.Companion.auto(): BiDiBodyLensSpec<T> {
    return Body.string(ContentType.APPLICATION_JSON)
        .map(
            nextIn = {
                fromJson<T>(it)
            },
            nextOut = {
                toJson<T>(it)
            },
        )
}

//unchecked auto!!
fun <T> Body.Companion.auto(kType: KType): BiDiBodyLensSpec<T> {
    val serializer = serializer(kType) as KSerializer<T>
    return Body.string(ContentType.APPLICATION_JSON)
        .map(
            nextIn = {
                fromJson(it, serializer)
            },
            nextOut = {
                toJson(it, serializer)
            },
        )
}


inline fun <reified T : Any> json(data: T): (Response) -> Response {
    return Body.auto<T>().toLens() of data
}
fun <T : Any> json(data: T,type: KType): (Response) -> Response {
    return Body.auto<T>(type).toLens() of data
}

