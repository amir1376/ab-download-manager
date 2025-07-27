package ir.amirab.downloader.utils

import okhttp3.Call
import okhttp3.Response
import okhttp3.coroutines.executeAsync

suspend fun Call.await(): Response {
    return executeAsync()
}
