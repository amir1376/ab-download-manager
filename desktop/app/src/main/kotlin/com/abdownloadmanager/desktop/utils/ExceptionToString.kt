package com.abdownloadmanager.desktop.utils

fun exceptionToString(exception: Exception): String {
    return exception.message?:exception::class.qualifiedName?:"Unknown Error"
}