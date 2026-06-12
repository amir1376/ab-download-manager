package com.abdownloadmanager.shared.util.notification

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Toolkit
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

actual fun platformNotificationSound(): INotificationSound = DesktopNotificationSound

internal object DesktopNotificationSound : BaseNotificationSound(), KoinComponent {
    private val notificationSoundSettings: INotificationSettingsStorage by inject()

    override fun getNotificationSoundSettingsStorage(): INotificationSettingsStorage {
        return notificationSoundSettings
    }

    override fun actualPlay(audioSource: String) {

        val audioStream = getAudioSource(audioSource) ?: run {
            runCatching {
                Toolkit.getDefaultToolkit().beep()
            }
            return
        }
        audioStream.use {
            val clip = AudioSystem.getClip()
            clip.open(it)
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    clip.close()
                }
            }
            clip.start()
        }
        return
    }

    private fun getAudioSource(audioSource: String): AudioInputStream? {
        if (audioSource.isBlank()) {
            return null
        }
        return runCatching {
            AudioSystem.getAudioInputStream(File(audioSource))
        }.getOrNull()
    }
}
