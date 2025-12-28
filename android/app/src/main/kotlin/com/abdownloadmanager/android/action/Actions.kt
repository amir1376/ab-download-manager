package com.abdownloadmanager.android.action

import com.abdownloadmanager.android.util.pagemanager.IBrowserPageManager
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.util.compose.action.AnAction
import ir.amirab.util.compose.action.simpleAction
import ir.amirab.util.compose.asStringSource

fun createOpenBrowserAction(
    browserPageManager: IBrowserPageManager,
): AnAction {
    return simpleAction(
        Res.string.browser.asStringSource(),
        MyIcons.earth,
    ) {
        browserPageManager.openBrowser(null)
    }
}
