package com.abdownloadmanager.shared.util.downloaderror

interface DownloadErrorMapper {
    fun accept(throwable: Throwable): Boolean

    /**
     * build a title and description from the throwable and pass it to [createErrorReason] and return it
     * @param throwable this is the throwable that we accept it to process. so casting it is safe
     */
    fun getReason(throwable: Throwable): DownloadErrorReason

    companion object {
        fun createErrorReason(
            title: String,
            description: String,
            suggestion: String,
            throwable: Throwable,
        ): DownloadErrorReason {
            return DownloadErrorReason(
                title = title,
                description = description,
                suggestion = suggestion,
                throwableName = throwable::class.qualifiedName ?: "<Unknown Throwable Class>",
                throwable.localizedMessage
            )
        }
    }
}
