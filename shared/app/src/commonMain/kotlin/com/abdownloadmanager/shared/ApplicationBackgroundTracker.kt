package com.abdownloadmanager.shared

import kotlinx.coroutines.flow.StateFlow

interface IApplicationBackgroundTracker {
    val isInBackgroundFlow: StateFlow<Boolean>
    fun isInBackground(): Boolean {
        return isInBackgroundFlow.value
    }
}

expect fun platformApplicationBackgroundTracker(): IApplicationBackgroundTracker
