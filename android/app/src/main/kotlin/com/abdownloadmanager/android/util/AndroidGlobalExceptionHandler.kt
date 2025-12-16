package com.abdownloadmanager.android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.abdownloadmanager.android.R
import com.abdownloadmanager.android.pages.crashreport.CrashReportActivity
import kotlin.system.exitProcess

class AndroidGlobalExceptionHandler(
    private val context: Context,
    private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    private val crashNotificationManager = CrashNotificationManager(context)
    override fun uncaughtException(t: Thread, e: Throwable) {
        runCatching {
            handleUncaughtException(t, e)
        }
        defaultUncaughtExceptionHandler
            ?.uncaughtException(t, e)
            ?: run {
                Log.e("Crash", e.localizedMessage, e)
                exitProcess(1)
            }
    }

    private fun handleUncaughtException(t: Thread, e: Throwable) {
        val intent = CrashReportActivity
            .createIntent(context, e)
            .addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        if (ApplicationBackgroundTracker.isInBackground()) {
            // show a notification so user can press and see the crash screen
            crashNotificationManager.postNotificationAboutTheCrash(context, intent)
        } else {
            // in case we are in the foreground directly show the error
            context.startActivity(intent)
        }
    }

    private class CrashNotificationManager(private val context: Context) {
        val notificationManagerCompat by lazy {
            NotificationManagerCompat.from(context)
        }
        private var initialized = false
        fun initNotificationChannel() {
            if (initialized) {
                return
            }
            runCatching {
                val notificationChanel = NotificationChannel(
                    AndroidConstants.NOTIFICATION_CRASH_REPORT_CHANEL_ID,
                    AndroidConstants.NOTIFICATION_CRASH_REPORT_CHANEL_NAME,
                    NotificationManager.IMPORTANCE_LOW,
                )
                notificationManagerCompat.createNotificationChannel(notificationChanel)
            }
            initialized = true
        }

        fun postNotificationAboutTheCrash(context: Context, intent: Intent) {
            initNotificationChannel()
            val notificationId = 555
            val pendingIntent =
                PendingIntent.getActivity(
                    context, notificationId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            val notification = NotificationCompat
                .Builder(context, AndroidConstants.NOTIFICATION_CRASH_REPORT_CHANEL_ID)
                .setSmallIcon(R.drawable.ic_monochrome)
                .setContentTitle("Application crashed!")
                .setSubText("Click to show info")
                .setGroup("Crash Report")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            runCatching {
                notificationManagerCompat.notify(notificationId, notification)
            }
        }
    }
}
