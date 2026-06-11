package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder

object OpenCodeTerminalRunner {

    private const val INITIAL_COLS = 120
    private const val INITIAL_ROWS = 40

    fun findOpencodePath(project: Project): String = findCliPath("opencode")

    fun findCliPath(name: String): String {
        val candidates = if (isWindows()) {
            listOf("$name.cmd", "$name.exe", name)
        } else {
            listOf(name)
        }

        for (cmd in candidates) {
            try {
                val command = if (isWindows()) listOf("where", cmd) else listOf("which", cmd)
                val result = ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start()
                val output = result.inputStream.bufferedReader(Charsets.UTF_8).readText().trim()
                result.waitFor()
                if (result.exitValue() == 0 && output.isNotBlank()) {
                    val firstLine = output.lines().first().trim()
                    thisLogger().info("Found $name at: $firstLine")
                    return firstLine
                }
            } catch (_: Exception) {
            }
        }

        return name
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
        env["LANG"] = "en_US.UTF-8"
        env["LC_ALL"] = "en_US.UTF-8"

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
