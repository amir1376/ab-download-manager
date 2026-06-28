package com.abdownloadmanager.cli.client

/**
 * Result of a DesktopClient HTTP request to the desktop app's integration server.
 */
sealed class DesktopResult {
    /** Request succeeded. data holds the raw response body string. */
    data class Success(val data: String) : DesktopResult()

    /** Connection-level error (desktop app not reachable). */
    data class ConnectionError(val message: String) : DesktopResult()

    /** HTTP error response from the server (non-2xx). */
    data class RemoteError(val statusCode: Int, val body: String) : DesktopResult()
}