package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Toolkit
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

enum class NotificationSoundEvent {
    DownloadCompleted,
    DownloadError,
    QueueStarted,
    QueueEnded,
}

class NotificationSoundPlayer : KoinComponent {
    private val appSettings: AppSettingsStorage by inject()

    fun play(event: NotificationSoundEvent) {
        play(event, respectGlobalToggle = true)
    }

    fun preview(event: NotificationSoundEvent) {
        play(event, respectGlobalToggle = false)
    }

    private fun play(event: NotificationSoundEvent, respectGlobalToggle: Boolean) {
        if (respectGlobalToggle && !appSettings.notificationSound.value) {
            return
        }
        val customPath = customPathFor(event)
        if (!customPath.isNullOrBlank() && playFile(customPath)) {
            return
        }
        playSystemDefault()
    }

    private fun customPathFor(event: NotificationSoundEvent): String {
        return when (event) {
            NotificationSoundEvent.DownloadCompleted -> appSettings.downloadCompletedSoundPath.value
            NotificationSoundEvent.DownloadError -> appSettings.downloadErrorSoundPath.value
            NotificationSoundEvent.QueueStarted -> appSettings.queueStartedSoundPath.value
            NotificationSoundEvent.QueueEnded -> appSettings.queueEndedSoundPath.value
        }
    }

    private fun playFile(path: String): Boolean {
        val file = runCatching { File(path).canonicalFile }.getOrNull() ?: return false
        if (!file.exists() || !file.isFile) {
            return false
        }
        return runCatching {
            AudioSystem.getAudioInputStream(file).use { inputStream ->
                playInputStream(inputStream)
            }
        }.getOrDefault(false)
    }

    private fun playSystemDefault() {
        runCatching {
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun playInputStream(inputStream: AudioInputStream): Boolean {
        return runCatching {
            val clip = AudioSystem.getClip()
            clip.open(inputStream)
            clip.addLineListener { lineEvent ->
                if (lineEvent.type == LineEvent.Type.STOP) {
                    clip.close()
                }
            }
            clip.start()
            true
        }.getOrDefault(false)
    }
}
