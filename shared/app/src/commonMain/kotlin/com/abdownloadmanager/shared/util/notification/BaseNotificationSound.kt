package com.abdownloadmanager.shared.util.notification

import com.abdownloadmanager.shared.ui.widget.NotificationType


expect fun platformNotificationSound(): INotificationSound

interface INotificationSound {
    companion object {
        const val DEFAULT_VALUE = ""
    }

    fun play(
        type: NotificationType,
        force: Boolean = false,
    )

    fun actualPlay(
        audioSource: String,
    )
}

internal abstract class BaseNotificationSound : INotificationSound {
    override fun play(
        type: NotificationType,
        force: Boolean,
    ) {
        val uri = getNotificationAudioSource(type)
        if (!force && !shouldPlay(type)) return
        actualPlay(uri)
    }

    protected abstract fun getNotificationSoundSettingsStorage(): INotificationSettingsStorage

    /**
     * check silent mode, DND etc...
     */
    protected open fun shouldPlay(type: NotificationType): Boolean {
        return getNotificationSoundSettingsStorage().notificationSound.value
    }

    abstract override fun actualPlay(audioSource: String)

    protected fun getNotificationAudioSource(
        type: NotificationType,
    ): String {
        val storage = getNotificationSoundSettingsStorage()
        return when (type) {
            NotificationType.Error -> storage.errorNotificationSound
            NotificationType.Success -> storage.successNotificationSound
            NotificationType.Warning,
            is NotificationType.Loading,
            NotificationType.Info -> storage.generalNotificationSound
        }.value
    }
}
