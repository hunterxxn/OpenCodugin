# OpenCodugin Changelog

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
