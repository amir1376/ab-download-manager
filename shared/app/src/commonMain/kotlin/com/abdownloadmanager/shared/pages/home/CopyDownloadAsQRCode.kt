package com.abdownloadmanager.shared.pages.home

import ir.amirab.util.compose.StringSource

data class CopyDownloadAsQRCode(
    val title: StringSource,
    val qrCode: String,
)
