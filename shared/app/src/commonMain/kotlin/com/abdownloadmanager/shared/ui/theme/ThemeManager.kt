package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import com.abdownloadmanager.shared.util.ui.MyColors
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import ir.amirab.util.guardedEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlin.collections.filter
import kotlin.collections.map

class ThemeManager(
    private val scope: CoroutineScope,
    private val appSettings: ThemeSettingsStorage,
    private val osThemeDetector: ISystemThemeDetector,
) {
    companion object {
        val defaultThemes = DefaultThemes.getAll()
        val DefaultDarkTheme = DefaultThemes.getDefaultDark()
        val DefaultLightTheme = DefaultThemes.getDefaultLight()
        val DefaultTheme = DefaultDarkTheme
        val DEFAULT_THEME_ID = DefaultTheme.id
        val systemThemeInfo = ThemeInfo(
            id = "system",
            name = Res.string.system.asStringSource(),
            color = Color.Gray,
        )
    }

    private val _availableThemes = MutableStateFlow(emptyList<MyColors>())
    val availableThemes = _availableThemes.asStateFlow()

    private fun getThemeById(themeId: String): MyColors? {
        return availableThemes.value.find {
            it.id == themeId
        }
    }

    val selectableThemes = availableThemes.mapStateFlow {
        buildList {
            if (osThemeDetector.isSupported) {
                add(systemThemeInfo)
            }
            addAll(it.map {
                it.toThemeInfo()
            })
        }
    }

    val selectableDarkThemes = availableThemes.mapStateFlow {
        it.filter { !it.isLight }.map { it.toThemeInfo() }
    }

    val selectableLightThemes = availableThemes.mapStateFlow {
        it.filter { it.isLight }.map { it.toThemeInfo() }
    }

    private val themeIds = selectableThemes.mapStateFlow {
        it.map { it.id }
    }


    val currentThemeInfo = combineStateFlows(
        appSettings.theme, selectableThemes
    ) { themeId, possibleThemes ->
        possibleThemes.find {
            it.id == themeId
        } ?: possibleThemes.find {
            it.id == DEFAULT_THEME_ID
        }!!
    }

    val selectedDarkThemeInfo = combineStateFlows(
        appSettings.defaultDarkTheme, selectableThemes
    ) { themeId, possibleThemes ->
        possibleThemes.find {
            it.id == themeId
        } ?: possibleThemes.find {
            it.id == DefaultDarkTheme.id
        }!!
    }

    val selectedLightThemeInfo = combineStateFlows(
        appSettings.defaultLightTheme, selectableThemes
    ) { themeId, possibleThemes ->
        possibleThemes.find {
            it.id == themeId
        } ?: possibleThemes.find {
            it.id == DefaultLightTheme.id
        }!!
    }


    private var osDarkModeFlow = MutableStateFlow(true)

    val currentThemeColor = combineStateFlows(
        themeIds,
        appSettings.theme,
        appSettings.defaultDarkTheme,
        appSettings.defaultLightTheme,
        osDarkModeFlow,
    ) { themes, themeId, userDefaultDarkThemeId, userDefaultLightThemeId, osThemeIsDark ->
        val id = if (themeId == systemThemeInfo.id) {
            if (osThemeIsDark) {
                userDefaultDarkThemeId
            } else {
                userDefaultLightThemeId
            }
        } else {
            themeId
        }
        if (themes.contains(id)) {
            getThemeById(id)!!
        } else {
            DefaultTheme
        }
    }

    fun setTheme(themeId: String) {
        synchronized(this) {
            if (themeId == systemThemeInfo.id) {
                registerSystemThemeDetector()
            } else {
                unRegisterSystemThemeDetector()
            }
            if (themeIds.value.contains(themeId)) {
                appSettings.theme.value = themeId
            } else {
                // theme id in setting is invalid update it
                appSettings.theme.value = DEFAULT_THEME_ID
            }
        }
    }

    fun setDarkTheme(themeId: String) {
        synchronized(this) {
            appSettings.defaultDarkTheme.value = if (themeIds.value.contains(themeId)) {
                themeId
            } else {
                // theme id in setting is invalid update it
                DefaultDarkTheme.id
            }
        }
    }

    fun setLightTheme(themeId: String) {
        synchronized(this) {
            appSettings.defaultLightTheme.value = if (themeIds.value.contains(themeId)) {
                themeId
            } else {
                // theme id in setting is invalid update it
                DefaultLightTheme.id
            }
        }
    }

    private var booted = guardedEntry()

    fun boot() {
        booted.action {
            // now we can load custom themes here
            // loadCustomThemes()
            //
            _availableThemes.update {
                it.plus(defaultThemes)
            }
            setTheme(appSettings.theme.value)
        }
    }

    private var osUpdateFlowJob: Job? = null
    private fun registerSystemThemeDetector() {
        osUpdateFlowJob?.cancel()
        if (osThemeDetector.isSupported) {
            // update immediately
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

}

/**
 * This is for demonstration purposes of a theme
 */
@Stable
data class ThemeInfo(
    val id: String,
    val name: StringSource,
    val color: Color,
)

private fun MyColors.toThemeInfo(): ThemeInfo {
    return ThemeInfo(
        id = id,
        name = name.asStringSource(),
        color = surface,
    )
}
