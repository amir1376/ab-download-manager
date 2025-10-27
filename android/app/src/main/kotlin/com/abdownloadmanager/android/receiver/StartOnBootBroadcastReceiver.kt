package com.abdownloadmanager.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StartOnBootBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val appManager: ABDMAppManager by inject()
    private val scope: CoroutineScope by inject()
    private val appSettingStorage: BaseAppSettingsStorage by inject()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (appManager.canStartDownloadEngine() && appSettingStorage.autoStartOnBoot.value) {
                scope.launch {
                    appManager.startDownloadSystem()
                    appManager.startOurService()
                }
            }
        }
    }
}
