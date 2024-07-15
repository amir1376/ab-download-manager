package ir.amirab.downloader.connection.response

import ir.amirab.downloader.connection.response.headers.getContentRange
import ir.amirab.downloader.connection.response.headers.extractFileNameFromContentDisposition
import ir.amirab.downloader.exception.UnSuccessfulResponseException
import ir.amirab.downloader.utils.FileNameUtil
import ir.amirab.util.UrlUtils

data class ResponseInfo(
    val statusCode: Int,
    val message: String,
    val requestUrl: String,
    val requestHeaders: Map<String, String> = linkedMapOf(),
    val responseHeaders: Map<String, String> = linkedMapOf(),
) {

    val isSuccessFul by lazy {
        statusCode in 200..299
    }

    val contentLength by lazy {
        responseHeaders["content-length"]?.toLongOrNull()
    }

    //total length of whole file even if it is partial content
    val totalLength by lazy {
        if (statusCode == 206) {
            getContentRange()?.fullSize
        } else contentLength
    }
    val requiresAuth by lazy {
        statusCode == 401
    }

    val requireBasicAuth by lazy {
        requiresAuth && (responseHeaders["www-authenticate"]?.contains("basic", true) ?: false)
    }

    val resumeSupport by lazy {
        statusCode == 206
    }
    val fileName: String? by lazy {
        val foundName = run {
            val nameFromHeader = responseHeaders["content-disposition"]?.let {
                extractFileNameFromContentDisposition(it)
            }
            if (nameFromHeader!=null){
                return@lazy nameFromHeader
            }
            UrlUtils.extractNameFromLink(requestUrl).orEmpty()
        }
        var valueToReturn = foundName
        if (isWebPage()){
            valueToReturn = FileNameUtil.replaceExtension(
                valueToReturn,
                "html",
                true
            )
        }
        valueToReturn
    }

    // It is good to use these properties to check file is valid
    // for now we depend on size
    val lastModified: String? by lazy {
        responseHeaders["last-modified"]
    }
    val etag: String? by lazy {
        responseHeaders["etag"]
    }
}

fun ResponseInfo.isWebPage(): Boolean {
    return responseHeaders["content-type"].orEmpty().contains("text/html")
}

fun ResponseInfo.expectSuccess() = apply {
    if (!isSuccessFul) {
        throw UnSuccessfulResponseException(statusCode, message)
    }
}