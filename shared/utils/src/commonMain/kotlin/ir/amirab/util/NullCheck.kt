@file:OptIn(ExperimentalContracts::class)
@file:Suppress("NOTHING_TO_INLINE")

package ir.amirab.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun <T> isNull(value: T): Boolean {
    contract {
        returns(true) implies (value == null)
        returns(false) implies (value != null)
    }
    return value == null
}

inline fun <T> isNotNull(value: T): Boolean {
    contract {
        returns(true) implies (value != null)
        returns(false) implies (value == null)
    }
    return value != null
}
