package ir.amirab.util.osfileutil

actual fun getPlatformFileUtil(): FileUtils {
    return AndroidFileUtil()
}
