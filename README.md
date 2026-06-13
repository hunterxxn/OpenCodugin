# OpenCodugin

![Build](https://github.com/hunterxxn/OpenCodugin/workflows/Build/badge.svg)

将 [opencode](https://github.com/anomalyco/opencode) CLI 集成到 IntelliJ IDEA 中，提供完整的终端鼠标支持和 IDE 上下文共享。

## 写在前面

由于多年开发习惯，我还是喜欢以 IDEA 作为我的日常开发环境。 而在这一波 AI 浪潮中，最先出现的就是以 IDE + cli 的形式集成 AI 工具。
近几年来，又涌现出相当多优秀的 Agent （我认为 TUI 也算是一种 Agent）。其中，OpenCode 是我最先并且到现在，一直在使用的 Agent 工具。开源，兼容，生态好，OpenCode 的有点自然不必多说。
但在 IDEA Terminal 中直接使用，发现了很多问题，包括但不限于，鼠标滚轮无法准确滚动聊天记录，快捷键互相影响（有的时候想复制直接就关闭进程了），无法快捷@某行代码...
因此我萌生了用 AI 重写一个 IDEA 插件的想法。其实在写之前，也想看看有没有现成的插件，有一个插件，但我感觉使用体验和我想的不一样，于是决定自己重写。
之前一直自己用，作用也就是套壳一下 OpenCode，添加点辅助功能，我觉得也没必要发出来。但前几天基于 OpenCode 开发的 Mimo Code 也发布了。我觉得我可以把我习惯用的 TUI 工具都集成进来，这样这个工具的用处就大了很多。
目前可以使用OpenCode，MimoCode 和 Reasonix （Custom 输入 npx reasonix code，官网推荐的启动方式）


## 功能特性

- **原生 PTY 终端** — 基于 Jediterm 的完整终端模拟，支持鼠标事件上报和滚动
- **多会话管理** — 在标签页中打开独立的 CLI 会话
- **底部工具窗口** — 固定在 IDE 底部，随时可用
- **IDE 上下文共享** — CLI 自动使用项目根目录作为工作路径
- **文件引用注入** — 右键文件即可将 ` @路径#L行号 ` 格式的引用注入活跃终端会话
- **多 CLI 支持** — 支持 OpenCode、Mimo 及自定义命令

## 安装

### 从插件市场安装

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > 搜索 **OpenCodugin** > <kbd>Install</kbd>

### 手动安装

从 [GitHub Releases](https://github.com/hunterxxn/OpenCodugin/releases/latest) 下载最新版本，然后：

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## 前置条件

- IntelliJ IDEA 2024.2 或更高版本
- 至少安装以下 CLI 工具之一：
  - [opencode](https://github.com/anomalyco/opencode)
  - [mimo](https://github.com/anthropics/mimo)
  - 或其他任意命令行工具

## 使用方法

### 启动

打开 IDE 底部的 **OpenCodugin** 工具窗口，会自动弹出 CLI 选择菜单：

| 选项 | 说明 |
|------|------|
| OpenCode | 使用系统中安装的 opencode CLI |
| Mimo | 使用系统中安装的 mimo CLI |
| Custom... | 输入自定义命令启动 |

选择后自动创建会话并启动终端。

### 工具栏按钮

| 按钮 | 功能 |
|------|------|
| New Session | 新建一个会话标签页，弹出 CLI 选择菜单 |
| Stop | 终止当前会话 |
| Check for Updates | 检查插件是否有新版本 |

### Custom 自定义命令

选择 **Custom...** 后，在弹出的输入框中输入任意命令。支持以下格式：

#### 单个命令

```
opencode
mimo
node
python
```

直接输入命令名，插件会自动在系统 PATH 中查找并解析为完整路径（包括 `.cmd`、`.exe` 等扩展名）。

#### 带参数的命令

```
npx reasonix code
node server.js --port 3000
python -m http.server 8080
```

命令和参数用空格分隔，插件会：
1. 将第一个词解析为可执行文件路径（如 `npx` → `C:\Users\...\npm\npx.cmd`）
2. 其余部分作为参数传递

#### 完整路径

```
C:\Tools\my-cli.exe
/usr/local/bin/custom-tool
D:\dev_program\some-tool\bin\tool.cmd
```

输入包含 `\` 或 `/` 时，视为完整路径，不做拆分和解析。

### 文件引用注入

在以下位置右键文件，选择 **Add to OpenCode**：

| 位置 | 行为 |
|------|------|
| 项目视图（文件树） | 插入相对于项目根目录的文件路径 |
| 编辑器标签页 | 插入相对于项目根目录的文件路径 |
| 编辑器内容区 | 插入文件路径，选中文本时自动附加行号范围 |

引用以可编辑文本形式插入终端，可补充上下文后按 Enter 发送。若无活跃会话，引用会复制到剪贴板。

支持多选：选中多个文件后右键，所有引用一次性插入，以空格分隔。

## 开发

```bash
./gradlew buildPlugin   # 编译并打包插件 ZIP
./gradlew check         # 运行测试
./gradlew runIde        # 启动沙箱 IDE 加载插件
```
