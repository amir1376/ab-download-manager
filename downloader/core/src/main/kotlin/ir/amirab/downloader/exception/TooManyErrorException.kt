package ir.amirab.downloader.exception

class TooManyErrorException(
    lastException: Throwable,
) : Exception(
    "Download is stopped because all parts exceeds max retries",
    lastException,
)
