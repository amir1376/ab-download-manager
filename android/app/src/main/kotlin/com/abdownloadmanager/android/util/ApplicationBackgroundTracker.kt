package com.abdownloadmanager.android.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object ApplicationBackgroundTracker {
    fun startTracking(application: Application) {
        application.registerActivityLifecycleCallbacks(Tracker)
    }
    val isInBackgroundFlow = Tracker.count.mapStateFlow {
        it == 0
    }
    fun isInBackground(): Boolean {
        return isInBackgroundFlow.value
    }
}

private object Tracker : Application.ActivityLifecycleCallbacks {
    val count = MutableStateFlow(0)
    override fun onActivityStarted(activity: Activity) {
        count.update { it + 1 }
    }

    override fun onActivityStopped(activity: Activity) {
        count.update { it - 1 }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }
}
