package com.abdownloadmanager.shared.pagemanager

import com.abdownloadmanager.shared.ui.widget.MessageDialogType
import com.abdownloadmanager.shared.ui.widget.NotificationType
import ir.amirab.util.compose.StringSource

interface NotificationSender {
    fun sendDialogNotification(title: StringSource, description: StringSource, type: MessageDialogType)
    fun sendNotification(tag: Any, title: StringSource, description: StringSource, type: NotificationType)
}
