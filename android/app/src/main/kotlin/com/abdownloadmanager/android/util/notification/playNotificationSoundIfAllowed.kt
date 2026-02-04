package com.abdownloadmanager.android.util.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
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
    val notificationsAudioVolume = getNotificationAudioVolume(context)
    if (notificationsAudioVolume == 0f) {
        return
    }
    val uri = RingtoneManager
        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ?: return
    val ringtone = RingtoneManager
        .getRingtone(context, uri)
        ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ringtone.volume = notificationsAudioVolume
    }
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

private fun getNotificationAudioVolume(context: Context): Float {
    val audioManager = context.getSystemService<AudioManager>() ?: return 0f
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
    val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        audioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION)
    } else {
        0
    }

    // convert current value to 0f..1f range, depend on min and max value
    return ((currentVolume - minVolume).toFloat() / (maxVolume - minVolume).toFloat()).coerceIn(
        0f,
        1f
    )
}
