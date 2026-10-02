package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import com.abdownloadmanager.shared.util.ui.MyColors
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.guardedEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class ThemeManager(
    private val scope: CoroutineScope,
    private val appSettings: ThemeSettingsStorage,
    private val osThemeDetector: ISystemThemeDetector,
) {
    companion object {
        val defaultPack = DefaultThemes.getDefaultPack()
        val DEFAULT_THEME_PACK_ID = defaultPack.id
    }

    private val _availablePacks = MutableStateFlow(emptyList<ThemePack>())
    val availablePacks = _availablePacks.asStateFlow()

    private fun getPackById(packId: String): ThemePack? {
        return availablePacks.value.find { it.id == packId }
    }

    val selectableThemePacks = availablePacks.mapStateFlow { packs ->
        packs.map { it.toThemePackInfo() }
    }

    val selectableDarkModes: List<DarkModePreference> = buildList {
        add(DarkModePreference.System)
        add(DarkModePreference.Dark)
        add(DarkModePreference.Light)
    }

    val currentDarkMode = appSettings.darkMode.mapStateFlow { name ->
        DarkModePreference.entries.find { it.name == name } ?: DarkModePreference.System
    }

    val currentThemePackInfo = combineStateFlows(
        appSettings.themePack, selectableThemePacks
    ) { packId, packs ->
        packs.find { it.id == packId }
            ?: packs.find { it.id == DEFAULT_THEME_PACK_ID }
            ?: packs.first()
    }

    private var osDarkModeFlow = MutableStateFlow(true)

    val currentThemeColor: StateFlow<MyColors> = combineStateFlows(
        appSettings.themePack,
        appSettings.darkMode,
        osDarkModeFlow,
        availablePacks,
    ) { packId, darkModeName, osThemeIsDark, packs ->
        val darkMode = DarkModePreference.entries.find { it.name == darkModeName }
            ?: DarkModePreference.System

        val isDark = when (darkMode) {
            DarkModePreference.System -> osThemeIsDark
            DarkModePreference.Dark -> true
            DarkModePreference.Light -> false
        }

        val pack = packs.find { it.id == packId }
            ?: packs.find { it.id == DEFAULT_THEME_PACK_ID }
            ?: defaultPack

        if (isDark) pack.dark else pack.light
    }

    fun setThemePack(packId: String) {
        synchronized(this) {
            val available = availablePacks.value.map { it.id }
            appSettings.themePack.value = if (available.contains(packId)) {
                packId
            } else {
                DEFAULT_THEME_PACK_ID
            }
        }
    }

    fun setDarkMode(preference: DarkModePreference) {
        synchronized(this) {
            appSettings.darkMode.value = preference.name
            when (preference) {
                DarkModePreference.System -> registerSystemThemeDetector()
                else -> unRegisterSystemThemeDetector()
            }
        }
    }

    private var booted = guardedEntry()

    fun boot() {
        booted.action {
            _availablePacks.update {
                it.plus(DefaultThemes.getAllPacks())
            }

            val darkMode = DarkModePreference.entries.find {
                it.name == appSettings.darkMode.value
            } ?: DarkModePreference.System

            when (darkMode) {
                DarkModePreference.System -> registerSystemThemeDetector()
                else -> unRegisterSystemThemeDetector()
            }
        }
    }

    private var osUpdateFlowJob: Job? = null
    private fun registerSystemThemeDetector() {
        osUpdateFlowJob?.cancel()
        if (osThemeDetector.isSupported) {
            osDarkModeFlow.value = osThemeDetector.isDark()
            osUpdateFlowJob = osThemeDetector.systemThemeFlow.onEach { isDark ->
                osDarkModeFlow.value = isDark
            }.launchIn(scope)
        }
    }

    private fun unRegisterSystemThemeDetector() {
        osUpdateFlowJob?.cancel()
        osUpdateFlowJob = null
    }

    fun registerDynamicThemes(darkColors: MyColors, lightColors: MyColors) {
        _availablePacks.update { existing ->
            val dynamicPack = ThemePack(
                id = DYNAMIC_THEME_PACK_ID,
                name = "Dynamic",
                dark = darkColors,
                light = lightColors,
            )
            existing
                .filter { it.id != DYNAMIC_THEME_PACK_ID }
                .toMutableList()
                .apply { add(0, dynamicPack) }
        }
    }
}

@Stable
data class ThemePackInfo(
    val id: String,
    val name: StringSource,
    val color: Color,
)

private fun ThemePack.toThemePackInfo(): ThemePackInfo {
    return ThemePackInfo(
        id = id,
        name = name.asStringSource(),
        color = dark.surface,
    )
}
