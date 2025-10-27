package com.abdownloadmanager.shared.util.ui.theme

import kotlinx.coroutines.flow.Flow

interface ISystemThemeDetector {
    val isSupported: Boolean
    fun isDark(): Boolean
    val systemThemeFlow: Flow<Boolean>
}
