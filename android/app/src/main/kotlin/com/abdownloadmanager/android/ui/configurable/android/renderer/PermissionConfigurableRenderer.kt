package com.abdownloadmanager.android.ui.configurable.android.renderer

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.abdownloadmanager.android.pages.onboarding.permissions.AppPermissionState
import com.abdownloadmanager.android.pages.onboarding.permissions.openApplicationDetailsInSettings
import com.abdownloadmanager.android.pages.onboarding.permissions.rememberAppPermissionState
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.android.ui.configurable.android.item.PermissionConfigurable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.myColors
import ir.amirab.util.compose.asStringSource

object PermissionConfigurableRenderer : ConfigurableRenderer<PermissionConfigurable> {
    @Composable
    override fun RenderConfigurable(
        configurable: PermissionConfigurable,
        configurableUiProps: ConfigurableUiProps
    ) {
        val permission by configurable.stateFlow.collectAsState()

        var rejectedOnce by remember(permission) { mutableStateOf(false) }

        val permissionState = rememberAppPermissionState(permission) { result ->
            if (!result) {
                rejectedOnce = true
            }
        }
        val userProbablyPressedOnDontAskAgain = !permissionState.requiresRational && rejectedOnce

        RenderPermissionConfigurable(
            cfg = configurable,
            configurableUiProps = configurableUiProps,
            permissionState = permissionState,
            userProbablyPressedOnDontAskAgain = userProbablyPressedOnDontAskAgain,
        )
    }

    @Composable
    fun RenderPermissionConfigurable(
        cfg: PermissionConfigurable,
        configurableUiProps: ConfigurableUiProps,
        permissionState: AppPermissionState,
        userProbablyPressedOnDontAskAgain: Boolean,
    ) {
        val activity = requireNotNull(LocalActivity.current) {
            "Activity is required to open app details"
        }
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    if (userProbablyPressedOnDontAskAgain) {
                        openApplicationDetailsInSettings(activity)
                    } else {
                        permissionState.launchRequest()
                    }
                }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(
                    cfg = cfg,
                    describeContent = if (permissionState.isGranted) {
                        Res.string.permission_granted
                    } else {
                        Res.string.permission_not_granted
                    }.asStringSource().rememberString(),
                    describeWrapper = { content ->
                        val contentColor =
                            if (permissionState.isGranted) myColors.success else myColors.warning
                        CompositionLocalProvider(
                            LocalContentColor provides contentColor
                        ) {
                            content()
                        }
                    }
                )
            },
            value = {
                NextIcon()
            }
        )
    }
}
