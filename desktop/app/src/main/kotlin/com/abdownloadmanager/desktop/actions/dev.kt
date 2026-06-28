package com.abdownloadmanager.desktop.actions

import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.di.Di
import com.abdownloadmanager.desktop.pages.poweractionalert.PowerActionComponent
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.ui.widget.MessageDialogType
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.action.createDummyExceptionAction
import com.abdownloadmanager.shared.action.createDummyMessageAction
import com.xeton.util.compose.action.AnAction
import com.xeton.util.compose.action.MenuItem
import com.xeton.util.compose.action.simpleAction
import com.xeton.util.compose.asStringSource
import com.xeton.util.desktop.poweraction.PowerActionConfig
import org.koin.core.component.get

private val appComponent = Di.get<AppComponent>()
val dummyMessage = createDummyMessageAction(appComponent)
val dummyException = createDummyExceptionAction()
val shutdown = simpleAction(
    Res.string.shutdown_now.asStringSource(),
    MyIcons.exit,
) {
    appComponent.initiatePowerAction(
        PowerActionConfig(PowerActionConfig.Type.Shutdown, false),
        PowerActionComponent.PowerActionReason.Unknown
    )
}
