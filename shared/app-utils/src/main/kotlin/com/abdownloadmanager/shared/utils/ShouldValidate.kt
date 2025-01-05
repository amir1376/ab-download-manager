package com.abdownloadmanager.shared.utils

import kotlinx.coroutines.flow.StateFlow

interface ShouldValidate {
    val valid: StateFlow<Boolean>
}