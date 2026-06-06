# OpenCodugin Changelog

## [Unreleased]

### Changed
- 终端文本选择改为直接拖拽即可选中，不再需要按住 Shift
- 复制快捷键改为 Ctrl+C（有选中文本时复制，无选中时发送 SIGINT），Ctrl+Shift+C 作为备用

## [0.1.4] - 2026-05-31

### Changed
- 缩窄 Plugin Verifier 验证范围至仅 2024.3.3 + 2026.1.1，优化 CI 磁盘占用

### Fixed
- 修复终端中文字体渲染问题，改进 CJK 字符显示效果
- 修复 IDEA 2024.3.3 兼容性问题

## [0.1.3] - 2026-05-23

### Changed
- 重构更新检查逻辑，业务逻辑与 UI Action 分离，消除手动构造 AnActionEvent 的弃用警告
- 扩展插件兼容范围至 IntelliJ 2024.2 ~ 2026.1.x

### Fixed
- GitHub Releases 无发布时不再显示错误，改为提示"已是最新版本"
- 修复检查更新按钮样式与工具栏其他按钮不一致的问题

## [0.1.2] - 2026-05-23

### Added
- 工具栏"检查更新"按钮，点击查询 GitHub Releases 是否有新版本
- 检测到新版本时弹出对话框，并提供跳转下载链接
- 检测到已是最新或无发布时，右下角通知提示

## [0.1.1] - 2026-05-22

### Added
- **文件引用注入** — 项目视图、编辑器标签页、编辑器内容区右键菜单 "Add to OpenCode"，将 ` @相对路径#L行号 ` 格式的引用注入到活跃终端会话；支持多选文件、编辑器文本选中时自动附加行号、无活跃 session 时剪贴板兜底

## [0.1.0] - 2026-05-21
### Added
- PTY 终端集成 opencode CLI，支持鼠标滚轮和鼠标事件上报
- 多会话标签页管理（新建 / 停止 / 清除）
- 底部工具窗口，自动使用项目根目录为工作路径
- 内嵌 Maple Mono NF CN 字体，支持 CJK 字符渲染
- 自动检测系统等宽字体作为终端回退方案

### Changed
- 隐藏 JediTerm 自带的无用滚动条
- 默认终端暗色主题配色（背景 #1E1E1E，前景 #CCCCCC）

### Fixed
- TtyConnector 强制使用 UTF-8 编码，修复 Windows 中文环境 GBK 乱码
- 修复 SGR 鼠标 release 事件未匹配 press 导致的悬停误触发
- 修复 PtyTtyConnector read() 丢失数据的问题
