package ir.amirab.downloader.anntation


/**
 * annotate that a method have long time operation and
 * should not used in main thread
 */
@Retention(AnnotationRetention.SOURCE)
annotation class HeavyCall