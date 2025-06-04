package com.abdownloadmanager.shared.utils

interface PlatformKeyStroke {
    val keyCode: Int

    fun getModifiers(): List<String>
    fun getKeyText(): String
}