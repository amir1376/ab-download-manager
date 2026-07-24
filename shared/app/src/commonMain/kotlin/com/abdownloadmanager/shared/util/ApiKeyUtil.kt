package com.abdownloadmanager.shared.util


object ApiKeyUtil {
    fun isValidKey(key: String): Boolean {
        return key.isNotBlank()
    }

    const val DEFAULT_KEY_LENGTH = 24
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    fun generateKey(length: Int = DEFAULT_KEY_LENGTH): String {
        return (1..length)
            .map { CHARS.random() }
            .joinToString("")
    }
}
