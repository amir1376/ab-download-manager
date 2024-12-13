package com.abdownloadmanager.updatechecker

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.Platform

data class UpdateInfo(
    val version: Version,
    val platform: Platform,
    val arch: Arch,
    val name:String,
    val link: String,
    val changeLog:String,
)