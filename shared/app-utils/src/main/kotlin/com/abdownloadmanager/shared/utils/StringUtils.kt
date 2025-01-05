package com.abdownloadmanager.shared.utils

fun String.takeOrAppendDots(takeCount: Int): String {
    val take = take(takeCount)
    if (length<=takeCount){
        return take
    }else{
        return "$take…"
    }
}