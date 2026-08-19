package com.abdownloadmanager.shared.util.dns

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.storage.DnsSettings
import com.abdownloadmanager.shared.util.dns.DnsModes.*
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

// switch for UI
enum class DnsModes(
    val stringSource: StringSource
) {
    System(Res.string.settings_dns_system.asStringSource()),
    DnsOverHttps(Res.string.settings_dns_doh.asStringSource()),
}

sealed interface DNSOption {
    data object System : DNSOption
    data class DnsOverHttps(
        val url: String,
    ) : DNSOption {
        companion object {
            fun isValid(url: String): Boolean {
                val isHttps = url.startsWith("https://")
                return isHttps && HttpUrlUtils.isValidUrl(url)
            }
        }
    }

    companion object {
        fun parse(
            string: String,
        ): DNSOption? {
            if (DnsOverHttps.isValid(string)) {
                return DnsOverHttps(string)
            }
            return null
        }
    }
}

fun DnsSettings.toDnsOptionOrDefault(): DNSOption {
    return when (mode) {
        System -> DNSOption.System
        DnsOverHttps -> DNSOption.parse(address)
    } ?: DNSOption.System
}

fun DNSOption.toEnum(): DnsModes {
    return when (this) {
        is DNSOption.DnsOverHttps -> DnsModes.DnsOverHttps
        DNSOption.System -> DnsModes.System
    }
}
