package com.abdownloadmanager.android.util.notification

import android.media.AudioManager
import android.media.RingtoneManager
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

fun playNotificationSoundIfAllowed(
    context: Context
) {
    if (isInDNDMode(context)) {
        return
    }
    if (isInSilentMode(context)) {
        return
    }
    val uri = RingtoneManager
        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ?: return
    val ringtone = RingtoneManager
        .getRingtone(context, uri)
        ?: return
    ringtone.play()
    return
}

private fun isInDNDMode(context: Context): Boolean {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return false
    return notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
}

private fun isInSilentMode(context: Context): Boolean {
    val audioManager = context.getSystemService<AudioManager>() ?: return false
    return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
}
