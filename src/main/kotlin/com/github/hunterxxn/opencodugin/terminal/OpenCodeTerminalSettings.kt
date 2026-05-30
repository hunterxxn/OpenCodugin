package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.jediterm.terminal.TerminalColor
import com.jediterm.terminal.ui.TerminalActionPresentation
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class OpenCodeTerminalSettings : DefaultSettingsProvider() {

    override fun enableMouseReporting(): Boolean = true

    override fun forceActionOnMouseReporting(): Boolean = false

    override fun copyOnSelect(): Boolean = false

    override fun pasteOnMiddleMouseClick(): Boolean = false

    override fun getPasteActionPresentation(): TerminalActionPresentation {
        val keystrokes = listOf(
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)
        )
        return TerminalActionPresentation("Paste", keystrokes)
    }

    override fun getDefaultBackground(): TerminalColor =
        TerminalColor.rgb(30, 30, 30)

    override fun getDefaultForeground(): TerminalColor =
        TerminalColor.rgb(204, 204, 204)


    override fun getTerminalFont(): Font {
        val size = getTerminalFontSize().toInt()

        val embedded = loadEmbeddedFont(size)
        if (embedded != null) return embedded

        val allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
        val candidates = listOf(
            "JetBrains Mono", "Cascadia Code", "Consolas",
            "Maple Mono NF CN"
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

