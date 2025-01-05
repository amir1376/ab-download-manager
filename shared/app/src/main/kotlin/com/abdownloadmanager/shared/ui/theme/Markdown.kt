package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.LocalTextStyle
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

@Composable
fun myMarkdownColors(): DefaultMarkdownColors {
    return DefaultMarkdownColors(
        text = LocalContentColor.current,
        linkText = myColors.info,
        codeText = myColors.onSurface,
        inlineCodeText = myColors.onSurface,
        codeBackground = myColors.surface,
        dividerColor = LocalContentColor.current.copy(alpha = 0.1f),
        inlineCodeBackground = myColors.surface,
    )
}

@Composable
fun myMarkdownTypography(): DefaultMarkdownTypography {
    val defaultTextStyle = LocalTextStyle.current
    return DefaultMarkdownTypography(
        h1 = defaultTextStyle.copy(
            fontSize = myTextSizes.xl * 1.1f,
            fontWeight = FontWeight.Bold,
        ),
        h2 = defaultTextStyle.copy(
            fontSize = myTextSizes.xl,
            fontWeight = FontWeight.Bold,
        ),
        h3 = defaultTextStyle.copy(
            fontSize = myTextSizes.lg,
            fontWeight = FontWeight.Bold,
        ),
        h4 = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Bold,
        ),
        h5 = defaultTextStyle.copy(
            fontSize = myTextSizes.sm,
            fontWeight = FontWeight.Bold,
        ),
        h6 = defaultTextStyle.copy(
            fontSize = myTextSizes.xs,
            fontWeight = FontWeight.Bold,
        ),
        text = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Bold,
        ),
        code = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
        ),
        inlineCode = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
        ),
        quote = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
        ),
        paragraph = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
        ),
        ordered = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
        ),
        bullet = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
        ),
        list = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
            fontWeight = FontWeight.Normal,
        ),
        link = defaultTextStyle.copy(
            fontSize = myTextSizes.base,
        ),
    )
}