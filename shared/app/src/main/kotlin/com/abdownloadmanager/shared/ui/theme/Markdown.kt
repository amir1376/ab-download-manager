package com.abdownloadmanager.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.abdownloadmanager.shared.utils.ui.LocalContentColor
import com.abdownloadmanager.shared.utils.ui.LocalTextStyle
import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

@Composable
fun myMarkdownColors(): DefaultMarkdownColors {
    val currentColor = LocalContentColor.current
    return DefaultMarkdownColors(
        text = currentColor,
        codeBackground = myColors.surface,
        dividerColor = currentColor.copy(alpha = 0.1f),
        inlineCodeBackground = myColors.surface,
        tableBackground = Color.Transparent,
    )
}

@Composable
fun myMarkdownTypography(): DefaultMarkdownTypography {
    val defaultTextStyle = LocalTextStyle.current
    val textSizes = myTextSizes
    val colors = myColors
    return DefaultMarkdownTypography(
        h1 = defaultTextStyle.copy(
            fontSize = textSizes.xl * 1.1f,
            fontWeight = FontWeight.Bold,
        ),
        h2 = defaultTextStyle.copy(
            fontSize = textSizes.xl,
            fontWeight = FontWeight.Bold,
        ),
        h3 = defaultTextStyle.copy(
            fontSize = textSizes.lg,
            fontWeight = FontWeight.Bold,
        ),
        h4 = defaultTextStyle.copy(
            fontSize = textSizes.base,
            fontWeight = FontWeight.Bold,
        ),
        h5 = defaultTextStyle.copy(
            fontSize = textSizes.sm,
            fontWeight = FontWeight.Bold,
        ),
        h6 = defaultTextStyle.copy(
            fontSize = textSizes.xs,
            fontWeight = FontWeight.Bold,
        ),
        text = defaultTextStyle.copy(
            fontSize = textSizes.base,
            fontWeight = FontWeight.Bold,
        ),
        code = defaultTextStyle.copy(
            fontSize = textSizes.base,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
        ),
        inlineCode = defaultTextStyle.copy(
            fontSize = textSizes.base,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
        ),
        quote = defaultTextStyle.copy(
            fontSize = textSizes.base,
        ),
        paragraph = defaultTextStyle.copy(
            fontSize = textSizes.base,
        ),
        ordered = defaultTextStyle.copy(
            fontSize = textSizes.base,
        ),
        bullet = defaultTextStyle.copy(
            fontSize = textSizes.base,
        ),
        list = defaultTextStyle.copy(
            fontSize = textSizes.base,
            fontWeight = FontWeight.Normal,
        ),
        textLink = TextLinkStyles(
            style = defaultTextStyle.copy(
                fontSize = textSizes.base,
                color = colors.info,
            ).toSpanStyle(),
            hoveredStyle = defaultTextStyle.copy(
                fontSize = textSizes.base,
                color = colors.info,
                textDecoration = TextDecoration.Underline
            ).toSpanStyle()
        ),
        table = defaultTextStyle,
    )
}
