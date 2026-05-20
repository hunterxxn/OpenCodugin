# OpenCode Terminal Plugin — Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace IDEA's built-in terminal with a custom tool window that runs opencode with full mouse support via JediTerm + PTY.

**Architecture:** ToolWindowFactory creates a bottom tool window containing a JBTabbedPane of terminal sessions. Each session wraps a JediTermWidget connected to a PtyProcess via PtyProcessTtyConnector, with a custom SettingsProvider ensuring mouse reporting is enabled. OpenCodeSessionManager (project service) manages the session lifecycle.

**Tech Stack:** IntelliJ Platform 2025.2, Kotlin 2.1.20, JediTerm (bundled), pty4j (bundled), JUnit 4 + BasePlatformTestCase

---

### Task 1: Clean template boilerplate

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml:1-19`
- Modify: `src/test/kotlin/com/github/hunterxxn/opencodugin/MyPluginTest.kt:1-39`
- Delete: `src/main/kotlin/com/github/hunterxxn/opencodugin/toolWindow/MyToolWindowFactory.kt`
- Delete: `src/main/kotlin/com/github/hunterxxn/opencodugin/startup/MyProjectActivity.kt`
- Delete: `src/main/kotlin/com/github/hunterxxn/opencodugin/services/MyProjectService.kt`

- [ ] **Step 1: Remove sample tool window and startup activity from plugin.xml**

Replace the content of `src/main/resources/META-INF/plugin.xml`:

```xml
<!-- Plugin Configuration File. Read more: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html -->
<idea-plugin>
    <id>com.github.hunterxxn.opencodugin</id>
    <name>OpenCodugin</name>
    <vendor>hunterxxn</vendor>
    <description><![CDATA[
        <p><b>OpenCodugin</b> integrates opencode CLI into IntelliJ IDEA with full terminal mouse support and IDE-aware context sharing.</p>
    ]]></description>

    <depends>com.intellij.modules.platform</depends>

    <resource-bundle>messages.MyBundle</resource-bundle>

    <extensions defaultExtensionNs="com.intellij">
        <toolWindow
            factoryClass="com.github.hunterxxn.opencodugin.terminal.OpenCodeToolWindowFactory"
            id="OpenCode"
            anchor="bottom"
            icon="AllIcons.Toolwindows.ToolWindowTerminal"/>
    </extensions>
</idea-plugin>
```

- [ ] **Step 2: Delete sample source files**

Remove the three template sample files:
- `src/main/kotlin/com/github/hunterxxn/opencodugin/toolWindow/MyToolWindowFactory.kt`
- `src/main/kotlin/com/github/hunterxxn/opencodugin/startup/MyProjectActivity.kt`
- `src/main/kotlin/com/github/hunterxxn/opencodugin/services/MyProjectService.kt`

Run:
```powershell
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\toolWindow\MyToolWindowFactory.kt"
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\startup\MyProjectActivity.kt"
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\services\MyProjectService.kt"
```

Also clean up empty directories:
```powershell
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\toolWindow" -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\startup" -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath "src\main\kotlin\com\github\hunterxxn\opencodugin\services" -Force -ErrorAction SilentlyContinue
```

- [ ] **Step 3: Rewrite test file to remove template-specific tests**

Replace `src/test/kotlin/com/github/hunterxxn/opencodugin/MyPluginTest.kt` with a minimal skeleton:

```kotlin
package com.github.hunterxxn.opencodugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testPluginRegistered() {
        val toolWindow = toolWindowManager.getToolWindow("OpenCode")
        assertNotNull("OpenCode tool window should be registered", toolWindow)
    }
}
```

- [ ] **Step 4: Verify build compiles after cleanup**

Run: `.\gradlew.bat buildPlugin`
Expected: BUILD SUCCESSFUL (no compilation errors)

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: remove template boilerplate, register OpenCode tool window placeholder"
```

---

