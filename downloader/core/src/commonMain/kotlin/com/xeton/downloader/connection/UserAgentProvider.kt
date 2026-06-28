package com.xeton.downloader.connection

interface UserAgentProvider {
    fun getUserAgent(): String?
}
