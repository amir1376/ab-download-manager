package com.abdownloadmanager.desktop.utils

import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.resources.ResourceUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayInputStream
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

enum class NotificationSoundEvent(
    val bundledResourcePath: String,
) {
    DownloadCompleted("com/abdownloadmanager/resources/sounds/download-completed.wav"),
    DownloadError("com/abdownloadmanager/resources/sounds/download-error.wav"),
    QueueStarted("com/abdownloadmanager/resources/sounds/queue-started.wav"),
    QueueEnded("com/abdownloadmanager/resources/sounds/queue-ended.wav"),
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
        playBundled(event)
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

    private fun playBundled(event: NotificationSoundEvent): Boolean {
        return runCatching {
            val bytes = ResourceUtil.readResourceAsByteArray(event.bundledResourcePath)
            ByteArrayInputStream(bytes).use { byteStream ->
                AudioSystem.getAudioInputStream(byteStream).use { inputStream ->
                    playInputStream(inputStream)
                }
            }
        }.getOrDefault(false)
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
