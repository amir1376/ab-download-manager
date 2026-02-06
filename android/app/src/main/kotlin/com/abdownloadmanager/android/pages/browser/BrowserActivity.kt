package com.abdownloadmanager.android.pages.browser

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.abdownloadmanager.android.util.AndroidIntentUtils
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.arkivanov.decompose.defaultComponentContext
import ir.amirab.util.HttpUrlUtils
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import androidx.core.net.toUri
import com.abdownloadmanager.android.storage.BrowserBookmarksStorage

class BrowserActivity : ABDMActivity() {
    private val browserBookmarksStorage: BrowserBookmarksStorage by inject()
    private val json: Json by inject()
    val component by lazy {
        BrowserComponent(
            componentContext = defaultComponentContext(),
            context = applicationContext,
            json = json,
            browserBookmarksStorage = browserBookmarksStorage,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setABDMContent {
            HandleEffects(component) {
                when (it) {
                    is BrowserComponent.Effects.StartActivity -> {
                        startActivity(it.intent)
                    }

                    is BrowserComponent.Effects.ShareText -> {
                        AndroidIntentUtils.shareText(this, it.text)
                    }
                }
            }
            BrowserPage(component)
        }
    }

    override fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val url = intent.data?.toString()
            if (url != null && HttpUrlUtils.isValidUrl(url)) {
                component.newTab(url)
            }
        }
    }

    companion object {
        fun createIntent(
            context: Context,
            url: String? = null,
        ): Intent {
            return Intent(context, BrowserActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = url?.toUri()
            }
        }

        object Launcher : KoinComponent {
            private val context: Context by inject()
            private val browserLauncherActivityAliasName by lazy {
                "com.abdownloadmanager.browser.BrowserIconInLauncher"
            }

            fun setEnabled(
                isEnabled: Boolean,
            ) {
                val newState = if (isEnabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                context.packageManager.setComponentEnabledSetting(
                    ComponentName(context, browserLauncherActivityAliasName),
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }

            fun isEnabled(): Boolean {
                return context.packageManager.getComponentEnabledSetting(
                    ComponentName(context, browserLauncherActivityAliasName),
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
        }
    }
}

