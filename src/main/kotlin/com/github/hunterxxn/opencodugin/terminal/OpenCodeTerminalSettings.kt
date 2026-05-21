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
        val allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
        val candidates = listOf(
            "Sarasa Term SC", "Sarasa Mono SC", "Sarasa Term J",
            "Cascadia Code", "Cascadia Mono",
            "JetBrains Mono", "JetBrainsMono Nerd Font",
            "Fira Code", "FiraCode Nerd Font",
            "Source Code Pro", "Hack",
            "Microsoft YaHei Mono",
            "DengXian",
            "NSimSun"
        )

        val name = candidates.firstOrNull { it in allFonts } ?: "Monospaced"
        val size = getTerminalFontSize().toInt()
        thisLogger().info("Selected terminal font: $name, size: $size")
        return Font(name, Font.PLAIN, size)
    }

    override fun getTerminalFontSize(): Float = 14f
}

