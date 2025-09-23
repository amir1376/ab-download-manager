package com.abdownloadmanager.shared.downloaderinui.http.add

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.ui.configurable.item.FileChecksumConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.IntConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.SpeedLimitConfigurable
import com.abdownloadmanager.shared.ui.configurable.item.StringConfigurable
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.ThreadCountLimitation
import com.abdownloadmanager.shared.utils.FileChecksum
import com.abdownloadmanager.shared.utils.convertPositiveSizeToHumanReadable
import com.abdownloadmanager.shared.utils.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.downloaderinui.http.applyToHttpDownload
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.createMutableStateFlowFromStateFlow
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.flow.mapTwoWayStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HttpNewDownloadInputs(
    downloadUiChecker: HttpDownloadUiChecker,
    scope: CoroutineScope,
    private val sizeAndSpeedUnitProvider: SizeAndSpeedUnitProvider
) : NewDownloadInputs<
        HttpDownloadItem,
        HttpDownloadCredentials,
        HttpResponseInfo,
        HttpLinkChecker
        >(
    downloadUiChecker
) {
    private val length: StateFlow<Long?> = downloadUiChecker.length

    //extra settings
    private var threadCount = MutableStateFlow(null as Int?)
    private var speedLimit = MutableStateFlow(0L)
    private var fileChecksum = MutableStateFlow(null as FileChecksum?)
    override val downloadItem: StateFlow<HttpDownloadItem> = combineStateFlows(
        this.credentials,
        this.folder,
        this.name,
        this.length,
        this.speedLimit,
        this.threadCount,
        this.fileChecksum,
    ) {
            credentials,
            folder,
            name,
            length,
            speedLimit,
            threadCount,
            fileChecksum,
        ->
        HttpDownloadItem(
            id = -1,
            folder = folder,
            name = name,
            link = credentials.link,
            contentLength = length ?: IDownloadItem.LENGTH_UNKNOWN,
            dateAdded = openedTime,
            startTime = null,
            completeTime = null,
            status = DownloadStatus.Added,
            preferredConnectionCount = threadCount,
            speedLimit = speedLimit,
            fileChecksum = fileChecksum?.toString()
        ).withCredentials(credentials)
    }

    override fun applyHostSettingsToExtraConfig(extraConfig: PerHostSettingsItem) {
        extraConfig.applyToHttpDownload(
            setUsername = { setCredentials(credentials.value.copy(username = it)) },
            setPassword = { setCredentials(credentials.value.copy(password = it)) },
            setUserAgent = { setCredentials(credentials.value.copy(userAgent = it)) },
            setThreadCount = { threadCount.value = it },
            setSpeedLimit = { speedLimit.value = it }
        )
    }

    override val configurableList = listOf(
        SpeedLimitConfigurable(
            Res.string.download_item_settings_speed_limit.asStringSource(),
            Res.string.download_item_settings_speed_limit_description.asStringSource(),
            backedBy = speedLimit,
            describe = {
                if (it == 0L) Res.string.unlimited.asStringSource()
                else convertPositiveSpeedToHumanReadable(
                    it, sizeAndSpeedUnitProvider.speedUnit.value
                ).asStringSource()
            }
        ),
        FileChecksumConfigurable(
            Res.string.download_item_settings_file_checksum.asStringSource(),
            Res.string.download_item_settings_file_checksum_description.asStringSource(),
            backedBy = fileChecksum,
            describe = { "".asStringSource() }
        ),
        IntConfigurable(
            Res.string.settings_download_thread_count.asStringSource(),
            Res.string.settings_download_thread_count_description.asStringSource(),
            backedBy = threadCount.mapTwoWayStateFlow(
                map = {
                    it ?: 0
                },
                unMap = {
                    it.takeIf { it >= 1 }
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
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.username.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(username = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                "".asStringSource()
            }
        ),
        StringConfigurable(
            Res.string.password.asStringSource(),
            Res.string.download_item_settings_password_description.asStringSource(),
            backedBy = createMutableStateFlowFromStateFlow(
                flow = credentials.mapStateFlow {
                    it.password.orEmpty()
                },
                updater = {
                    setCredentials(credentials.value.copy(password = it.takeIf { it.isNotBlank() }))
                }, scope
            ),
            describe = {
                "".asStringSource()
            }
        ),
    )

    override val lengthStringFlow: StateFlow<StringSource> = downloadUiChecker.responseInfo.mapStateFlow {
        val fileInfo = it ?: return@mapStateFlow Res.string.unknown.asStringSource()
        fileInfo.totalLength?.let {
            convertPositiveSizeToHumanReadable(it, sizeAndSpeedUnitProvider.sizeUnit.value)
        }.takeIf {
            // this is a length of a html page (error)
            fileInfo.isSuccessFul
        } ?: Res.string.unknown.asStringSource()
    }
}
