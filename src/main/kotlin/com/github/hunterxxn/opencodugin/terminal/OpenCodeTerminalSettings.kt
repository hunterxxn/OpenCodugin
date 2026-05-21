package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import java.awt.Font
import java.awt.GraphicsEnvironment

class OpenCodeTerminalSettings : DefaultSettingsProvider() {

    override fun enableMouseReporting(): Boolean = true

    override fun forceActionOnMouseReporting(): Boolean = false

    override fun copyOnSelect(): Boolean = false

    override fun pasteOnMiddleMouseClick(): Boolean = false

    override fun getTerminalFont(): Font {
        val size = getTerminalFontSize().toInt()

        val embedded = loadEmbeddedFont(size)
        if (embedded != null) return embedded

        val allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
        val candidates = listOf(
            "Maple Mono NF CN", "Sarasa Fixed SC",
            "SimSun", "新宋体", "NSimSun",
            "Cascadia Code", "JetBrains Mono"
        )
        val name = candidates.firstOrNull { it in allFonts } ?: "Monospaced"
        thisLogger().info("Selected terminal font: $name, size: $size")
        return Font(name, Font.PLAIN, size)
    }

    override fun getTerminalFontSize(): Float = 14f

    private fun loadEmbeddedFont(size: Int): Font? {
        return try {
            val stream = javaClass.classLoader.getResourceAsStream("fonts/MapleMono-NF-CN-Regular.ttf")
                ?: return null
            stream.use { s ->
                val font = Font.createFont(Font.TRUETYPE_FONT, s).deriveFont(Font.PLAIN, size.toFloat())
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                ge.registerFont(font)
                thisLogger().info("Loaded embedded font: MapleMono-NF-CN")
                font
            }
        } catch (e: Exception) {
            thisLogger().warn("Failed to load embedded font", e)
            null
        }
    }
}

