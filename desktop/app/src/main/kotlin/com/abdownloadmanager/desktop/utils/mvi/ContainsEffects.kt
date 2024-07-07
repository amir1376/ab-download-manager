package com.abdownloadmanager.desktop.utils.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface ContainsEffects<Effect> {
    val effects: SharedFlow<Effect>
    fun sendEffect(effect:Effect)
}

private class SupportsEffects<Effect>: ContainsEffects<Effect> {
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val effects: SharedFlow<Effect> = _effects
    override fun sendEffect(effect:Effect){
        _effects.tryEmit(effect)
    }
}

fun <Effect>supportEffects():ContainsEffects<Effect> = SupportsEffects()

@Composable
fun <T> HandleEffects(
    effectContainer :ContainsEffects<T>,
    handle:(T)->Unit
){
    LaunchedEffect(effectContainer){
        effectContainer.effects.onEach {
            handle(it)
        }.launchIn(this)
    }
}