package com.abdownloadmanager.shared.util

import kotlinx.coroutines.flow.StateFlow
import okhttp3.internal.platform.Platform
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * at the moment we simply use okhttp ssl factory provider with a toggleable trust manager to ignore ssl certificates
 */
class AppSSLFactoryProvider(
    private val ignoreSSLCertificates: StateFlow<Boolean>,
) {
    val trustManager: X509TrustManager by lazy {
        ToggleableTrustManager(
            trustManager = Platform.get().platformTrustManager(),
            shouldCheck = { !ignoreSSLCertificates.value }
        )
    }

    fun createSSLSocketFactory(): SSLSocketFactory {
        return Platform.get().newSslSocketFactory(
            trustManager = trustManager,
        )
    }
}


private class ToggleableTrustManager(
    private val trustManager: X509TrustManager,
    private val shouldCheck: () -> Boolean,
) : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if (shouldCheck()) {
            trustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if (shouldCheck()) {
            trustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return trustManager.acceptedIssuers
    }
}
