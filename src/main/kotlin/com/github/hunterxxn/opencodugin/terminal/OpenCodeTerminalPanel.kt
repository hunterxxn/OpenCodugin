package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.ui.JediTermWidget
import com.pty4j.PtyProcess
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.MouseWheelEvent
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

            Toolkit.getDefaultToolkit().addAWTEventListener(
                AWTEventListener { event ->
                    if (event is MouseWheelEvent && terminalWidget.isAncestorOf(event.component)) {
                        val button = if (event.wheelRotation < 0) 64 else 65
                        val seq = "\u001b[?1006h\u001b[<$button;3;3M"
                        try {
                            process.outputStream.write(seq.toByteArray(Charset.defaultCharset()))
                            process.outputStream.flush()
                        } catch (_: Exception) {
                        }
                    }
                },
                AWTEvent.MOUSE_WHEEL_EVENT_MASK
            )

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
    private var lastWasPress = false
    private val sgrRelease = Regex("\u001b\\[<3;\\d+;\\d+M")
    private val sgrPress = Regex("\u001b\\[<[02];\\d+;\\d+M")

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
            val inputStream = process.inputStream
            val byteBuf = ByteArray(length)
            val len = inputStream.read(byteBuf, 0, length)
            if (len <= 0) return len

            val str = String(byteBuf, 0, len, charset)
            val n = minOf(str.length, buf.size - offset)
            for (i in 0 until n) {
                buf[offset + i] = str[i]
            }
            n
        } catch (e: IOException) {
            -1
        }
    }

    override fun write(bytes: ByteArray) {
        try {
            val str = String(bytes, 0, bytes.size, charset)

            if (sgrRelease.matches(str)) {
                if (!lastWasPress) return
                lastWasPress = false
            } else if (sgrPress.matches(str)) {
                lastWasPress = true
            }

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
