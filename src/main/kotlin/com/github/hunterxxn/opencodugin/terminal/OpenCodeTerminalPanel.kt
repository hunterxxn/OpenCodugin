package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.ui.JediTermWidget
import com.pty4j.PtyProcess
import java.awt.BorderLayout
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.io.IOException
import java.nio.charset.Charset
import javax.swing.JPanel
import javax.swing.SwingUtilities

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
        hideTerminalScrollbar()
        installMouseWheelForwarder()
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

    private fun hideTerminalScrollbar() {
        SwingUtilities.invokeLater {
            val scrollbar = findScrollbar(terminalWidget)
            scrollbar?.apply {
                isVisible = false
                isEnabled = false
                preferredSize = java.awt.Dimension(0, 0)
                minimumSize = java.awt.Dimension(0, 0)
                maximumSize = java.awt.Dimension(0, 0)
            }
        }
    }

    private fun installMouseWheelForwarder() {
        terminalWidget.addMouseWheelListener(object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                val s = session ?: return
                if (!s.isAlive) return
                try {
                    val metrics = terminalWidget.getFontMetrics(terminalWidget.font)
                    val charW = metrics.charWidth('W')
                    val charH = metrics.height
                    val col = (e.x / charW).coerceAtLeast(0)
                    val row = (e.y / charH).coerceAtLeast(0)
                    val button = if (e.unitsToScroll < 0) 64 else 65
                    val seq = "\u001b[<$button;${col + 1};${row + 1}M"
                    s.process.outputStream.write(seq.toByteArray(Charset.defaultCharset()))
                    s.process.outputStream.flush()
                    e.consume()
                } catch (_: Exception) {
                }
            }
        })
    }

    private fun findScrollbar(container: java.awt.Container): java.awt.Component? {
        for (component in container.components) {
            val clsName = component.javaClass.name.lowercase()
            if (clsName.contains("scrollbar") || clsName.contains("scroll")) return component
            if (component is java.awt.Container) {
                findScrollbar(component)?.let { return it }
            }
        }
        return null
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