### Task 2: Create custom terminal settings provider

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalSettings.kt`

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalSettingsTest.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.jediterm.terminal.emulator.mouse.MouseMode
import com.jediterm.terminal.emulator.mouse.MouseFormat
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeTerminalSettingsTest : BasePlatformTestCase() {

    fun testMouseReportingEnabled() {
        val settings = OpenCodeTerminalSettings()
        assertTrue("Mouse reporting should be enabled", settings.enableMouseReporting())
        assertEquals(MouseMode.MOUSE_REPORTING_ALL_MOTION, settings.getMouseReportingMode())
    }

    fun testMouseActionsNotForced() {
        val settings = OpenCodeTerminalSettings()
        assertFalse("Should not force actions on mouse reporting", settings.forceActionOnMouseReporting())
    }

    fun testTerminalTypeReportsXterm() {
        val settings = OpenCodeTerminalSettings()
        assertTrue(settings.getTerminalType().contains("xterm"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat check --tests "com.github.hunterxxn.opencodugin.terminal.OpenCodeTerminalSettingsTest"`
Expected: FAIL (class not found)

- [ ] **Step 3: Implement OpenCodeTerminalSettings**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalSettings.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.jediterm.terminal.emulator.mouse.MouseFormat
import com.jediterm.terminal.emulator.mouse.MouseMode
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider

class OpenCodeTerminalSettings : DefaultSettingsProvider() {

    override fun enableMouseReporting(): Boolean = true

    override fun forceActionOnMouseReporting(): Boolean = false

    override fun getMouseReportingMode(): MouseMode = MouseMode.MOUSE_REPORTING_ALL_MOTION

    override fun getMouseFormat(): MouseFormat = MouseFormat.MOUSE_FORMAT_SGR

    override fun getTerminalType(): String = "xterm-256color"

    override fun copyOnSelect(): Boolean = false

