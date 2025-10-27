package com.abdownloadmanager.shared.util.ui

import androidx.compose.animation.animateColor
import com.abdownloadmanager.shared.util.darker
import com.abdownloadmanager.shared.util.div
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalMyColors = compositionLocalOf<MyColors> { error("LocalMyColors not provided") }

val myColors
    @Composable
    get() = LocalMyColors.current

@Immutable
data class MyColors(
    val id: String,
    val name: String,


    val primary: Color,
    val primaryVariant: Color = primary,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryVariant: Color = secondary,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val onSurface: Color,
    val surface: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val isLight: Boolean,
) {

    val warningGradient: Brush by lazy {
        Brush.linearGradient(
            listOf(warning, warning.darker())
        )
    }
    val errorGradient: Brush by lazy {
        Brush.linearGradient(
            listOf(error, error.darker())
        )
    }
    val successGradient: Brush by lazy {
        Brush.linearGradient(
            listOf(success, success.darker())
        )
    }
    val infoGradient: Brush by lazy {
        Brush.linearGradient(
            listOf(info, info.darker())
        )
    }

    val menuGradientBackground = surface
    val menuBorderColor = onSurface / 0.1f
    val onMenuColor = onSurface

    val primaryGradientColors = listOf(primary, secondary)
    val primaryGradient by lazy {
        Brush.linearGradient(primaryGradientColors)
    }
    val onPrimaryGradient = Color.White

    fun selectionGradient(
        startAlpha: Float = 1f, endAlpha: Float = 0f,
        color: Color = surface,
    ): Brush {
        return Brush.linearGradient(listOf(color / startAlpha, color / endAlpha))
    }

    val focusedBorderColor = primary


    fun getContentColorFor(color: Color): Color {
        return when (color) {
            primary, primaryVariant -> onPrimary
            secondary, secondaryVariant -> onSecondary
            error -> onError
            success -> onSuccess
            background -> onBackground
            surface -> onSurface
            else -> Color.Unspecified
        }
    }

    val contrast = if (isLight) Color.White else Color.Black
    val onContrast = if (isLight) Color.Black else Color.White
}

private object AnimateMyColors {
    @Composable
    fun animatedColors(
        toBeAnimated: MyColors,
        spec: FiniteAnimationSpec<Color> = tween(500),
    ): MyColors {
        val primary by animated(toBeAnimated.primary, spec)
        val primaryVariant by animated(toBeAnimated.primaryVariant, spec)
        val onPrimary by animated(toBeAnimated.onPrimary, spec)

        val secondary by animated(toBeAnimated.secondary, spec)
        val secondaryVariant by animated(toBeAnimated.secondaryVariant, spec)
        val onSecondary by animated(toBeAnimated.onSecondary, spec)

        val background by animated(toBeAnimated.background, spec)
        val onBackground by animated(toBeAnimated.onBackground, spec)

        val surface by animated(toBeAnimated.surface, spec)
        val onSurface by animated(toBeAnimated.onSurface, spec)

        val success by animated(toBeAnimated.success, spec)
        val onSuccess by animated(toBeAnimated.onSuccess, spec)

        val error by animated(toBeAnimated.error, spec)
        val onError by animated(toBeAnimated.onError, spec)

        val warning by animated(toBeAnimated.warning, spec)
        val onWarning by animated(toBeAnimated.onWarning, spec)

        val info by animated(toBeAnimated.info, spec)
        val onInfo by animated(toBeAnimated.onInfo, spec)


        val isLight = toBeAnimated.isLight

        return MyColors(
            primary = primary,
            primaryVariant = primaryVariant,
            onPrimary = onPrimary,
            secondary = secondary,
            secondaryVariant = secondaryVariant,
            onSecondary = onSecondary,
            background = background,
            onBackground = onBackground,
            onSurface = onSurface,
            surface = surface,
            error = error,
            onError = onError,
            success = success,
            onSuccess = onSuccess,
            warning = warning,
            onWarning = onWarning,
            info = info,
            onInfo = onInfo,
            isLight = isLight,
            name = toBeAnimated.name,
            id = toBeAnimated.id,
        )
    }

    @Composable
    private fun animated(
        color: Color,
        animationSpec: AnimationSpec<Color> = tween(500),
    ): State<Color> {
        return animateColorAsState(color, animationSpec = animationSpec)
    }
}

// it seems this method is more laggy! even though it uses single transition!
private object AnimateMyColorsWithSingleTransition {
    @Composable
    fun animatedColors(
        toBeAnimated: MyColors,
        spec: FiniteAnimationSpec<Color> = tween(500)
    ): MyColors {
        val spec: @Composable Transition.Segment<MyColors>.() -> FiniteAnimationSpec<Color> = { spec }
        val transition = updateTransition(toBeAnimated, "animateMyColors")

        val primary by transition.animateColor(spec, "primary") { it.primary }
        val primaryVariant by transition.animateColor(spec, "primaryVariant") { it.primaryVariant }
        val onPrimary by transition.animateColor(spec, "onPrimary") { it.onPrimary }

        val secondary by transition.animateColor(spec, "secondary") { it.secondary }
        val secondaryVariant by transition.animateColor(spec, "secondaryVariant") { it.secondaryVariant }
        val onSecondary by transition.animateColor(spec, "onSecondary") { it.onSecondary }

        val background by transition.animateColor(spec, "background") { it.background }
        val onBackground by transition.animateColor(spec, "onBackground") { it.onBackground }

        val surface by transition.animateColor(spec, "surface") { it.surface }
        val onSurface by transition.animateColor(spec, "onSurface") { it.onSurface }

        val success by transition.animateColor(spec, "success") { it.success }
        val onSuccess by transition.animateColor(spec, "onSuccess") { it.onSuccess }

        val error by transition.animateColor(spec, "error") { it.error }
        val onError by transition.animateColor(spec, "onError") { it.onError }

        val warning by transition.animateColor(spec, "warning") { it.warning }
        val onWarning by transition.animateColor(spec, "onWarning") { it.onWarning }

        val info by transition.animateColor(spec, "info") { it.info }
        val onInfo by transition.animateColor(spec, "onInfo") { it.onInfo }

        return MyColors(
            primary = primary,
            primaryVariant = primaryVariant,
            onPrimary = onPrimary,
            secondary = secondary,
            secondaryVariant = secondaryVariant,
            onSecondary = onSecondary,
            background = background,
            onBackground = onBackground,
            onSurface = onSurface,
            surface = surface,
            error = error,
            onError = onError,
            success = success,
            onSuccess = onSuccess,
            warning = warning,
            onWarning = onWarning,
            info = info,
            onInfo = onInfo,
            isLight = toBeAnimated.isLight,
            name = toBeAnimated.name,
            id = toBeAnimated.id,
        )
    }
}

@Composable
fun animatedColors(
    toBeAnimated: MyColors,
    spec: FiniteAnimationSpec<Color> = tween(500),
): MyColors {
    return AnimateMyColors.animatedColors(
        toBeAnimated = toBeAnimated,
        spec = spec,
    )
}
