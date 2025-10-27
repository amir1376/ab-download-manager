package com.abdownloadmanager.shared.ui.theme

import androidx.compose.ui.graphics.Color
import com.abdownloadmanager.shared.util.ui.MyColors

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
        background = Color(0xFF1E1F22),
        onBackground = Color(0xFFD6D6D6),
        surface = Color(0xFF2A2B2F),
        onSurface = Color(0xFFE0E0E0),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFEFEFEF),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFE5E5E5),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF1E1E1E),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF1E1E1E),
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

    val black = MyColors(
        id = "black",
        name = "Black",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFFEFF2F6),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFFEFF2F6),
        background = Color(0xFF000000),
        onBackground = Color(0xFFEFEFEF),
        surface = Color(0xFF1A1F26),
        onSurface = Color(0xFFEFEFEF),
        error = Color(0xFFEA4C3C),
        onError = Color(0xFFFFFFFF),
        success = Color(0xFF45B36B),
        onSuccess = Color(0xFFFFFFFF),
        warning = Color(0xFFF6C244),
        onWarning = Color(0xFF000000),
        info = Color(0xFF40A9F3),
        onInfo = Color(0xFF000000),
        isLight = false
    )

    val lightGray = MyColors(
        id = "light_gray",
        name = "Light Gray",
        primary = Color(0xFF4791BF),
        primaryVariant = Color(0xFF60A6D9),
        onPrimary = Color(0xFF20303A),
        secondary = Color(0xFFB85DFF),
        secondaryVariant = Color(0xFFD1A6FF),
        onSecondary = Color(0xFF20303A),
        background = Color(0xFFF0F0F0),
        onBackground = Color(0xFF232323),
        surface = Color(0xFFE0E0E0),
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


    fun getAll(): List<MyColors> {
        return listOf(
            dark,
            light,
            obsidian,
            deepOcean,
            black,
            lightGray,
        )
    }

    fun getDefaultDark() = dark
    fun getDefaultLight() = light
}
