package com.abdownloadmanager.desktop.pages.settings

import com.abdownloadmanager.desktop.ui.customwindow.CustomWindow
import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.utils.mvi.HandleEffects
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState

@Composable
fun SettingWindow(
    settingsComponent: SettingsComponent,
    onRequestCloseWindow:()->Unit,
){
    val state= rememberWindowState()
    CustomWindow(state, {
        onRequestCloseWindow()
    }) {
        HandleEffects(settingsComponent){
            when(it){
                SettingPageEffects.BringToFront -> {
                    state.isMinimized=false
                    window.toFront()
                }
            }
        }
//        Spacer(Modifier.fillMaxWidth().height(1.dp).background(myColors.surface))
        SettingsPage(settingsComponent,onRequestCloseWindow)
    }
}