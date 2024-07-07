package ir.amirab.downloader.exception

class TooManyErrorException : Exception(
    "Download is stopped because all parts exceeds max retries"
)
