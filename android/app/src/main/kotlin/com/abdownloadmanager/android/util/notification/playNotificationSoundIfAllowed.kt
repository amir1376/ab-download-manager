package com.abdownloadmanager.android.util.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.getSystemService
import java.io.File

fun playNotificationSoundIfAllowed(
    context: Context,
    customSoundPath: String = "",
) {
    if (isInDNDMode(context)) {
        return
    }
    if (isInSilentMode(context)) {
        return
    }

    // Try custom path first if provided
    if (customSoundPath.isNotBlank()) {
        runCatching {
            val customUri = if (customSoundPath.startsWith("content://", ignoreCase = true)
                || customSoundPath.startsWith("android.resource://", ignoreCase = true)
            ) {
                Uri.parse(customSoundPath)
            } else {
                Uri.fromFile(File(customSoundPath))
            }
            val ringtone = RingtoneManager.getRingtone(context, customUri)
            if (ringtone != null) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone.play()
                return
            }
        }.onFailure {
            it.printStackTrace()
        }
        // Fall through to default if custom fails
    }

    // Default system notification sound
    runCatching {
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
    }.onFailure {
        it.printStackTrace()
    }
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
