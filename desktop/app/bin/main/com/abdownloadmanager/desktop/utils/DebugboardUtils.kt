package com.abdownloadmanager.desktop.utils
//
//import ir.amirab.debugboard.core.plugin.watcher.RemoveWatch
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.job
//
//fun RemoveWatch.inScope(scope: CoroutineScope) {
//    val removeWatch = this
//    scope.coroutineContext.job.invokeOnCompletion {
//        removeWatch()
//    }
//}