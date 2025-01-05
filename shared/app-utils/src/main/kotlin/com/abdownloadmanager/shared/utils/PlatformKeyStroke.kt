package com.abdownloadmanager.shared.utils

interface PlatformKeyStroke {
    fun getModifiers(): List<String>
    fun getKeyText(): String
}