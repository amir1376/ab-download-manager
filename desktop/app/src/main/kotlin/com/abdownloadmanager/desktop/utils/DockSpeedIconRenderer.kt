package com.abdownloadmanager.desktop.utils

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Composes the app icon with a native-looking speed pill for the macOS dock.
 */
object DockSpeedIconRenderer {
    private const val SIZE = 512
    // macOS app icons leave a transparent margin around the artwork inside the
    // icon canvas; keep the same margin so replacing the icon does not resize it.
    private const val INSET = 50
    private const val CONTENT_SIZE = SIZE - INSET * 2

    private val baseIcon: BufferedImage by lazy {
        val stream = checkNotNull(DockSpeedIconRenderer::class.java.getResourceAsStream("/icon.png")) {
            "icon.png is missing from application resources"
        }
        stream.use { ImageIO.read(it) }
    }

    fun render(label: String): ByteArray {
        val image = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        try {
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            )
            g.drawImage(baseIcon, INSET, INSET, CONTENT_SIZE, CONTENT_SIZE, null)
            drawSpeedPill(g, label)
        } finally {
            g.dispose()
        }

        return ByteArrayOutputStream().use {
            ImageIO.write(image, "png", it)
            it.toByteArray()
        }
    }

    private fun drawSpeedPill(g: java.awt.Graphics2D, label: String) {
        val maxTextWidth = CONTENT_SIZE * 0.78f
        var fontSize = CONTENT_SIZE * 0.17f
        var font = Font("Helvetica Neue", Font.BOLD, fontSize.toInt())
        var metrics = g.getFontMetrics(font)
        while (metrics.stringWidth(label) > maxTextWidth && fontSize > CONTENT_SIZE * 0.07f) {
            fontSize -= 2f
            font = Font("Helvetica Neue", Font.BOLD, fontSize.toInt())
            metrics = g.getFontMetrics(font)
        }
        g.font = font

        val textWidth = metrics.stringWidth(label)
        val horizontalPadding = (fontSize * 0.5f).toInt()
        val verticalPadding = (fontSize * 0.22f).toInt()
        val pillWidth = textWidth + horizontalPadding * 2
        val pillHeight = metrics.height + verticalPadding
        val x = (SIZE - pillWidth) / 2
        val y = INSET + CONTENT_SIZE - pillHeight - (CONTENT_SIZE * 0.02f).toInt()

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        g.color = Color(18, 18, 18, 205)
        g.fillRoundRect(x, y, pillWidth, pillHeight, pillHeight, pillHeight)
        g.color = Color(255, 255, 255, 45)
        g.stroke = BasicStroke(3f)
        g.drawRoundRect(x, y, pillWidth, pillHeight, pillHeight, pillHeight)

        g.color = Color.WHITE
        val baseline = y + (pillHeight - metrics.height) / 2 + metrics.ascent
        g.drawString(label, x + horizontalPadding, baseline)
    }
}
