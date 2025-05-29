package ir.amirab.downloader.utils

import ir.amirab.downloader.exception.UnSuccessfulResponseException
import kotlinx.coroutines.CancellationException
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionUtils {
    fun isNormalCancellation(e: Throwable): Boolean {
        return e is CancellationException
    }

    fun isIOInterrupted(e: Throwable): Boolean {
        return e is InterruptedIOException
    }

    fun isNetworkError(e: Throwable): Boolean {
        return e is UnknownHostException ||
                e is SocketException ||
                e is SocketTimeoutException
    }

    fun isResponseError(e: Throwable): Boolean {
        return e is UnSuccessfulResponseException
    }
}

inline fun <T : Throwable> T.throwIf(condition: (T) -> Boolean) {
    if (condition(this)) {
        throw this
    }
}
inline fun <T : Throwable> T.throwIfCancelled() {
    throwIf { ExceptionUtils.isNormalCancellation(this) }
}


fun Throwable.printStackIfNOtUsual() {
    if (
        ExceptionUtils.isNormalCancellation(this) ||
        ExceptionUtils.isNetworkError(this) ||
        ExceptionUtils.isIOInterrupted(this) ||
        ExceptionUtils.isResponseError(this)
    ) {
        return
    }
    printStackTrace()
}
