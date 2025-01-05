package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.desktop.ui.widget.MessageDialogType
import ir.amirab.util.compose.action.AnAction
import ir.amirab.util.compose.action.MenuItem
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource
import org.koin.core.component.get

private val appComponent = Di.get<AppComponent>()

val dummyException by lazy {
    simpleAction(
        "Dummy Exception".asStringSource(),
        MyIcons.info
    ) {
        error("This is a dummy exception that is thrown by developer")
    }
}
val dummyMessage by lazy {
    MenuItem.SubMenu(
        title = "Show Dialog Message".asStringSource(),
        icon = MyIcons.info,
        items = listOf(
            MessageDialogType.Info,
            MessageDialogType.Error,
            MessageDialogType.Warning,
            MessageDialogType.Success,
        ).map(::createDummyMessage)
    )
}

private fun createDummyMessage(type: MessageDialogType): AnAction {
    return simpleAction(
        "$type Message".asStringSource(),
        MyIcons.info,
    ) {
        appComponent.sendDialogNotification(
            type = type,
            title = "Dummy Message".asStringSource(),
            description = "This is a test message".asStringSource()
        )
    }
}