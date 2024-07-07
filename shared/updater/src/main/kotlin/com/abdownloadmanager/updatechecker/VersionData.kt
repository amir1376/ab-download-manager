package com.abdownloadmanager.updatechecker

import io.github.z4kn4fein.semver.Version
import ir.amirab.util.platform.Platform

data class VersionData(
    val version: Version,
    val platform: Platform,
    val name:String,
    val link: String,
    val changeLog:String,
)