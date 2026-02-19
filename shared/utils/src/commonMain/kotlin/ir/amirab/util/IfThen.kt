@file:OptIn(ExperimentalContracts::class)

package ir.amirab.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.ExperimentalExtendedContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalExtendedContracts::class)
inline fun <Base, T : Base> T.ifThen(condition: Boolean, block: T.() -> Base): Base {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        condition holdsIn block
    }
    return if (condition) {
        this.block()
    } else {
        this
    }
}
