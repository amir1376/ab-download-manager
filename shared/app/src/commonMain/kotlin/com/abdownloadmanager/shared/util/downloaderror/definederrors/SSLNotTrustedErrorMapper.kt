package com.abdownloadmanager.shared.util.downloaderror.definederrors

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorMapper.Companion.createErrorReason
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import ir.amirab.util.compose.asStringSource

object SSLNotTrustedErrorMapper : DownloadErrorMapper {
    override fun accept(throwable: Throwable): Boolean {
        return isSslTrustError(throwable)
    }

    override fun getReason(throwable: Throwable): DownloadErrorReason {
        return createErrorReason(
            title = Res.string.download_error_reason_ssl_verification_failed_title.asStringSource().getString(),
            description = Res.string.download_error_reason_ssl_verification_failed_description.asStringSource()
                .getString(),
            suggestion = Res.string.download_error_reason_ssl_verification_failed_suggestion.asStringSource()
                .getString(),
            throwable = throwable,
        )
    }

    private fun isSslTrustError(t: Throwable): Boolean {
        var current: Throwable? = t

        while (current != null) {

            when (current) {
                is javax.net.ssl.SSLHandshakeException,
                is javax.net.ssl.SSLPeerUnverifiedException,
                is java.security.cert.CertPathValidatorException -> {
                    return true
                }
            }

            val msg = current.message?.lowercase()

            if (msg != null) {
                if (
                    "pkix path building failed" in msg ||
                    "trust anchor for certification path not found" in msg ||
                    "unable to find valid certification path" in msg
                ) {
                    return true
                }
            }

            current = current.cause
        }

        return false
    }

}
//description = "There is a problem when we want to verify the SSL, some website might use self signed certificates, in case you trust the website owner and your network you can enable 'Ignore SSL Verification' in the settings",
