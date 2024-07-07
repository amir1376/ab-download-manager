package com.abdownloadmanager.desktop.utils

import kotlinx.coroutines.flow.StateFlow

interface ShouldValidate {
    val valid: StateFlow<Boolean>
}