package com.abdownloadmanager.android.pages.add

import android.content.Intent
import android.os.Bundle
import arrow.core.firstOrNone
import arrow.core.getOrElse
import com.abdownloadmanager.android.pages.add.multiple.AddMultiDownloadActivity
import com.abdownloadmanager.android.pages.add.single.AddSingleDownloadActivity
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.ui.MainActivity
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadConfig
import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions
import com.abdownloadmanager.shared.util.extractors.linkextractor.DownloadCredentialFromStringExtractor
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class AddDownloadActivity : ABDMActivity() {
    val json: Json by inject()
    val permissionManager: PermissionManager by inject()
    private fun createDownloaderInUiProps(
        credentials: IDownloadCredentials
    ): AddDownloadCredentialsInUiProps {
        return AddDownloadCredentialsInUiProps(
            credentials,
            AddDownloadCredentialsInUiProps.Configs(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!permissionManager.isReady()) {
            // user not opened the app at least once. we must redirect it to the permission page first
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        val credentials = getDownloadCredentialsFromIntent(intent)
        val intent = if (credentials.size > 1) {
            AddMultiDownloadActivity.createIntent(
                this,
                AddDownloadConfig.MultipleAddConfig(
                    newDownloads = credentials.map(::createDownloaderInUiProps),
                    importOptions = ImportOptions(),
                ),
                json = json,
            )
        } else {
            AddSingleDownloadActivity.createIntent(
                this,
                AddDownloadConfig.SingleAddConfig(
                    newDownload = credentials
                        .firstOrNone()
                        .getOrElse { HttpDownloadCredentials("") }
                        .let(::createDownloaderInUiProps),
                    importOptions = ImportOptions(),
                ),
                json = json,
            )
        }
        startActivity(intent)
        finish()
    }

    private fun getDownloadCredentialsFromIntent(intent: Intent): List<IDownloadCredentials> {
        val links = when (intent.action) {
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            }

            else -> {
                // action view etc...
                intent.data?.toString().orEmpty()
            }
        }
        return DownloadCredentialFromStringExtractor
            .extract(links)
            .distinctBy { it.link }
    }
}
