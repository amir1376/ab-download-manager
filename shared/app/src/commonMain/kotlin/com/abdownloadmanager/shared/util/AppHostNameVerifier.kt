package com.abdownloadmanager.shared.util

import kotlinx.coroutines.flow.StateFlow
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class AppHostNameVerifier(
    private val delegateHostnameVerifier: HostnameVerifier,
    private val ignoreHostNameVerification: StateFlow<Boolean>,
) : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        if (ignoreHostNameVerification.value) {
            return true
        }
        return delegateHostnameVerifier.verify(hostname, session)
    }
}
