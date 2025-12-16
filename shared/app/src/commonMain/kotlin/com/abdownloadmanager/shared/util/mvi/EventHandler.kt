package com.abdownloadmanager.shared.util.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


interface SupportsEvents<Event> : IEventHandlerAndReceiver<Event> {
    fun getEventHandler(): IEventHandlerAndReceiver<Event>
    override fun sendEvent(event: Event) {
        getEventHandler().sendEvent(event)
    }
}

fun <Event> eventHandler(
    scope: CoroutineScope,
    handler: suspend (handle: Event) -> Unit,
) = object : EventHandler<Event>(scope) {
    override suspend fun handleEvent(event: Event) {
        handler(event)
    }
}


interface IEventReceiver<Event> {
    fun sendEvent(event: Event)
}

interface IEventHandler<Event>{
    suspend fun handleEvent(event: Event)
}

interface IEventHandlerAndReceiver<Event> : IEventReceiver<Event>, IEventHandler<Event>

abstract class EventHandler<Event>(
    private val scope: CoroutineScope,
) : IEventHandlerAndReceiver<Event> {
    private val _eventsFlow = MutableSharedFlow<Event>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        _eventsFlow.onEach {
            handleEvent(it)
        }.launchIn(scope)
    }

    override fun sendEvent(event: Event) {
        _eventsFlow.tryEmit(event)
    }
}

