package com.abdownloadmanager.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.shared.storage.appsettings.BaseAppSettingsStorage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StartOnBootBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val appManager: ABDMAppManager by inject()
    private val appSettingStorage: BaseAppSettingsStorage by inject()
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (appSettingStorage.autoStartOnBoot.value) {
                appManager.bootDownloadSystemAndService()
            }
        }
    }
}
