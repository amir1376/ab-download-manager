package com.abdownloadmanager.desktop.pages.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.desktop.storage.AppSettingsStorage
import com.abdownloadmanager.desktop.ui.theme.MyColors
import com.abdownloadmanager.desktop.ui.theme.SystemThemeDetector
import com.abdownloadmanager.desktop.ui.theme.darkColors
import com.abdownloadmanager.desktop.ui.theme.lightColors
import com.abdownloadmanager.resources.Res
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource
import ir.amirab.util.flow.combineStateFlows
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class ThemeManager(
    private val scope: CoroutineScope,
    private val appSettings: AppSettingsStorage,
) {
    companion object {
        val defaultThemes = listOf(
            darkColors,
            lightColors,
        )
        val defaultDarkTheme = darkColors
        val defaultLightTheme = lightColors
        val DefaultTheme = defaultDarkTheme
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

    val possibleThemesToSelect = availableThemes.mapStateFlow {
        buildList {
            addAll(it.map {
                it.toThemeInfo()
            })
            if (osThemeDetector.isSupported) {
                add(systemThemeInfo)
            }
        }
    }
    private val themeIds = possibleThemesToSelect.mapStateFlow {
        it.map { it.id }
    }


    val currentThemeInfo = combineStateFlows(
        appSettings.theme, possibleThemesToSelect
    ) { themeId, possibleThemes ->
        possibleThemes.find {
            it.id == themeId
        } ?: possibleThemes.find {
            it.id == DEFAULT_THEME_ID
        }!!
    }




    private val osThemeDetector = SystemThemeDetector()
    private var osDarkModeFlow = MutableStateFlow(true)

    val currentThemeColor = combineStateFlows(
        themeIds, appSettings.theme, osDarkModeFlow
    ) { themes, themeId, osThemeIsDark ->
        if (themeId == systemThemeInfo.id) {
            if (osThemeIsDark) {
                defaultDarkTheme
            } else {
                defaultLightTheme
            }
        } else {
            if (themes.contains(themeId)) {
                getThemeById(themeId)!!
            } else {
                defaultDarkTheme
            }
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

    @Volatile
    private var booted = false

    fun boot() {
        if (booted) return
        // now we can load custom themes here
        // loadCustomThemes()
        //
        _availableThemes.update {
            it.plus(defaultThemes)
        }
        setTheme(appSettings.theme.value)
        booted = true
    }

    private var osUpdateFlowJob: Job? = null
    private fun registerSystemThemeDetector() {
        osUpdateFlowJob?.cancel()
        if (osThemeDetector.isSupported) {
            // update immediately
            osDarkModeFlow.value = osThemeDetector.isDark
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