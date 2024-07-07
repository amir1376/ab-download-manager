package com.abdownloadmanager.desktop.utils

fun <T> T.isAnyOf(vararg conditions: (T) -> Boolean): Boolean {
    return conditions.any {
        it(this)
    }
}

fun <T> T.isAllOf(vararg conditions: (T) -> Boolean): Boolean {
    return conditions.all {
        it(this)
    }
}