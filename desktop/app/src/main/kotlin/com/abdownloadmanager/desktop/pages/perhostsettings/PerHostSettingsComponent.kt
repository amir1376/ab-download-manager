package com.abdownloadmanager.desktop.pages.perhostsettings

import arrow.core.prependTo
import com.abdownloadmanager.desktop.pages.settings.ThreadCountLimitation
import com.abdownloadmanager.desktop.pages.settings.configurable.IntConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.SpeedLimitConfigurable
import com.abdownloadmanager.desktop.pages.settings.configurable.StringConfigurable
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.utils.configurable.ConfigurableGroup
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.utils.BaseComponent
import com.abdownloadmanager.shared.utils.convertPositiveSpeedToHumanReadable
import com.abdownloadmanager.shared.utils.mvi.ContainsEffects
import com.abdownloadmanager.shared.utils.mvi.supportEffects
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsManager
import com.arkivanov.decompose.ComponentContext
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.compose.asStringSourceWithARgs
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapTwoWayStateFlow
import ir.amirab.util.flow.onEachLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PerHostSettingsItemWithId(
    val perHostSettingsItem: PerHostSettingsItem,
    val id: String = UUID.randomUUID().toString(),
)

data class PerHostSettingsConfigurableWithId(
    val configurableGroups: List<ConfigurableGroup>,
    val id: String,
)

