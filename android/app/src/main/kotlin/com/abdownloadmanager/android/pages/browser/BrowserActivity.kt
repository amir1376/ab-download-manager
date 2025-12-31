package com.abdownloadmanager.android.pages.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.abdownloadmanager.android.util.AndroidIntentUtils
import com.abdownloadmanager.android.util.activity.ABDMActivity
import com.abdownloadmanager.android.util.activity.HandleActivityEffects
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.arkivanov.decompose.defaultComponentContext
import ir.amirab.util.HttpUrlUtils
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class BrowserActivity : ABDMActivity() {
    val json: Json by inject()
    val component by lazy {
        BrowserComponent(defaultComponentContext(), applicationContext, json)
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
                data = url?.let { Uri.parse(it) }
            }
        }
    }
}

