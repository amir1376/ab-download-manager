package ir.amirab.downloader.exception

//it should not happened unless web server is not respect our header
class ServerPartIsNotTheSameAsWeExpectException(msg: String) : DownloadValidationException(msg)
