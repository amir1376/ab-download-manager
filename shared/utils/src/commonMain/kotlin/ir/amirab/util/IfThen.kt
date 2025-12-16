@file:OptIn(ExperimentalContracts::class)

package ir.amirab.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <Base, T : Base> T.ifThen(condition: Boolean, block: T.() -> Base): Base {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (condition) {
        this.block()
    } else {
        this
    }
}
