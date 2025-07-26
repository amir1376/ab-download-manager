package com.abdownloadmanager.desktop.actions.onevennts

import com.abdownloadmanager.desktop.PowerActionManager
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import com.abdownloadmanager.desktop.pages.poweractionalert.PowerActionComponent
import com.abdownloadmanager.desktop.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.utils.ondownloadcompletion.OnDownloadCompletionAction
import com.abdownloadmanager.shared.utils.ondownloadcompletion.OnDownloadCompletionActionProvider
import ir.amirab.downloader.downloaditem.DownloadItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class DesktopOnDownloadCompletionActionProvider(
    private val extraDownloadSettingsStorage: ExtraDownloadSettingsStorage,
) : OnDownloadCompletionActionProvider, KoinComponent {
    // TODO: BUG
    // at the moment if I move this to constructor the DI halts
    // probably due to Circular Dependency Exception
    // I need to redesign the dependency graph to prevent these sorts of issues!
    private val powerActionManager: PowerActionManager by inject()

    override suspend fun getOnDownloadCompletionAction(downloadItem: DownloadItem): List<OnDownloadCompletionAction> {
        val downloadId = downloadItem.id
        val extraDownloadItemSettings = extraDownloadSettingsStorage.getExtraDownloadItemSettings(downloadId)
        return buildList {
            extraDownloadItemSettings.getPowerActionConfigOnFinish()?.let {
                add(PowerActionOnDownloadFinish(powerActionManager, it))
            }
            add(
                CleanExtraSettingsOnDownloadFinish(extraDownloadSettingsStorage)
            )
        }
    }
}

class PowerActionOnDownloadFinish(
    val powerActionManager: PowerActionManager,
    val powerActionConfig: PowerActionConfig,
) : OnDownloadCompletionAction {
    override suspend fun onDownloadCompleted(downloadItem: DownloadItem) {
        powerActionManager.initiatePowerAction(
            powerActionConfig,
            PowerActionComponent.PowerActionReason.DownloadFinished,
        )
    }
}

class CleanExtraSettingsOnDownloadFinish(
    private val storage: ExtraDownloadSettingsStorage
) : OnDownloadCompletionAction {
    override suspend fun onDownloadCompleted(downloadItem: DownloadItem) {
        storage.deleteExtraDownloadItemSettings(downloadItem.id)
    }
}
