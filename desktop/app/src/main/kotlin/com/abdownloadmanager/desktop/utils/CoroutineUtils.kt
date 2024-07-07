package com.abdownloadmanager.desktop.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun newScopeBasedOn(
    scope: CoroutineScope,
    extraContext: CoroutineContext = EmptyCoroutineContext
): CoroutineScope {
    return CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext.job) + extraContext)
}