package com.abdownloadmanager.desktop.utils.action

import com.abdownloadmanager.desktop.ui.icon.IconSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import javax.swing.Icon

abstract class AnAction(
    title: String,
    icon: IconSource? = null,
) : MenuItem.SingleItem(
    title=title,
    icon=icon,
) {
    override fun onClick() = actionPerformed()

    abstract fun actionPerformed()
}


