package com.abdownloadmanager.shared.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.util.ui.MyColors

val isDynamicColorSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun dynamicDarkColors(context: Context): MyColors {
    val scheme = dynamicDarkColorScheme(context)
    return MyColors(
        id = DYNAMIC_THEME_ID_DARK,
        name = "Dynamic Dark",
        primary = scheme.primary,
        primaryVariant = scheme.primaryContainer,
        onPrimary = scheme.onPrimary,
        secondary = scheme.secondary,
        secondaryVariant = scheme.secondaryContainer,
        onSecondary = scheme.onSecondary,
        background = scheme.background,
        onBackground = scheme.onBackground,
        surface = scheme.surfaceVariant,
        onSurface = scheme.onSurfaceVariant,
        error = scheme.error,
        onError = scheme.onError,
        success = scheme.tertiary,
        onSuccess = scheme.onTertiary,
        warning = scheme.tertiaryContainer,
        onWarning = scheme.onTertiaryContainer,
        info = scheme.inversePrimary,
        onInfo = scheme.primary,
        isLight = false,
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun dynamicLightColors(context: Context): MyColors {
    val scheme = dynamicLightColorScheme(context)
    return MyColors(
        id = DYNAMIC_THEME_ID_LIGHT,
        name = "Dynamic Light",
        primary = scheme.primary,
        primaryVariant = scheme.primaryContainer,
        onPrimary = scheme.onPrimary,
        secondary = scheme.secondary,
        secondaryVariant = scheme.secondaryContainer,
        onSecondary = scheme.onSecondary,
        background = scheme.background,
        onBackground = scheme.onBackground,
        surface = scheme.surfaceVariant,
        onSurface = scheme.onSurfaceVariant,
        error = scheme.error,
        onError = scheme.onError,
        success = scheme.tertiary,
        onSuccess = scheme.onTertiary,
        warning = scheme.tertiaryContainer,
        onWarning = scheme.onTertiaryContainer,
        info = scheme.inversePrimary,
        onInfo = scheme.primary,
        isLight = true,
    )
}
