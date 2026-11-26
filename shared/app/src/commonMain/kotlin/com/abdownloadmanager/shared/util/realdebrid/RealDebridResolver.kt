package com.abdownloadmanager.shared.util.realdebrid

import kotlinx.coroutines.delay
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

data class RealDebridResolvedLink(
    val directLink: String,
    val fileName: String? = null,
)

/**
 * Converts links that Real-Debrid can handle (magnet links and links of the
 * hosters that Real-Debrid supports) into plain direct download links.
 */
class RealDebridResolver(
    private val settingsStorage: RealDebridSettingsStorage,
    private val client: RealDebridClient,
) {
    fun isMagnetLink(link: String): Boolean {
        return link.trim().startsWith("magnet:?", ignoreCase = true)
    }

    /**
     * Folder links (for example a public Mega folder) are not accepted by the
     * `unrestrict/link` endpoint, they have to go through `unrestrict/folder`.
     */
    fun isFolderLink(link: String): Boolean {
        val url = link.trim().toHttpUrlOrNull() ?: return false
        val host = url.host.lowercase()
        if (host != "mega.nz" && host != "mega.co.nz") return false
        val path = url.encodedPath.lowercase()
        if (path.contains("/folder/") || path.endsWith("/folder")) return true
        return url.fragment.orEmpty().startsWith("F!", ignoreCase = true)
    }

    fun supportsThisLink(link: String): Boolean {
        val settings = settingsStorage.data.value
        if (!settings.isUsable) return false
        val trimmedLink = link.trim()
        if (isMagnetLink(trimmedLink)) return true
        val host = trimmedLink.toHttpUrlOrNull()?.host?.lowercase() ?: return false
        return supportedHosts.any { supportedHost ->
            host == supportedHost || host.endsWith(".$supportedHost")
        }
    }

    /**
     * @return one direct link per file (torrent packs can produce more than one)
     */
    suspend fun resolve(link: String): List<RealDebridResolvedLink> {
        val apiToken = settingsStorage.data.value.apiToken.trim()
        if (apiToken.isBlank()) {
            throw RealDebridException("Real-Debrid API token is not set")
        }
        val trimmedLink = link.trim()
        return when {
            isMagnetLink(trimmedLink) -> resolveMagnet(apiToken, trimmedLink)
            isFolderLink(trimmedLink) -> resolveFolder(
                apiToken = apiToken,
                folderLink = trimmedLink,
                notFoundMessage = "Real-Debrid did not return any file for this folder link. " +
                        "Make sure the folder is shared publicly with its link and key.",
            )

            else -> resolveSingleLink(apiToken, trimmedLink)
        }
    }

    private suspend fun resolveLink(
        apiToken: String,
        link: String,
    ): RealDebridResolvedLink {
        return client.unrestrictLink(apiToken, link).toResolvedLink()
    }

    /**
     * Some hosts (Mega for example) reject their folder links on the
     * `unrestrict/link` endpoint, so if that happens we try again as a folder.
     */
    private suspend fun resolveSingleLink(
        apiToken: String,
        link: String,
    ): List<RealDebridResolvedLink> {
        return try {
            listOf(resolveLink(apiToken, link))
        } catch (e: RealDebridException) {
            if (e.errorCode == HOSTER_UNSUPPORTED_ERROR_CODE) {
                resolveFolder(
                    apiToken = apiToken,
                    folderLink = link,
                    notFoundMessage = "Real-Debrid does not support this link. " +
                            "Folder links must be public and from a supported host.",
                )
            } else {
                throw e
            }
        }
    }

    /**
     * @return one direct link per file inside the folder
     */
    private suspend fun resolveFolder(
        apiToken: String,
        folderLink: String,
        notFoundMessage: String,
    ): List<RealDebridResolvedLink> {
        val fileLinks = client.unrestrictFolder(apiToken, folderLink)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { isFolderLink(it) }
            .filterNot { it.trimEnd('/').equals(folderLink.trimEnd('/'), ignoreCase = true) }
            .distinct()
        if (fileLinks.isEmpty()) {
            throw RealDebridException(notFoundMessage)
        }
        val failures = mutableListOf<Throwable>()
        val resolvedLinks = fileLinks.mapNotNull { fileLink ->
            runCatching { resolveLink(apiToken, fileLink) }
                .onFailure { failures.add(it) }
                .getOrNull()
        }
        if (resolvedLinks.isEmpty()) {
            throw failures.firstOrNull() as? RealDebridException
            ?: RealDebridException(notFoundMessage)
        }
        return resolvedLinks
    }

    private fun RealDebridUnrestrictedLink.toResolvedLink(): RealDebridResolvedLink {
        if (download.isBlank()) {
            throw RealDebridException("Real-Debrid did not return a direct link")
        }
        return RealDebridResolvedLink(
            directLink = download,
            fileName = filename?.takeIf { it.isNotBlank() },
        )
    }

    private suspend fun resolveMagnet(
        apiToken: String,
        magnet: String,
    ): List<RealDebridResolvedLink> {
        val addedTorrent = client.addMagnet(apiToken, magnet)
        val torrentId = addedTorrent.id.takeIf { it.isNotBlank() }
            ?: throw RealDebridException("Real-Debrid did not accept this torrent")

        // the torrent first has to be converted, then we can accept its files
        var info = awaitTorrent(
            torrentId = torrentId,
            apiToken = apiToken,
            timeout = MAGNET_CONVERSION_TIMEOUT,
        ) { torrent ->
            torrent.status != "magnet_conversion"
        }
        if (info.status == "waiting_files_selection") {
            client.selectAllTorrentFiles(apiToken, torrentId)
        }
        if (info.status != "downloaded") {
            info = awaitTorrent(
                torrentId = torrentId,
                apiToken = apiToken,
                timeout = TORRENT_DOWNLOAD_TIMEOUT,
            ) { torrent ->
                torrent.status == "downloaded"
            }
        }

        if (info.links.isEmpty()) {
            throw RealDebridException("Real-Debrid did not return any file for this torrent")
        }
        return info.links.map { torrentLink ->
            resolveLink(apiToken, torrentLink)
        }
    }

    private suspend fun awaitTorrent(
        torrentId: String,
        apiToken: String,
        timeout: kotlin.time.Duration,
        isReady: (RealDebridTorrentInfo) -> Boolean,
    ): RealDebridTorrentInfo {
        val start = TimeSource.Monotonic.markNow()
        var lastStatus = ""
        while (start.elapsedNow() < timeout) {
            val info = client.getTorrentInfo(apiToken, torrentId)
            when (info.status) {
                "magnet_error", "error", "virus", "dead" -> {
                    throw RealDebridException("Real-Debrid could not process this torrent (${info.status})")
                }

                else -> {
                    lastStatus = info.status
                    if (isReady(info)) return info
                }
            }
            delay(POLL_INTERVAL)
        }
        throw RealDebridException(
            "Timed out while waiting for Real-Debrid to process this torrent" +
                    lastStatus.takeIf { it.isNotBlank() }?.let { " (last status: $it)" }.orEmpty()
        )
    }

    companion object {
        private const val HOSTER_UNSUPPORTED_ERROR_CODE = 16
        private val POLL_INTERVAL = 2.seconds
        private val MAGNET_CONVERSION_TIMEOUT = 2.minutes
        private val TORRENT_DOWNLOAD_TIMEOUT = 20.minutes

        /**
         * Hosters that Real-Debrid supports and that make sense to resolve
         * before downloading. Magnet links are handled separately.
         */
        private val supportedHosts = setOf(
            "real-debrid.com",
            "mega.nz",
            "mega.co.nz",
            "1fichier.com",
            "rapidgator.net",
            "uploaded.net",
            "uptobox.com",
            "turbobit.net",
            "hitfile.net",
            "nitroflare.com",
            "filefactory.com",
            "keep2share.cc",
            "k2s.cc",
            "ddownload.com",
            "mediafire.com",
            "usersdrive.com",
            "katfile.com",
            "file.al",
            "filejoker.net",
            "hexupload.net",
            "mixdrop.co",
            "premium.to",
            "racaty.net",
            "sendspace.com",
            "solidfiles.com",
            "streamtape.com",
            "uploadrar.com",
            "uploadbox.com",
            "wetransfer.com",
            "clicknupload.cc",
            "dailyuploads.net",
            "dropapk.to",
            "filespace.com",
            "filefox.cc",
            "streamlare.com",
            "upstream.to",
            "waaw.to",
            "zippyshare.com",
        )
    }
}
