package com.abdownloadmanager.shared.ui.theme

import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.shared.utils.ui.MyColors

object DefaultThemes {
    val dark = MyColors(
        id = "dark",
        name = "Dark",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFFEFF2F6),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFFEFF2F6),
        background = Color(0xFF1E1E1E),
        onBackground = Color(0xFFE5E5E5),
        surface = Color(0xFF2D2D30),
        onSurface = Color(0xFFE5E5E5),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFE5E5E5),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFE5E5E5),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF232323),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF232323),
        isLight = false
    )

    val light = MyColors(
        id = "light",
        name = "Light",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF3576A1),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFF9700FF),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF232323),
        surface = Color(0xFFF2F2F2),
        onSurface = Color(0xFF232323),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFFFFFFF),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF232323),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF232323),
        isLight = true
    )

    val comfortLight = MyColors(
        id = "comfort_light",
        name = "Comfort Light",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFF20303A),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFF20303A),
        background = Color(0xFFFAFAF7),
        onBackground = Color(0xFF232323),
        surface = Color(0xFFF3F3F0),
        onSurface = Color(0xFF232323),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFFFFFFF),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF232323),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF232323),
        isLight = true
    )

    val obsidian = MyColors(
        id = "obsidian",
        name = "Obsidian",
        primary = Color(0xFF4791BF),
        onPrimary = Color.White,
        secondary = Color(0xFFB85DFF),
        onSecondary = Color.White,
        background = Color(0xFF16161E),
        onBackground = Color(0xFFBBBBBB),
        onSurface = Color(0xFFBBBBBB),
        surface = Color(0xFF22222A),
        error = Color(0xffff5757),
        onError = Color.White,
        success = Color(0xff69BA5A),
        onSuccess = Color.White,
        warning = Color(0xFFffbe56),
        onWarning = Color.White,
        info = Color(0xFF2f77d4),
        onInfo = Color.White,
        isLight = false,
    )

    val modernDark = MyColors(
        id = "modern_dark",
        name = "Modern Dark",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFFEFF2F6),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFFEFF2F6),
        background = Color(0xFF181A20),
        onBackground = Color(0xFFEAEAEA),
        surface = Color(0xFF23272F),
        onSurface = Color(0xFFEAEAEA),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFEAEAEA),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFEAEAEA),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF181A20),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF181A20),
        isLight = false
    )

    val deepOcean = MyColors(
        id = "deep_ocean",
        name = "Deep Ocean",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFFEFF2F6),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFFEFF2F6),
        background = Color(0xFF17212B),
        onBackground = Color(0xFFE5EAF2),
        surface = Color(0xFF242F3D),
        onSurface = Color(0xFFE5EAF2),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFE5E5E5),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFE5E5E5),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF232323),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF232323),
        isLight = false
    )

    fun getAll(): List<MyColors> {
        return listOf(
            dark,
            light,
            obsidian,
            modernDark,
            deepOcean,
            comfortLight,
        )
    }

    fun getDefaultDark() = dark
    fun getDefaultLight() = light
}
