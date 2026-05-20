# OpenCode Terminal Plugin — Design Spec

## Problem

IDEA 内置 Terminal（JediTerm）的终端模拟层无法正确处理鼠标事件透传，导致 opencode 的 bubbletea TUI 出现严重的鼠标冲突。需要一个插件：
1. 先用标准终端模拟器让 opencode 正常运行
2. 预留扩展点，后续可添加 IDE 集成功能（快速引用文件、目录等）

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│  OpenCode Tool Window (ToolWindowFactory)            │
│  ┌───────────────────────────────────────────────┐  │
│  │  Toolbar (ActionToolbar / DefaultActionGroup) │  │
│  │  [New Session] [Stop] [Clear]                 │  │
│  │  [...]  扩展位（Phase 2 添加 IDE 集成按钮）    │  │
│  ├───────────────────────────────────────────────┤  │
│  │  Session Tabs (JBTabs / TabbedPane)           │  │
│  │  ┌─────────────────────────────────────┐      │  │
│  │  │ Tab 1: project-a                     │      │  │
│  │  │ ┌─────────────────────────────────┐  │      │  │
│  │  │ │ TerminalPanel                   │  │      │  │
│  │  │ │  (JediTerm Widget,              │  │      │  │
│  │  │ │   mouse tracking enabled)       │  │      │  │
│  │  │ │                                 │  │      │  │
│  │  │ │  Process: opencode              │  │      │  │
│  │  │ │  (OSProcessHandler + PTY)       │  │      │  │
│  │  │ └─────────────────────────────────┘  │      │  │
│  │  └─────────────────────────────────────┘      │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Terminal Tool Window

**入口：** `OpenCodeToolWindowFactory` implements `ToolWindowFactory`

- 注册到 `plugin.xml` 的 `<toolWindow>` 扩展点
- 创建工具窗口的主体结构（toolbar + tabs + terminal panel）
- anchor 默认设为 `bottom`，便于与 terminal 工作流一致

### 2. Session Manager

**服务：** `OpenCodeSessionManager` — project-level service

- 管理多个 opencode 进程实例（每个 tab 对应一个 session）
- 每个 session 包含：进程（`ProcessHandler`）、终端控件、工作目录
- 职责：
  - `createSession(project, workingDir)` → 创建新 session
  - `destroySession(sessionId)` → 终止 opencode 进程
  - `getActiveSession()` → 当前激活的 session
  - `listSessions()` → 所有 session

### 3. Terminal Panel

**组件：** `OpenCodeTerminalPanel`

- 封装 JediTerm `TerminalWidget` 创建和配置
- **关键配置：** 启用鼠标报告（TerminalMouseMode / xterm mouse tracking）确保 opencode TUI 正常接收鼠标事件
- 封装 `ProcessHandler` 与 PTY 连接的建立
- 暴露 `writeToProcess(text)` 方法 —— **核心扩展点**，供 Phase 2 的 IDE 集成功能调用

### 4. Extension Points (预留 Phase 2)

以下接口在 Phase 1 即定义好，Phase 2 实现：

```kotlin
// 向当前 session 注入内容的统一接口
interface OpenCodeCommandProvider {
    fun buildCommand(context: OpenCodeContext): String
    fun getActionLabel(): String
}

// 注入上下文：当前编辑器文件、选中的目录等
data class OpenCodeContext(
    val project: Project,
    val selectedFiles: List<VirtualFile>,
    val activeEditorFile: VirtualFile?
)
```

扩展通过 toolbar `DefaultActionGroup` 添加按钮实现，无需修改核心终端逻辑。

## Data Flow

```
Phase 1 流程：
User → Toolbar [New Session] 
     → SessionManager.createSession()
     → spawn opencode process (PTY)
     → TerminalPanel displays TUI
     → User interacts via keyboard/mouse → PTY → opencode
     

Phase 2 即将添加的流程：
User → Toolbar [Add File] / Right-click → context menu
     → buildCommand(context) → "Please look at @file:..."
     → SessionManager.activeSession.terminal.writeToProcess(text)
     → opencode receives context via stdin
```

## Key Decisions

- **为什么用 JediTerm 而不是其他终端库？** JediTerm 是 IDEA 平台自带的终端库，不需要额外依赖。鼠标事件问题出在 IDEA 对 JediTerm 的集成层（默认禁用了鼠标报告），而非 JediTerm 本身。正确配置即可解决。
- **为什么通过 stdin 注入而非 API？** opencode 没有对外 API，stdin 是唯一通信通道。使用 opencode 已有的文件引用语法（`@file:`）是最低侵入的方案。
- **为什么用 project-level service 而不是 application-level？** 每个打开的项目需要独立的 opencode session 集合，project service 生命周期与项目绑定，更合理。

## Testing Strategy

- 单元测试：`OpenCodeSessionManager` 生命周期管理
- 集成测试：使用 `BasePlatformTestCase` 验证 tool window 注册和创建
- 手动验证：在 sandbox IDE 中启动 opencode 进程，验证鼠标交互正常
- 测试数据目录：`src/test/testData/`

## File Layout

```
src/main/kotlin/com/github/hunterxxn/opencodugin/
├── terminal/
│   ├── OpenCodeToolWindowFactory.kt    # ToolWindowFactory 入口
│   ├── OpenCodeSessionManager.kt       # project-level service，session 生命周期
│   ├── OpenCodeTerminalPanel.kt        # 终端面板封装
│   ├── OpenCodeTerminalRunner.kt       # 进程启动（opencode binary 发现 + PTY）
│   └── OpenCodeSession.kt              # session 数据类
├── commands/                            # Phase 2 — 预留扩展接口
│   └── OpenCodeCommandProvider.kt       # 命令注入接口
├── MyBundle.kt                          # 已存在
├── services/MyProjectService.kt         # 已存在
└── ...
src/main/resources/META-INF/
└── plugin.xml                           # 注册 toolWindow + projectService
```

## Acceptance Criteria

1. 工具窗口显示在 IDE 底部，包含 toolbar 和终端面板
2. 点击 "New Session" 启动 opencode 进程，TUI 正常渲染
3. **opencode TUI 的鼠标操作（点击、滚动、选择）正常工作**
4. 支持多 tab 管理多个 session
5. "Stop" 按钮可终止进程，"Clear" 清空终端
6. `OpenCodeCommandProvider` 接口定义完成，Phase 2 可直接实现
