package com.abdownloadmanager.shared.util.dns

import ir.amirab.util.singleEntryCache
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.internal.OkHttpInternalApi
import okhttp3.internal.platform.Platform
import java.net.InetAddress


class AppDns(
    private val parentClient: OkHttpClient,
    private val dnsOptionProvider: DnsOptionProvider
) : Dns {

    private var lastUsedDns = singleEntryCache<DNSOption, Dns>(
        threadSafe = true,
    )

    fun getDelegate(): Dns {
        return lastUsedDns.getOrCreate(dnsOptionProvider.getDNSOption()) {
            createDnsFromOptions(it)
        }
    }

    @OptIn(OkHttpInternalApi::class)
    private fun createDnsFromOptions(dnsOption: DNSOption): Dns {
        val dns = when (dnsOption) {
            is DNSOption.DnsOverHttps -> DnsOverHttps.Builder()
                .client(parentClient)
                .url(dnsOption.url.toHttpUrl())
                .build()

            DNSOption.System -> Platform.get().systemDns
        }
        return dns
    }

    override fun lookup(hostname: String): List<InetAddress> {
        return getDelegate().lookup(hostname)
    }

    override fun newCall(request: Dns.Request): Dns.Call {
        return getDelegate().newCall(request)
    }
}
