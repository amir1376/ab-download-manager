package com.abdownloadmanager.shared.utils

fun exceptionToString(exception: Exception): String {
    return exception.message?:exception::class.qualifiedName?:"Unknown Error"
}