package com.github.hunterxxn.opencodugin.terminal

import com.github.hunterxxn.opencodugin.commands.CliProvider
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.ui.JediTermWidget
import com.pty4j.PtyProcess
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseWheelEvent
import java.io.IOException
import java.io.InputStreamReader
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JScrollBar
import javax.swing.JScrollPane

class OpenCodeTerminalPanel(
    private val project: Project,
    private val workingDirectory: String
) {
    val component: JPanel = JPanel(BorderLayout())

    private val settings = OpenCodeTerminalSettings()
    private val terminalWidget: JediTermWidget = JediTermWidget(settings)
    private var session: OpenCodeSession? = null
    private var shiftEnterDispatcher: KeyEventDispatcher? = null

    init {
        component.add(terminalWidget, BorderLayout.CENTER)
        hideScrollBars(terminalWidget)
    }

    fun startSession(provider: CliProvider, cliPath: String? = null): OpenCodeSession? {
        if (session?.isAlive == true) {
            thisLogger().warn("Session already running")
            return session
        }

        return try {
            val process = if (cliPath != null) {
                OpenCodeTerminalRunner.createSession(project, workingDirectory, cliPath)
            } else {
                OpenCodeTerminalRunner.createSession(project, workingDirectory)
            }
            val ttyConnector = PtyTtyConnector(process, provider.displayName)

            terminalWidget.createTerminalSession(ttyConnector)
            terminalWidget.start()

            val dispatcher = KeyEventDispatcher { event ->
                if (event.id == KeyEvent.KEY_PRESSED
                    && event.keyCode == KeyEvent.VK_ENTER
                    && event.isShiftDown
                    && !event.isControlDown
                    && !event.isAltDown
                    && !event.isMetaDown
                    && terminalWidget.isAncestorOf(event.component)
                ) {
                    try {
                        process.outputStream.write(byteArrayOf(0x0a))
                        process.outputStream.flush()
                    } catch (_: Exception) {
                    }
                    true
                } else {
                    false
                }
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
            shiftEnterDispatcher = dispatcher

            Toolkit.getDefaultToolkit().addAWTEventListener(
                AWTEventListener { event ->
                    if (event is MouseWheelEvent && terminalWidget.isAncestorOf(event.component)) {
                        val button = if (event.wheelRotation < 0) 64 else 65
                        val seq = "\u001b[?1006h\u001b[<$button;3;3M"
                        try {
                            process.outputStream.write(seq.toByteArray(Charsets.UTF_8))
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
                terminalWidget = terminalWidget,
                cliProvider = provider
            )

            thisLogger().info("OpenCode session started in $workingDirectory")
            session
        } catch (e: Exception) {
            thisLogger().error("Failed to start opencode session", e)
            null
        }
    }

    fun stopSession() {
        shiftEnterDispatcher?.let {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(it)
        }
        shiftEnterDispatcher = null
        session?.close()
        session = null
    }


    fun getCurrentSession(): OpenCodeSession? = session

    fun applyFont(font: Font) {
        terminalWidget.terminalPanel.setFont(font)
        terminalWidget.terminalPanel.repaint()
    }

    fun dispose() {
        stopSession()
    }

    companion object {
        fun hideScrollBars(component: Component) {
            when (component) {
                is JScrollPane -> {
                    component.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
                    component.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                }
                is JScrollBar -> component.isVisible = false
                is java.awt.Container -> component.components.forEach { hideScrollBars(it) }
            }
        }
    }
}

private class PtyTtyConnector(
    private val process: PtyProcess,
    private val providerName: String = "OpenCode"
) : TtyConnector {
    private val reader = InputStreamReader(process.inputStream, Charsets.UTF_8)
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

    override fun getName(): String = "$providerName PTY"

    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        return try {
            val n = reader.read(buf, offset, length)
            if (n < 0) -1 else n
        } catch (e: IOException) {
            -1
        }
    }

    override fun write(bytes: ByteArray) {
        try {
            val str = String(bytes, Charsets.UTF_8)

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
            reader.ready()
        } catch (_: IOException) {
            false
        }
    }

    override fun write(string: String) {
        write(string.toByteArray(Charsets.UTF_8))
    }

    override fun waitFor(): Int {
        return try {
            process.waitFor()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            -1
        }
    }
}
