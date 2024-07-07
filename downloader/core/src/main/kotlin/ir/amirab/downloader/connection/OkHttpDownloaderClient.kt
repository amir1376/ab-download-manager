package ir.amirab.downloader.connection

import ir.amirab.downloader.connection.response.ResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.utils.await
import okhttp3.*

class OkHttpDownloaderClient(
    private val okHttpClient: OkHttpClient,
) : DownloaderClient() {
    private fun newCall(
        downloadCredentials: IDownloadCredentials,
        start: Long,
        end: Long?,
        extraBuilder: Request.Builder.() -> Unit,
    ): Call {
        val rangeHeader = createRangeHeader(start, end)
        return okHttpClient.newCall(
            Request.Builder()
                .url(downloadCredentials.link)
                .apply {
                    defaultHeaders().forEach { (k, v) ->
                        header(k, v)
                    }
                    downloadCredentials.headers
                        ?.filter {
                            //OkHttp handles this header and if we override it,
                            //makes redirected links to have this "Host" instead of their own!, and cause error
                            !it.key.equals("Host",true)
                        }
                        ?.forEach { (k, v) ->
                        header(k, v)
                    }
                    val username = downloadCredentials.username
                    val password = downloadCredentials.password
                    if (username?.isNotBlank() == true && password?.isNotBlank() == true) {
                        header("Authorization", Credentials.basic(username, password))
                    }
                    downloadCredentials.userAgent?.let { userAgent->
                        header("User-Agent", userAgent)
                    }
                }
                .apply(extraBuilder)
                .header(rangeHeader.first, rangeHeader.second)
                .build()
        )
    }


    override suspend fun head(credentials: IDownloadCredentials): ResponseInfo {
        newCall(
            downloadCredentials = credentials,
            start = 0,
            end = 255,
            extraBuilder = {
//                head()
            }
        ).await().use { response ->
//            println(response.headers)
            return createFileInfo(response)
        }
    }

    private fun createFileInfo(response: Response): ResponseInfo {
        return ResponseInfo(
            statusCode = response.code,
            message = response.message,
            requestHeaders = response.request.headers.associate { (key, value) ->
                key.lowercase() to value
            },
            responseHeaders = response.headers.associate { (key, value) ->
                key.lowercase() to value
            },
        )
    }

    override suspend fun connect(
        credentials: IDownloadCredentials,
        start: Long,
        end: Long?,
    ): Connection {
        val response = newCall(
            downloadCredentials = credentials,
            start = start,
            end = end,
            extraBuilder = {
                get()
            }
        ).await()
        val body = runCatching {
            requireNotNull(response.body){
                "body is null"
            }
        }.onFailure {
            response.close()
        }.getOrThrow()
        return Connection(
            source = body.source(),
            contentLength = body.contentLength(),
            closeable = response,
            responseInfo = createFileInfo(response)
        )
    }
}