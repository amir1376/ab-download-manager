package com.abdownloadmanager.shared.downloaderinui.http.edit

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.http.HttpCredentialsToItemMapper
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import com.abdownloadmanager.shared.ui.configurable.item.FileChecksumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.IntConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.ThreadCountLimitation
import com.abdownloadmanager.shared.util.FileChecksum
import com.abdownloadmanager.shared.util.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.util.convertPositiveSpeedToHumanReadable
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HttpEditDownloadInputs(
    currentDownloadItem: MutableStateFlow<HttpDownloadItem>,
    editedDownloadItem: MutableStateFlow<HttpDownloadItem>,
    val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider,
    mapper: HttpCredentialsToItemMapper,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
    linkCheckerFactory: LinkCheckerFactory<HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker>,
    editDownloadCheckerFactory: EditDownloadCheckerFactory<HttpDownloadItem, HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker>
) : EditDownloadInputs<HttpDownloadItem, HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker, HttpCredentialsToItemMapper>(
    currentDownloadItem = currentDownloadItem,
    editedDownloadItem = editedDownloadItem,
    mapper = mapper,
    scope = scope,
    conflictDetector = conflictDetector,
    linkCheckerFactory = linkCheckerFactory,
    editDownloadCheckerFactory = editDownloadCheckerFactory,
) {

    override val configurableList = listOf(
        SpeedLimitConfigurable(
            Res.string.download_item_settings_speed_limit.asStringSource(),
            Res.string.download_item_settings_speed_limit_description.asStringSource(),
            backedBy = editedDownloadItem.mapTwoWayStateFlow(
                map = {
                    it.speedLimit
                },
                unMap = {
                    copy(speedLimit = it)
                }
            ),
            describe = {
                if (it == 0L) Res.string.unlimited.asStringSource()
                else convertPositiveSpeedToHumanReadable(it, sizeAndSpeedUnitProvider.speedUnit.value).asStringSource()
            }
        ),
        FileChecksumConfigurable(
            Res.string.download_item_settings_file_checksum.asStringSource(),
            Res.string.download_item_settings_file_checksum_description.asStringSource(),
            backedBy = editedDownloadItem.mapTwoWayStateFlow(
                map = {
                    it.fileChecksum?.let {
                        runCatching {
                            FileChecksum.Companion.fromString(it)
                        }.onFailure {
                            println(it.printStackTrace())
                        }.getOrNull()
                    }
                },
                unMap = {
                    copy(fileChecksum = it?.toString())
                }
            ),
            describe = { "".asStringSource() }
        ),
        IntConfigurable(
            Res.string.settings_download_thread_count.asStringSource(),
            Res.string.settings_download_thread_count_description.asStringSource(),
            backedBy = editedDownloadItem.mapTwoWayStateFlow(
                map = {
                    it.preferredConnectionCount ?: 0
                },
                unMap = {
                    copy(
                        preferredConnectionCount = it.takeIf { it >= 1 }
                    )
                }
            ),
            range = 0..ThreadCountLimitation.MAX_ALLOWED_THREAD_COUNT,
            describe = {
                if (it == 0) Res.string.use_global_settings.asStringSource()
                else Res.string.download_item_settings_thread_count_describe
                    .asStringSourceWithARgs(
                        Res.string.download_item_settings_thread_count_describe_createArgs(
                            count = it.toString()
                        )
                    )
            }
        ),
        StringConfigurable(
            Res.string.username.asStringSource(),
            Res.string.download_item_settings_username_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.username.orEmpty()
                },
                unMap = {
                    copy(username = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.password.asStringSource(),
            Res.string.download_item_settings_password_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.password.orEmpty()
                },
                unMap = {
                    copy(password = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.download_item_settings_user_agent.asStringSource(),
            Res.string.download_item_settings_user_agent_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.userAgent.orEmpty()
                },
                unMap = {
                    copy(userAgent = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.download_item_settings_download_page.asStringSource(),
            Res.string.download_item_settings_download_page_description.asStringSource(),
            backedBy = credentials.mapTwoWayStateFlow(
                map = {
                    it.downloadPage.orEmpty()
                },
                unMap = {
                    copy(downloadPage = it.takeIf { it.isNotEmpty() })
                }
            ),
            describe = {
                "".asStringSource()
            }
        ),
    )
    val length = linkChecker.length
    override val downloadJobConfig: MutableStateFlow<DownloadJobExtraConfig?> = MutableStateFlow(null)

    private fun HttpDownloadItem.applyOurChanges(edited: HttpDownloadItem) {
        // we don't change some of these properties, so I commented them

        link = edited.link
        headers = edited.headers
        username = edited.username
        password = edited.password
        downloadPage = edited.downloadPage
        userAgent = edited.userAgent

//        id = edited.id
        folder = edited.folder
        name = edited.name

        contentLength = edited.contentLength
        serverETag = edited.serverETag

//        dateAdded = edited.dateAdded
//        startTime = edited.startTime
//        completeTime = edited.completeTime
//        status = edited.status
        preferredConnectionCount = edited.preferredConnectionCount
        speedLimit = edited.speedLimit

        fileChecksum = edited.fileChecksum
    }

    override fun applyEditedItemTo(item: HttpDownloadItem) {
        val edited = editedDownloadItem.value
        item.applyOurChanges(edited)
    }

    init {
        length.onEach {
            scheduleRefresh(alsoRecheckLink = false)
        }.launchIn(scope)
    }

    override val lengthStringFlow: StateFlow<StringSource> = linkChecker.responseInfo.mapStateFlow {
        val fileInfo = it ?: return@mapStateFlow Res.string.unknown.asStringSource()
        fileInfo.totalLength?.let {
            convertPositiveSizeToHumanReadable(it, sizeAndSpeedUnitProvider.sizeUnit.value)
        }.takeIf {
            // this is a length of a html page (error)
            fileInfo.isSuccessFul
        } ?: Res.string.unknown.asStringSource()
    }
}