class PerHostSettingsComponent(
    ctx: ComponentContext,
    private val perHostSettingsManager: PerHostSettingsManager,
    private val appRepository: AppRepository,
    private val appScope: CoroutineScope,
    private val closeRequested: () -> Unit,
) : BaseComponent(ctx),
    ContainsEffects<PerHostSettingsComponentEffects> by supportEffects() {
    val editedPerHostSettings: MutableStateFlow<List<PerHostSettingsItemWithId>>
    val savedPerHostSettings: MutableStateFlow<List<PerHostSettingsItemWithId>>

    init {
        val data = getStorageDataWithUnitqueIds()
        editedPerHostSettings = MutableStateFlow(data)
        savedPerHostSettings = MutableStateFlow(data)
    }

    val selectedId = MutableStateFlow(null as String?)
    val canSave = combineStateFlows(
        savedPerHostSettings, editedPerHostSettings
    ) { a, b ->
        a != b
    }

    fun onHostSelected(host: String) {
        selectedId.value = editedPerHostSettings.value.find {
            it.perHostSettingsItem.host == host
        }?.id
    }

    fun onIdSelected(id: String) {
        selectedId.value = id
    }

    fun bringToFront() {
        sendEffect(PerHostSettingsComponentEffects.BringToFront)
    }

    fun load() {
        val data = getStorageDataWithUnitqueIds()
        editedPerHostSettings.value = data
        savedPerHostSettings.value = data
    }

    private fun getStorageDataWithUnitqueIds(): List<PerHostSettingsItemWithId> {
        return perHostSettingsManager.getStorageData().map {
            PerHostSettingsItemWithId(perHostSettingsItem = it)
        }
    }

    fun save() {
        appScope.launch {
            perHostSettingsManager.setSettingsData(editedPerHostSettings.value.map {
                it.perHostSettingsItem
            })
        }
    }

    val selectedItemConfigurableList: MutableStateFlow<PerHostSettingsConfigurableWithId?> = MutableStateFlow(null)

    init {
        selectedId.onEachLatest { selectedId ->
            coroutineScope {
                selectedItemConfigurableList.value = run {
                    if (selectedId != null) {
                        val configWithId = editedPerHostSettings.value
                            .find { it.id == selectedId } ?: return@run null
                        val state = MutableStateFlow(configWithId.perHostSettingsItem)
                        launch {
                            performUpdatesFromStateFlow(state)
                        }
                        PerHostSettingsConfigurableWithId(
                            id = selectedId,
                            configurableGroups = createConfigurableGroupForItem(state)
                        )
                    } else {
                        null
                    }
                }
            }
        }.launchIn(scope)
    }

    private suspend fun performUpdatesFromStateFlow(
        state: MutableStateFlow<PerHostSettingsItem>
    ) {
        state.collect { newUpdate ->
            editedPerHostSettings.value = editedPerHostSettings.value.map {
                if (it.id == selectedId.value) {
                    it.copy(perHostSettingsItem = newUpdate)
                } else {
                    it
                }
            }
        }
    }

    private fun createConfigurableGroupForItem(
        state: MutableStateFlow<PerHostSettingsItem>
    ): List<ConfigurableGroup> {
        return listOf(
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    StringConfigurable(
                        title = Res.string.settings_per_host_settings_host.asStringSource(),
                        description = Res.string.settings_per_host_settings_host_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.host
                            },
                            unMap = {
                                copy(host = it)
                            }
                        ),
                        describe = {
                            "".asStringSource()
                        }
                    ),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    SpeedLimitConfigurable(
                        title = Res.string.download_item_settings_speed_limit.asStringSource(),
                        description = Res.string.download_item_settings_speed_limit_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.speedLimit ?: 0
                            },
                            unMap = {
                                copy(speedLimit = it.takeIf { it != 0L })
                            }
                        ),
                        describe = {
                            if (it == 0L) Res.string.unlimited.asStringSource()
                            else convertPositiveSpeedToHumanReadable(it, appRepository.speedUnit.value).asStringSource()
                        }
                    ),
                    IntConfigurable(
                        title = Res.string.download_item_settings_thread_count.asStringSource(),
                        description = Res.string.download_item_settings_thread_count_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.threadCount ?: 0
                            },
                            unMap = {
                                copy(threadCount = it.takeIf { it != 0 })
                            }
                        ),
                        range = 0..ThreadCountLimitation.MAX_ALLOWED_THREAD_COUNT,
                        describe = {
                            if (it == 0) Res.string.use_global_settings.asStringSource()
                            else Res.string.download_item_settings_thread_count_describe
                                .asStringSourceWithARgs(
                                    Res.string.download_item_settings_thread_count_describe_createArgs(
                                        count = it.toString()
                                    )
                                )
                        }
                    ),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    StringConfigurable(
                        title = Res.string.username.asStringSource(),
                        description = Res.string.download_item_settings_username_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.username.orEmpty()
                            },
                            unMap = {
                                copy(username = it.takeIf { it.isNotBlank() })
                            }
                        ),
                        describe = {
                            "".asStringSource()
                        }
                    ),
                    StringConfigurable(
                        title = Res.string.password.asStringSource(),
                        description = Res.string.download_item_settings_password_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.password.orEmpty()
                            },
                            unMap = {
                                copy(password = it.takeIf { it.isNotBlank() })
                            }
                        ),
                        describe = {
                            "".asStringSource()
                        }
                    ),
                )
            ),
            ConfigurableGroup(
                nestedConfigurable = listOf(
                    StringConfigurable(
                        title = Res.string.settings_default_user_agent.asStringSource(),
                        description = Res.string.settings_default_user_agent_description.asStringSource(),
                        backedBy = state.mapTwoWayStateFlow(
                            map = {
                                it.userAgent.orEmpty()
                            },
                            unMap = {
                                copy(userAgent = it.takeIf { it.isNotBlank() })
                            }
                        ),
                        describe = {
                            "".asStringSource()
                        }
                    ),
                )
            )
        )
    }


    fun onRequestAddNewHostSettingsItem() {
        val perHostSettingsItemWithId = PerHostSettingsItemWithId(
            PerHostSettingsItem("")
        )
        editedPerHostSettings.update {
            perHostSettingsItemWithId.prependTo(it)
        }
        selectedId.value = perHostSettingsItemWithId.id
    }

    fun onRequestDeleteConfig(id: String) {
        val index = editedPerHostSettings.value.indexOfFirst {
            it.id == id
        }
        editedPerHostSettings.update {
            it.filterNot { item ->
                item.id == id
            }
        }
        selectedId.update { selectedId ->
            if (selectedId == id) {
                val editedConfigs = this.editedPerHostSettings.value
                runCatching {
                    index.coerceIn(editedConfigs.indices)
                }.getOrNull()?.let {
                    editedConfigs.getOrNull(it)?.id
                }
            } else {
                selectedId
            }
        }
    }

    fun close() {
        closeRequested()
    }

    fun saveAndClose() {
        save()
        close()
    }

    class Config(
        val openedHost: String?
    )
}

sealed interface PerHostSettingsComponentEffects {
    data object BringToFront : PerHostSettingsComponentEffects
}
