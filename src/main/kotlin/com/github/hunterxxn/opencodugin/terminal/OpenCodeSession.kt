package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.commands.CliProvider
import com.jediterm.terminal.ui.JediTermWidget
import com.pty4j.PtyProcess
import java.util.UUID

data class OpenCodeSession(
    val id: String = UUID.randomUUID().toString(),
    val workingDirectory: String,
    val process: PtyProcess,
    val terminalWidget: JediTermWidget,
    val cliProvider: CliProvider
) {
    val isAlive: Boolean get() = process.isAlive

    fun close() {
        if (process.isAlive) {
            process.destroy()
        }
    }
}
