package com.abdownloadmanager.android.pages.perhostsettings

import com.abdownloadmanager.shared.pages.perhostsettings.BasePerHostSettingsComponent
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class AndroidPerHostSettingsComponent(
    ctx: ComponentContext,
    perHostSettingsManager: PerHostSettingsManager,
    appRepository: BaseAppRepository,
    appScope: CoroutineScope,
    closeRequested: () -> Unit,
) : BasePerHostSettingsComponent(
    ctx = ctx,
    perHostSettingsManager = perHostSettingsManager,
    appRepository = appRepository,
    appScope = appScope,
    closeRequested = closeRequested,
) {
    @Serializable
    data class Config(
        override val openedHost: String?
    ) : BasePerHostSettingsComponent.Config

    sealed interface Effects : BasePerHostSettingsComponent.Effects.Platform {
    }

    fun reset() {
        editedPerHostSettings.value = savedPerHostSettings.value
        onIdSelected(null)
    }

    fun saveAndReturn() {
        save()
        onIdSelected(null)
    }
}
