package com.abdownloadmanager.shared.util

fun exceptionToString(exception: Exception): String {
    return exception.message?:exception::class.qualifiedName?:"Unknown Error"
}
