package com.abdownloadmanager.shared.utils.ui.theme

import kotlinx.coroutines.flow.Flow

interface ISystemThemeDetector {
    val isSupported: Boolean
    val isDark: Boolean
    val systemThemeFlow: Flow<Boolean>
}