package com.xeton.util.osfileutil

actual fun getPlatformFileUtil(): FileUtils {
    return AndroidFileUtil()
}
