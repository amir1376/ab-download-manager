package com.abdownloadmanager.android.ui.configurable.comon.renderer

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import arrow.optics.copy
import com.abdownloadmanager.android.pages.onboarding.permissions.AppPermissionState
import com.abdownloadmanager.android.pages.onboarding.permissions.rememberAppPermissionState
import com.abdownloadmanager.android.ui.configurable.ConfigTemplate
import com.abdownloadmanager.android.ui.configurable.NextIcon
import com.abdownloadmanager.android.ui.configurable.TitleAndDescription
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRenderer
import com.abdownloadmanager.shared.ui.configurable.ConfigurableUiProps
import com.abdownloadmanager.android.ui.configurable.comon.item.PermissionConfigurable
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.resources.MyStringResource
import ir.amirab.util.compose.resources.myStringResource

object PermissionConfigurableRenderer : ConfigurableRenderer<PermissionConfigurable> {
    @Composable
    override fun RenderConfigurable(
        configurable: PermissionConfigurable,
        configurableUiProps: ConfigurableUiProps
    ) {
        val permission by configurable.stateFlow.collectAsState()
        val permissionState = rememberAppPermissionState(permission) { result ->

        }

        RenderPermissionConfigurable(
            cfg = configurable,
            configurableUiProps = configurableUiProps,
            permissionState = permissionState
        )
    }

    @Composable
    fun RenderPermissionConfigurable(
        cfg: PermissionConfigurable,
        configurableUiProps: ConfigurableUiProps,
        permissionState: AppPermissionState,
    ) {
        ConfigTemplate(
            modifier = configurableUiProps.modifier
                .clickable {
                    permissionState.launchRequest()
                }
                .padding(configurableUiProps.itemPaddingValues),
            title = {
                TitleAndDescription(
                    cfg = cfg,
                    describe = true,
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