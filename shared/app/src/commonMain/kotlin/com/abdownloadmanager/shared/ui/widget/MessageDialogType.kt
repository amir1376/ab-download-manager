package com.abdownloadmanager.shared.ui.widget

@Suppress("unused")
sealed class MessageDialogType {
    data object Success : MessageDialogType()
    data object Info : MessageDialogType()
    data object Error : MessageDialogType()
    data object Warning : MessageDialogType()
}