    override fun pasteOnMiddleMouseClick(): Boolean = false
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat check --tests "com.github.hunterxxn.opencodugin.terminal.OpenCodeTerminalSettingsTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalSettings.kt src/test/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalSettingsTest.kt
git commit -m "feat: add OpenCodeTerminalSettings with mouse reporting enabled"
```

---

### Task 3: Create OpenCodeSession data class

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSession.kt`

- [ ] **Step 1: Create OpenCodeSession**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSession.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.pty4j.PtyProcess
import com.jediterm.terminal.ui.JediTermWidget
import java.util.UUID

data class OpenCodeSession(
    val id: String = UUID.randomUUID().toString(),
    val workingDirectory: String,
    val process: PtyProcess,
    val terminalWidget: JediTermWidget
) {
    val isAlive: Boolean get() = process.isRunning

    fun close() {
        if (process.isRunning) {
            process.destroy()
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `.\gradlew.bat compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSession.kt
git commit -m "feat: add OpenCodeSession data class"
```

---

### Task 4: Create OpenCodeTerminalRunner (PTY process spawner)

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalRunner.kt`

- [ ] **Step 1: Create OpenCodeTerminalRunner**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalRunner.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jediterm.core.util.TermSize
import com.pty4j.PtyProcessBuilder
import java.nio.charset.Charset
import java.nio.file.Path

object OpenCodeTerminalRunner {

    private const val INITIAL_COLS = 120
    private const val INITIAL_ROWS = 40

    fun findOpencodePath(project: Project): String {
        val candidates = listOf(
            "opencode",
            "opencode.bat",
            if (isWindows()) "opencode.exe" else "opencode"
        )

        for (cmd in candidates) {
            try {
                val result = ProcessBuilder()
                    .command(if (isWindows()) listOf("where", cmd) else listOf("which", cmd))
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
                // try next candidate
            }
        }

        return "opencode"
    }

    fun createSession(
        project: Project,
        workingDirectory: String,
        opencodePath: String = findOpencodePath(project)
    ): PtyProcess {
        val env = System.getenv().toMutableMap()
        env["TERM"] = "xterm-256color"
        env["COLORTERM"] = "truecolor"

        val command = listOf(opencodePath)
        val initialTermSize = TermSize(INITIAL_COLS, INITIAL_ROWS)

        thisLogger().info("Starting opencode: $command in $workingDirectory")

        return PtyProcessBuilder()
            .setCommand(command.toTypedArray())
            .setDirectory(workingDirectory)
            .setEnvironment(env.toMap())
            .setInitialColumns(initialTermSize.columns)
            .setInitialRows(initialTermSize.rows)
            .setConsole(false)
            .setCygwin(false)
            .setUseWinConPty(true)
            .setWindowsAnsiColorEnabled(true)
            .setRedirectErrorStream(true)
            .start()
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("win")
    }
}
```

- [ ] **Step 2: Update MyBundle.properties with terminal strings**

Replace `src/main/resources/messages/MyBundle.properties`:

```properties
opencode.toolwindow.title=OpenCode
opencode.session.new=New Session
opencode.session.stop=Stop
opencode.session.clear=Clear Terminal
opencode.session.tab.default=Session
opencode.notification.sessionStarted=OpenCode session started in {0}
opencode.notification.processTerminated=OpenCode process terminated
```

- [ ] **Step 3: Verify compilation**

Run: `.\gradlew.bat compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalRunner.kt src/main/resources/messages/MyBundle.properties
git commit -m "feat: add OpenCodeTerminalRunner for PTY process spawning"
```

---

### Task 5: Create OpenCodeTerminalPanel (JediTermWidget + process wiring)

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalPanel.kt`

- [ ] **Step 1: Create OpenCodeTerminalPanel**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalPanel.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.terminal.pty.PtyProcessTtyConnector
import com.jediterm.core.util.TermSize
import com.jediterm.terminal.ui.JediTermWidget
import javax.swing.JPanel
import java.awt.BorderLayout

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
            val initialTermSize = TermSize(120, 40)

            val ttyConnector = PtyProcessTtyConnector(process, settings.charset)
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
        terminalWidget.terminal.textBuffer.clearAll()
        terminalWidget.repaint()
    }

    fun writeToTerminal(text: String) {
        try {
            session?.process?.outputStream?.use { out ->
                out.write(text.toByteArray(settings.charset))
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
```

- [ ] **Step 2: Verify compilation**

Run: `.\gradlew.bat compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeTerminalPanel.kt
git commit -m "feat: add OpenCodeTerminalPanel with JediTermWidget and PTY wiring"
```

---

### Task 6: Create OpenCodeSessionManager (project service)

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSessionManager.kt`

- [ ] **Step 1: Write failing test**

Create `src/test/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSessionManagerTest.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeSessionManagerTest : BasePlatformTestCase() {

    fun testServiceRegistered() {
        val manager = project.service<OpenCodeSessionManager>()
        assertNotNull("OpenCodeSessionManager should be registered as project service", manager)
    }

    fun testInitialStateEmpty() {
        val manager = project.service<OpenCodeSessionManager>()
        assertTrue("Sessions should be empty initially", manager.getSessions().isEmpty())
    }

    fun testCreateAndRemovePanel() {
        val manager = project.service<OpenCodeSessionManager>()
        val panel = manager.createPanel(project.basePath ?: "/tmp")

        assertNotNull("Created panel should not be null", panel)
        assertEquals("Should have 1 panel", 1, manager.getSessions().size)

        val panelId = manager.getSessions().first().key
        manager.removePanel(panelId)
        assertEquals("Should have 0 panels after removal", 0, manager.getSessions().size)
    }

    fun testActivePanelTracking() {
        val manager = project.service<OpenCodeSessionManager>()
        val panel1 = manager.createPanel(project.basePath ?: "/tmp")
        val panel2 = manager.createPanel(project.basePath ?: "/tmp")

        assertNotNull(panel1)
        assertNotNull(panel2)
        assertEquals("Should have 2 panels", 2, manager.getSessions().size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat check --tests "com.github.hunterxxn.opencodugin.terminal.OpenCodeSessionManagerTest"`
Expected: FAIL (class not found)

- [ ] **Step 3: Implement OpenCodeSessionManager**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSessionManager.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class OpenCodeSessionManager(private val project: Project) {

    private val panels = ConcurrentHashMap<String, OpenCodeTerminalPanel>()

    fun createPanel(workingDirectory: String): OpenCodeTerminalPanel {
        val panel = OpenCodeTerminalPanel(project, workingDirectory)
        panel.startSession()
        val session = panel.getCurrentSession()
        if (session != null) {
            panels[session.id] = panel
        }
        return panel
    }

    fun getSessions(): Map<String, OpenCodeTerminalPanel> = panels.toMap()

    fun getPanel(sessionId: String): OpenCodeTerminalPanel? = panels[sessionId]

    fun removePanel(sessionId: String) {
        panels[sessionId]?.dispose()
        panels.remove(sessionId)
    }

    fun removeAll() {
        panels.values.forEach { it.dispose() }
        panels.clear()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat check --tests "com.github.hunterxxn.opencodugin.terminal.OpenCodeSessionManagerTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSessionManager.kt src/test/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeSessionManagerTest.kt
git commit -m "feat: add OpenCodeSessionManager project service"
```

---

### Task 7: Create OpenCodeToolWindowFactory (tool window UI)

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeToolWindowFactory.kt`

- [ ] **Step 1: Create OpenCodeToolWindowFactory**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeToolWindowFactory.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.terminal

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.github.hunterxxn.opencodugin.MyBundle
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar
import java.awt.BorderLayout

class OpenCodeToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().info("OpenCodeToolWindowFactory initialized")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val sessionManager = project.service<OpenCodeSessionManager>()
        val tabs = JBTabsImpl(project)

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(createToolbar(project, sessionManager, tabs), BorderLayout.NORTH)
        mainPanel.add(tabs.component, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(mainPanel, null, false)
        toolWindow.contentManager.addContent(content)

        val defaultPanel = sessionManager.createPanel(project.basePath ?: System.getProperty("user.home"))
        val defaultSession = defaultPanel.getCurrentSession()
        if (defaultSession != null) {
            val tabInfo = TabInfo(defaultPanel.component)
            tabInfo.text = MyBundle["opencode.session.tab.default"]
            tabs.addTab(tabInfo)
            tabs.select(tabInfo, true)
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true

    private fun createToolbar(
        project: Project,
        sessionManager: OpenCodeSessionManager,
        tabs: JBTabsImpl
    ): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        toolbar.add(JButton(MyBundle["opencode.session.new"]).apply {
            addActionListener {
                val panel = sessionManager.createPanel(project.basePath ?: System.getProperty("user.home"))
                val session = panel.getCurrentSession()
                if (session != null) {
                    val tabInfo = TabInfo(panel.component)
                    tabInfo.text = "${MyBundle["opencode.session.tab.default"]} ${tabs.tabCount + 1}"
                    tabs.addTab(tabInfo)
                    tabs.select(tabInfo, true)
                }
            }
        })

        toolbar.add(JButton(MyBundle["opencode.session.stop"]).apply {
            addActionListener {
                val selectedInfo = tabs.selectedInfo ?: return@addActionListener
                val panel = findPanelForTab(sessionManager, selectedInfo) ?: return@addActionListener
                val session = panel.getCurrentSession()
                if (session != null) {
                    sessionManager.removePanel(session.id)
                }
                tabs.removeTab(selectedInfo)
            }
        })

        toolbar.add(JButton(MyBundle["opencode.session.clear"]).apply {
            addActionListener {
                val selectedInfo = tabs.selectedInfo ?: return@addActionListener
                val panel = findPanelForTab(sessionManager, selectedInfo) ?: return@addActionListener
                panel.clearTerminal()
            }
        })

        return toolbar
    }

    private fun findPanelForTab(
        sessionManager: OpenCodeSessionManager,
        tabInfo: TabInfo
    ): OpenCodeTerminalPanel? {
        for ((_, panel) in sessionManager.getSessions()) {
            if (panel.component == tabInfo.component) {
                return panel
            }
        }
        return null
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `.\gradlew.bat compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/OpenCodeToolWindowFactory.kt
git commit -m "feat: add OpenCodeToolWindowFactory with tabbed terminal UI"
```

---

### Task 8: Create command provider interface (Phase 2 extension point)

**Files:**
- Create: `src/main/kotlin/com/github/hunterxxn/opencodugin/commands/OpenCodeCommandProvider.kt`

- [ ] **Step 1: Create the extension interface**

Create `src/main/kotlin/com/github/hunterxxn/opencodugin/commands/OpenCodeCommandProvider.kt`:

```kotlin
package com.github.hunterxxn.opencodugin.commands

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class OpenCodeContext(
    val project: Project,
    val selectedFiles: List<VirtualFile>,
    val activeEditorFile: VirtualFile?
)

interface OpenCodeCommandProvider {
    fun buildCommand(context: OpenCodeContext): String
    fun getActionLabel(): String
}
```

- [ ] **Step 2: Verify compilation**

Run: `.\gradlew.bat compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/hunterxxn/opencodugin/commands/OpenCodeCommandProvider.kt
git commit -m "feat: add OpenCodeCommandProvider extension interface for Phase 2"
```

---

### Task 9: Final test update and verification

**Files:**
- Modify: `src/test/kotlin/com/github/hunterxxn/opencodugin/MyPluginTest.kt:1-12`

- [ ] **Step 1: Update the main test file**

Ensure `src/test/kotlin/com/github/hunterxxn/opencodugin/MyPluginTest.kt` contains:

```kotlin
package com.github.hunterxxn.opencodugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyPluginTest : BasePlatformTestCase() {

    fun testPluginRegistered() {
        val toolWindow = toolWindowManager.getToolWindow("OpenCode")
        assertNotNull("OpenCode tool window should be registered", toolWindow)
    }
}
```

- [ ] **Step 2: Run full check**

Run: `.\gradlew.bat check`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 3: Run buildPlugin**

Run: `.\gradlew.bat buildPlugin`
Expected: BUILD SUCCESSFUL, ZIP produced in `build/distributions/`

- [ ] **Step 4: Run verifyPlugin**

Run: `.\gradlew.bat verifyPlugin`
Expected: No plugin structure violations

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: finalize Phase 1 — OpenCode terminal with mouse support"
```

---

## Verification Checklist

After all tasks complete, verify:

1. `.\gradlew.bat buildPlugin` — produces ZIP artifact
2. `.\gradlew.bat check` — all tests pass (MyPluginTest, OpenCodeTerminalSettingsTest, OpenCodeSessionManagerTest)
3. `.\gradlew.bat verifyPlugin` — no plugin structure violations
4. `.\gradlew.bat runIde` — sandbox IDE launches, OpenCode tool window visible at bottom, New Session spawns opencode process with working mouse interaction

## File Summary

```
Created:
  src/main/kotlin/com/github/hunterxxn/opencodugin/terminal/
    OpenCodeTerminalSettings.kt       — custom SettingsProvider with mouse enabled
    OpenCodeSession.kt                — session data class
    OpenCodeTerminalRunner.kt         — PTY process spawner
    OpenCodeTerminalPanel.kt          — JediTermWidget + process wiring
    OpenCodeSessionManager.kt         — project service, multi-session lifecycle
    OpenCodeToolWindowFactory.kt      — tool window UI with tabs and toolbar
  src/main/kotlin/com/github/hunterxxn/opencodugin/commands/
    OpenCodeCommandProvider.kt        — Phase 2 extension interface
  src/test/kotlin/com/github/hunterxxn/opencodugin/terminal/
    OpenCodeTerminalSettingsTest.kt   — settings provider tests
    OpenCodeSessionManagerTest.kt     — session manager tests

Modified:
  src/main/resources/META-INF/plugin.xml              — registered OpenCode tool window
  src/main/resources/messages/MyBundle.properties     — terminal UI strings
  src/test/kotlin/.../MyPluginTest.kt                 — updated for new tool window

Deleted:
  src/main/kotlin/.../toolWindow/MyToolWindowFactory.kt
  src/main/kotlin/.../startup/MyProjectActivity.kt
  src/main/kotlin/.../services/MyProjectService.kt
  src/test/testData/rename/foo.xml
  src/test/testData/rename/foo_after.xml
```
