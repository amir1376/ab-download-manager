package com.abdownloadmanager.shared.utils

import java.net.URL

fun isValidUrl(link: String): Boolean {
    return runCatching { URL(link) }.isSuccess
}