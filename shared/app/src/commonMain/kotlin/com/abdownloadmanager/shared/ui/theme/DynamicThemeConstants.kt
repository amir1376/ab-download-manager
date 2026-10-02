package com.abdownloadmanager.shared.ui.theme

import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.shared.util.ui.MyColors

const val DYNAMIC_THEME_PACK_ID = "dynamic"
const val DYNAMIC_THEME_ID_DARK = "dynamic_dark"
const val DYNAMIC_THEME_ID_LIGHT = "dynamic_light"

object DynamicFallbackColors {
    val dark = MyColors(
        id = DYNAMIC_THEME_ID_DARK,
        name = "Dynamic Dark",
        primary = Color(0xFFD0BCFF),
        primaryVariant = Color(0xFFE8DEF8),
        onPrimary = Color(0xFF381E72),
        secondary = Color(0xFFCCC2DC),
        secondaryVariant = Color(0xFFEADDFF),
        onSecondary = Color(0xFF332D41),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF49454F),
        onSurface = Color(0xFFCAC4D0),
        error = Color(0xFFF2B8B8),
        onError = Color(0xFF601410),
        success = Color(0xFF6DD58C),
        onSuccess = Color(0xFF003919),
        warning = Color(0xFFFFB77C),
        onWarning = Color(0xFF4A2800),
        info = Color(0xFF90CAFF),
        onInfo = Color(0xFF003258),
        isLight = false,
    )

    val light = MyColors(
        id = DYNAMIC_THEME_ID_LIGHT,
        name = "Dynamic Light",
        primary = Color(0xFF6750A4),
        primaryVariant = Color(0xFFEADDFF),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF625B71),
        secondaryVariant = Color(0xFFE8DEF8),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFE7E0EC),
        onSurface = Color(0xFF49454F),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF146C2E),
        onSuccess = Color(0xFFFFFFFF),
        warning = Color(0xFF7D5700),
        onWarning = Color(0xFFFFFFFF),
        info = Color(0xFF6750A4),
        onInfo = Color(0xFFFFFFFF),
        isLight = true,
    )

    val pack = ThemePack(
        id = DYNAMIC_THEME_PACK_ID,
        name = "Dynamic",
        dark = dark,
        light = light,
    )
}
