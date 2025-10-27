package ir.amirab.downloader.exception

//it should not happen unless web server is not respect our header
class ServerPartIsNotTheSameAsWeExpectException(
    start:Long,
    end:Long?,
    expectedLength:Long?,
    actualLength:Long?,
) : DownloadValidationException (
    "Response Length not match.expecting '${expectedLength}',but we got '$actualLength',requested range is range is ${start}-${end}"
//            + "\n request headers ${conn.responseInfo.requestHeaders}"
//            + "\n response headers ${conn.responseInfo.responseHeaders}"
){
    override fun isCritical(): Boolean {
        // some webservers somehow does not return the expected size at the first place
        // but after some try... they do!!!
        // because of them, I have to make this error non-critical
        // I have to investigate why!
        return false
    }
}
