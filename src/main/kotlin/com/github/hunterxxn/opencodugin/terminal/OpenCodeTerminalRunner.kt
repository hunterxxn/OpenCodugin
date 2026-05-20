package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import java.nio.charset.Charset

object OpenCodeTerminalRunner {

    private const val INITIAL_COLS = 120
    private const val INITIAL_ROWS = 40

    fun findOpencodePath(project: Project): String {
        val candidates = if (isWindows()) {
            listOf("opencode.cmd", "opencode.exe", "opencode")
        } else {
            listOf("opencode")
        }

        for (cmd in candidates) {
            try {
                val command = if (isWindows()) listOf("where", cmd) else listOf("which", cmd)
                val result = ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start()
                val output = result.inputStream.bufferedReader(Charset.defaultCharset()).readText().trim()
                result.waitFor()
                if (result.exitValue() == 0 && output.isNotBlank()) {
                    val firstLine = output.lines().first().trim()
                    thisLogger().info("Found opencode at: $firstLine")
                    return firstLine
                }
            } catch (_: Exception) {
            }
        }

        return "opencode"
    }

    fun createSession(
        project: Project,
        workingDirectory: String,
        opencodePath: String = findOpencodePath(project)
    ): PtyProcess {
        val env = mutableMapOf<String, String>()
        env.putAll(System.getenv())
        env["TERM"] = "xterm-256color"
        env["COLORTERM"] = "truecolor"

        thisLogger().info("Starting opencode: [$opencodePath] in $workingDirectory")

        return PtyProcessBuilder()
            .setCommand(arrayOf(opencodePath))
            .setDirectory(workingDirectory)
            .setEnvironment(env)
            .setInitialColumns(INITIAL_COLS)
            .setInitialRows(INITIAL_ROWS)
            .setConsole(false)
            .setCygwin(false)
            .setUseWinConPty(true)
            .setWindowsAnsiColorEnabled(true)
            .setRedirectErrorStream(true)
            .start()
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").lowercase().contains("win")
}
