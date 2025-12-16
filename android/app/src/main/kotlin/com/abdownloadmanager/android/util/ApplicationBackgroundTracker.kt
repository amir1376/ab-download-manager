package com.abdownloadmanager.android.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import arrow.atomic.AtomicInt

object ApplicationBackgroundTracker {
    fun startTracking(application: Application) {
        application.registerActivityLifecycleCallbacks(Tracker)
    }

    fun isInBackground(): Boolean {
        return Tracker.count.get() == 0
    }
}

private object Tracker : Application.ActivityLifecycleCallbacks {
    val count = AtomicInt(0)
    override fun onActivityStarted(activity: Activity) {
        count.incrementAndGet()
    }

    override fun onActivityStopped(activity: Activity) {
        count.decrementAndGet()
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
