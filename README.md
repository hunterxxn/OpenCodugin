# OpenCodugin

![Build](https://github.com/hunterxxn/OpenCodugin/workflows/Build/badge.svg)

Integrates [opencode](https://github.com/anomalyco/opencode) CLI into IntelliJ IDEA with full terminal mouse support and IDE-aware context sharing.

## Features

- **Native PTY terminal** — full terminal emulation via Jediterm, with mouse reporting and scroll support
- **Multiple sessions** — open separate opencode sessions in tabs
- **Bottom tool window** — docked at the bottom of the IDE, always accessible
- **IDE context sharing** — opencode is aware of the current project directory
- **File reference injection** — right-click any file in Project View or Editor to insert ` @path#L1-L10 ` into the active session

## Usage

Open the **OpenCode** tool window at the bottom of the IDE. A session starts automatically in the project root directory.

| Button | Action |
|--------|--------|
| New Session | Open a new opencode session in a new tab |
| Stop | Terminate the current session |
| Clear | Clear the terminal screen |

### Add File to OpenCode

Right-click a file in one of these locations and select **Add to OpenCode**:

| Location | Behavior |
|----------|----------|
| Project View (file tree) | Inserts file path relative to project root |
| Editor tab | Inserts file path relative to project root |
| Editor content area | Inserts file path + line range if text is selected |

The file reference is placed in the active terminal session as editable text — add more context, then press Enter to send. If no terminal session is active, the reference is copied to clipboard instead.

Multi-select is supported: select several files, right-click, and all references are inserted at once, space-separated.

## Installation

### From Marketplace

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > Search for **OpenCodugin** > <kbd>Install</kbd>

### Manual

Download the [latest release](https://github.com/hunterxxn/OpenCodugin/releases/latest) and install via:

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Prerequisites

- [opencode](https://github.com/anomalyco/opencode) must be installed and available on your PATH
- IntelliJ IDEA 2025.2 or later

## Development

```bash
./gradlew buildPlugin   # compile and package plugin ZIP
./gradlew check         # run tests
./gradlew runIde        # launch sandbox IDE with plugin loaded
```
