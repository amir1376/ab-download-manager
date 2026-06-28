package com.xeton.util

fun String.toSingleLine(): String {
    return ifThen(contains('\n') || contains('\r')) {
        buildString(length) {
            for (c in this@toSingleLine) {
                if (c == '\n' || c == '\r') {
                    continue
                }
                append(c)
            }
        }
    }

}
