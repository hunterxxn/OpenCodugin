package com.github.hunterxxn.opencodugin.terminal

import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import java.awt.Font
import java.awt.GraphicsEnvironment

class OpenCodeTerminalSettings : DefaultSettingsProvider() {

    override fun enableMouseReporting(): Boolean = true

    override fun forceActionOnMouseReporting(): Boolean = false

    override fun copyOnSelect(): Boolean = false

    override fun pasteOnMiddleMouseClick(): Boolean = false

    override fun getTerminalFont(): Font {
        val candidates = listOf(
            "Sarasa Term SC",
            "Sarasa Mono SC",
            "Cascadia Code",
            "Cascadia Mono",
            "JetBrains Mono",
            "NSimSun"
        )
        val available = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .availableFontFamilyNames.toList()
        for (name in candidates) {
            if (name in available) return Font(name, Font.PLAIN, getTerminalFontSize().toInt())
        }
        return Font("Monospaced", Font.PLAIN, getTerminalFontSize().toInt())
    }

    override fun getTerminalFontSize(): Float = 14f
}

