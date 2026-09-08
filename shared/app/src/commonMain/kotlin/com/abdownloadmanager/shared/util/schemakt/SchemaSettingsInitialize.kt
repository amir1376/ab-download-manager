package com.abdownloadmanager.shared.util.schemakt

import io.github.amir1376.schemakt.Schema

fun Schema.Companion.initializeForABDM() {
    Schema.Settings.defaultStrict = false
}