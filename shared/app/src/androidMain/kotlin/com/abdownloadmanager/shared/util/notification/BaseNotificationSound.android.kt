package com.abdownloadmanager.shared.util.notification


import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.abdownloadmanager.shared.ui.widget.NotificationType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


actual fun platformNotificationSound(): INotificationSound = AndroidNotificationSound

internal object AndroidNotificationSound : BaseNotificationSound(), KoinComponent {
    private val context: Context by inject()
    private val notificationSoundSettings: INotificationSettingsStorage by inject()

    override fun getNotificationSoundSettingsStorage(): INotificationSettingsStorage {
        return notificationSoundSettings
    }

    override fun actualPlay(audioSource: String) {

        val uri = getAudioSourceUri(audioSource)
        // default system notification
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
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

    override fun shouldPlay(type: NotificationType): Boolean {
        if (!super.shouldPlay(type)) {
            return false
        }
        if (isInDNDMode(context)) {
            return false
        }
        if (isInSilentMode(context)) {
            return false
        }
        return true
    }

    private fun getAudioSourceUri(audioSource: String): Uri? {
        if (audioSource.isBlank()) {
            return null
        }
        return runCatching {
            audioSource.toUri()
        }.getOrNull()
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
}
