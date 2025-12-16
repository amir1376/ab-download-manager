package com.abdownloadmanager.android.util

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.headlessComposeRuntime(
    content: @Composable () -> Unit,
): Job {
    val effectCoroutineContext = coroutineContext + HeadlessDefaultMonotonicFrameClock
    val recomposer = Recomposer(effectCoroutineContext)
    val composition = Composition(UnitApplier, recomposer)
    composition.setContent(content)
    val job = launch(HeadlessDefaultMonotonicFrameClock) {
        try {
            recomposer.runRecomposeAndApplyChanges()
        } catch (e: Throwable) {
            e.printStackTrace()
            composition.dispose()
        }
    }
    return job
}

private object UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) {}
    override fun insertTopDown(index: Int, instance: Unit) {}
    override fun move(from: Int, to: Int, count: Int) {}
    override fun remove(index: Int, count: Int) {}
    override fun onClear() {}
}

private object HeadlessDefaultMonotonicFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return onFrame(System.nanoTime())
    }
}
