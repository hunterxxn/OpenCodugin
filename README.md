# OpenCodugin

![Build](https://github.com/hunterxxn/OpenCodugin/workflows/Build/badge.svg)

Integrates [opencode](https://github.com/anomalyco/opencode) CLI into IntelliJ IDEA with full terminal mouse support and IDE-aware context sharing.

## Features

- **Native PTY terminal** — full terminal emulation via Jediterm, with mouse reporting and scroll support
- **Multiple sessions** — open separate opencode sessions in tabs
- **Bottom tool window** — docked at the bottom of the IDE, always accessible
- **IDE context sharing** — opencode is aware of the current project directory

## Usage

Open the **OpenCode** tool window at the bottom of the IDE. A session starts automatically in the project root directory.

| Button | Action |
|--------|--------|
| New Session | Open a new opencode session in a new tab |
| Stop | Terminate the current session |
| Clear | Clear the terminal screen |

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
