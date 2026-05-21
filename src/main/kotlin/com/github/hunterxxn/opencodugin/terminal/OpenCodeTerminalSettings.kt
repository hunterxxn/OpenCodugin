package com.github.hunterxxn.opencodugin.terminal

import com.jediterm.terminal.ui.settings.DefaultSettingsProvider

class OpenCodeTerminalSettings : DefaultSettingsProvider() {

    override fun enableMouseReporting(): Boolean = true

    override fun forceActionOnMouseReporting(): Boolean = false

    override fun copyOnSelect(): Boolean = false

    override fun pasteOnMiddleMouseClick(): Boolean = false
}
