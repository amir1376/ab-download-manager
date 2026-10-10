package com.abdownloadmanager.shared.storage

import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

enum class SpeedLimitMode {
    Disabled,
    Enabled,
    EnabledWhenBessy,
}

fun SpeedLimitMode.asShortStringSource(): StringSource = when (this) {
    SpeedLimitMode.Disabled -> Res.string.disabled
    SpeedLimitMode.Enabled -> Res.string.enabled
    SpeedLimitMode.EnabledWhenBessy -> Res.string.enabled_when_bessy
}.asStringSource()
fun SpeedLimitMode.asLongStringSource(): StringSource = when (this) {
    SpeedLimitMode.Disabled -> Res.string.unlimited
    SpeedLimitMode.Enabled -> Res.string.limited
    SpeedLimitMode.EnabledWhenBessy -> Res.string.settings_global_speed_limiter_enabled_describe_when_bessy
}.asStringSource()
