package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.ui.JediTermWidget
import com.pty4j.PtyProcess
import java.awt.BorderLayout
import java.io.IOException
import java.nio.charset.Charset
import javax.swing.JPanel

class OpenCodeTerminalPanel(
    private val project: Project,
    private val workingDirectory: String
) {
    val component: JPanel = JPanel(BorderLayout())

    private val settings = OpenCodeTerminalSettings()
    private val terminalWidget: JediTermWidget = JediTermWidget(settings)
    private var session: OpenCodeSession? = null

    init {
        component.add(terminalWidget, BorderLayout.CENTER)
    }

    fun startSession(): OpenCodeSession? {
        if (session?.isAlive == true) {
            thisLogger().warn("Session already running")
            return session
        }

        return try {
            val process = OpenCodeTerminalRunner.createSession(project, workingDirectory)
            val ttyConnector = PtyTtyConnector(process)

            terminalWidget.createTerminalSession(ttyConnector)
            terminalWidget.start()

            session = OpenCodeSession(
                workingDirectory = workingDirectory,
                process = process,
                terminalWidget = terminalWidget
            )

            thisLogger().info("OpenCode session started in $workingDirectory")
            session
        } catch (e: Exception) {
            thisLogger().error("Failed to start opencode session", e)
            null
        }
    }

    fun stopSession() {
        session?.close()
        session = null
    }

    fun clearTerminal() {
        writeToTerminal("\u001b[2J\u001b[H")
    }

    fun writeToTerminal(text: String) {
        try {
            session?.process?.outputStream?.use { out ->
                out.write(text.toByteArray(Charset.defaultCharset()))
                out.flush()
            }
        } catch (e: Exception) {
            thisLogger().error("Failed to write to terminal process", e)
        }
    }

    fun getCurrentSession(): OpenCodeSession? = session

    fun dispose() {
        stopSession()
    }
}

private class PtyTtyConnector(
    private val process: PtyProcess
) : TtyConnector {
    private val charset = Charset.defaultCharset()

    override fun close() {
        if (process.isAlive) {
            process.destroy()
        }
    }

    override fun resize(termSize: com.jediterm.core.util.TermSize) {
        process.winSize = com.pty4j.WinSize(termSize.columns, termSize.rows)
    }

    override fun getName(): String = "OpenCode PTY"

    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        return try {
            val bytes = ByteArray(length)
            val inputStream = process.inputStream
            if (inputStream.available() > 0) {
                val read = inputStream.read(bytes, 0, length.coerceAtMost(inputStream.available()))
                if (read > 0) {
                    val str = String(bytes, 0, read, charset)
                    str.toCharArray(buf, offset, 0, str.length)
                    str.length
                } else {
                    -1
                }
            } else {
                val read = inputStream.read()
                if (read >= 0) {
                    buf[offset] = read.toChar()
                    1
                } else {
                    -1
                }
            }
        } catch (e: IOException) {
            -1
        }
    }

    override fun write(bytes: ByteArray) {
        try {
            process.outputStream.write(bytes)
            process.outputStream.flush()
        } catch (_: Exception) {
        }
    }

    override fun isConnected(): Boolean = process.isAlive

    override fun ready(): Boolean {
        return try {
            process.inputStream.available() > 0
        } catch (_: IOException) {
            false
        }
    }

    override fun write(string: String) {
        write(string.toByteArray(charset))
    }

    override fun waitFor(): Int {
        return try {
            process.waitFor()
        } catch (_: InterruptedException) {
            -1
        }
    }
}
