package ir.amirab.downloader.downloaditem.torrent

object TorrentLinks {
    fun isMagnet(link: String): Boolean =
        link.trim().startsWith("magnet:", ignoreCase = true)

    fun isTorrentFile(link: String): Boolean =
        link.trim().substringBefore("?").endsWith(".torrent", ignoreCase = true)

    fun isSupported(link: String): Boolean = isMagnet(link) || isTorrentFile(link)

    // stub: name guessed from the link only, no network and no percent-decoding yet
    fun guessName(link: String): String? {
        val l = link.trim()
        if (isMagnet(l)) {
            return l.substringAfter("?", "").split("&")
                .firstOrNull { it.startsWith("dn=", ignoreCase = true) }
                ?.substringAfter("=")?.replace("+", " ")
                ?.takeIf { it.isNotBlank() }
        }
        return l.substringBefore("?").substringAfterLast("/").takeIf { it.isNotBlank() }
    }
}
