package com.abdownloadmanager.android.util.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
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

    ringtone.audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    ringtone.play()
    return
}

private fun isInDNDMode(context: Context): Boolean {
    val notificationManager = context.getSystemService<NotificationManager>() ?: return false
    return notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
}

private fun isInSilentMode(context: Context): Boolean {
    val audioManager = context.getSystemService<AudioManager>() ?: return false
    val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL || volume == 0
}

