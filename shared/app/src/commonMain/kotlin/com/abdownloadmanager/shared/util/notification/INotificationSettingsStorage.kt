package com.abdownloadmanager.shared.util.notification

import kotlinx.coroutines.flow.MutableStateFlow

interface INotificationSettingsStorage {
    val notificationSound: MutableStateFlow<Boolean>
    val generalNotificationSound: MutableStateFlow<String>
    val errorNotificationSound: MutableStateFlow<String>
    val successNotificationSound: MutableStateFlow<String>
}
