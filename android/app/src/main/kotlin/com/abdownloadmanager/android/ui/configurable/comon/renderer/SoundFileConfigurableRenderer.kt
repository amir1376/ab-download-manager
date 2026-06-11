package com.abdownloadmanager.android.ui.configurable.comon.renderer

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.ui.configurable.item.SoundFileConfigurable
import ir.amirab.util.compose.asStringSource

object SoundFileConfigurableRenderer : ConfigurableRenderer<SoundFileConfigurable> {
    @Composable
    override fun RenderConfigurable(
        configurable: SoundFileConfigurable,
        configurableUiProps: ConfigurableUiProps,
    ) {
        RenderSoundFileConfig(configurable, configurableUiProps)
    }

    @Composable
    private fun RenderSoundFileConfig(
        cfg: SoundFileConfigurable,
        configurableUiProps: ConfigurableUiProps,
    ) {
        val context = LocalContext.current
        val value by cfg.stateFlow.collectAsState()
        val setValue = cfg::set

        // Compute a display-friendly ringtone name for the current value.
        // For content URIs we query the system ringtone title;
        // for file paths and blank values we fall back to the configurable's describe function.
        val describedStringSource = remember(value, context) {
            when {
                value.isBlank() -> cfg.describe(value)
                value.startsWith("content://", ignoreCase = true)
                    || value.startsWith("android.resource://", ignoreCase = true)
                -> {
                    runCatching {
                        val uri = Uri.parse(value)
                        val ringtone = RingtoneManager.getRingtone(context, uri)
                        ringtone?.getTitle(context)
                    }.getOrNull()
                        ?.asStringSource()
                        ?: cfg.describe(value)
                }

                else -> cfg.describe(value)
            }
        }
        val displayText = describedStringSource.rememberString()
        val titleText = cfg.title.rememberString()

        val ringtonePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val pickedUri =
                        result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    setValue(pickedUri?.toString() ?: "")
                }
                // User cancelled or pressed back — no change
            }
        }

        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_TYPE,
                            RingtoneManager.TYPE_NOTIFICATION,
                        )
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_TITLE,
                            titleText,
                        )
                        // Pre-populate the picker with the currently selected ringtone
                        // when the stored value is a parsable URI.
                        if (value.isNotBlank()
                            && (value.startsWith("content://", ignoreCase = true)
                                || value.startsWith(
                                    "android.resource://",
                                    ignoreCase = true,
                                ))
                        ) {
                            runCatching {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    Uri.parse(value),
                                )
                            }
                        }
                    }
                    ringtonePickerLauncher.launch(intent)
                }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(
                    cfg = cfg,
                    describe = true,
                    describeContent = displayText,
                )
            },
            value = {
                NextIcon()
            },
        )
    }
}
