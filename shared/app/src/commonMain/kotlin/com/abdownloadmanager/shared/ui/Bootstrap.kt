package com.abdownloadmanager.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.ui.configurable.ConfigurableRendererRegistry
import com.abdownloadmanager.shared.ui.configurable.LocalConfigurationRendererRegistry
import com.abdownloadmanager.shared.util.LocalUseRelativeDateTime
import com.abdownloadmanager.shared.util.ProvideSizeAndSpeedUnit
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.LocalIconFromUriResolver


@Composable
fun ProvideCommonSettings(
    appSettings: BaseAppSettingsStorage,
    iconProvider: IIconResolver,
    configurableRendererRegistry: ConfigurableRendererRegistry,
    content: @Composable () -> Unit,
) {
    val useNativeDateTime by appSettings.useRelativeDateTime.collectAsState()
    CompositionLocalProvider(
        LocalUseRelativeDateTime provides useNativeDateTime,
        LocalIconFromUriResolver provides iconProvider,
        LocalConfigurationRendererRegistry provides configurableRendererRegistry,
    ) {
        content()
    }
}

@Composable
fun ProvideSizeUnits(
    appRepository: BaseAppRepository,
    content: @Composable () -> Unit,
) {
    ProvideSizeAndSpeedUnit(
        sizeUnitConfig = appRepository.sizeUnit.collectAsState().value,
        speedUnitConfig = appRepository.speedUnit.collectAsState().value,
        content = content
    )
}
