package com.abdownloadmanager.shared.settings

import com.abdownloadmanager.shared.ui.configurable.ConfigurableGroup
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow

abstract class BaseSettingsComponent(
    context: ComponentContext
) : BaseComponent(
    context
),
    ContainsEffects<BaseSettingsComponent.Effects> by supportEffects() {
    abstract val configurables: StateFlow<List<ConfigurableGroup>>

    sealed interface Effects {
        interface Platform : Effects
    }
}
