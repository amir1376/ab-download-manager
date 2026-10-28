package com.abdownloadmanager.shared.util

import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.asStringSource

val EmptyStringSource = "".asStringSource()

fun Boolean.asToggledStringSource() = when (this) {
    true -> Res.string.enabled
    false -> Res.string.disabled
}.asStringSource()
