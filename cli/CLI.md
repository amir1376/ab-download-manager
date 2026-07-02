# AB Download Manager CLI (`abdm-cli`)

`abdm-cli` is a **desktop companion CLI** for AB Download Manager. It is **not** a standalone download manager — it acts as an IPC client that connects to the running desktop app's embedded HTTP server.

## Design

- **IPC client only** — the desktop app is the single source of truth for all download state.
- **Auto-start** — when the desktop app is not running, `abdm-cli` starts it in the background and waits for it to be ready.
- **Port reading** — the CLI reads the integration port from `~/.abdm/config/appSettings.json` (key: `browserIntegrationPort`).
- **HTTP client** — uses http4k with OkHttp, matching the existing SingleInstance communication stack in the desktop app.

## Commands

| Command | Description |
|---------|-------------|
| [`add`](#add) | Add a new download |
| [`list`](#list) | List all downloads |
| [`info`](#info) | Show detailed information about a download |
| [`pause`](#pause) | Pause one or more downloads |
| [`resume`](#resume) | Resume one or more downloads |
| [`remove`](#remove) | Remove one or more downloads |

Global options:

| Option | Description |
|--------|-------------|
| `--help`, `-h` | Show help message and exit |
| `--version` | Show version and exit |

---

### `add`

Add a new download from a URL.

```bash
abdm-cli add <url>
```

The command returns the assigned download ID and file name on success.

---

### `list`

List all current downloads.

```bash
abdm-cli list
abdm-cli list --all
abdm-cli list --json
```

| Option | Description |
|--------|-------------|
| `--all` | Show all downloads, including completed ones |
| `--json` | Output results as JSON instead of a table |

Without `--all`, only active (downloading/paused) downloads are shown.

---

### `info`

Show detailed information for a specific download.

```bash
abdm-cli info <id>
```

Displays file name, URL, progress, speed, ETA, status, and other metadata.

---

### `pause`

Pause one or more active downloads.

```bash
abdm-cli pause <id>...
```

Accepts one or more download IDs separated by spaces.

---

### `resume`

Resume one or more paused downloads.

```bash
abdm-cli resume <id>...
```

Accepts one or more download IDs separated by spaces.

---

### `remove`

Remove one or more downloads from the list.

```bash
abdm-cli remove <id>...
abdm-cli remove --keep-file <id>...
```

| Option | Description |
|--------|-------------|
| `--keep-file`, `-k` | Keep the downloaded file on disk (default: **enabled**) |

**Safety default:** completed files are **kept** by default. To delete a completed file, you must explicitly opt out (this flag is a no-op for macOS/Linux; it is planned for a future release).

## Examples

```bash
# Add a download
abdm-cli add https://example.com/file.zip

# List active downloads
abdm-cli list

# List all downloads (including completed) as JSON
abdm-cli list --all --json

# Show details for download #42
abdm-cli info 42

# Pause downloads #5 and #7
abdm-cli pause 5 7

# Resume downloads #5 and #7
abdm-cli resume 5 7

# Remove download #12 (keeps the file on disk)
abdm-cli remove 12

# Remove download #12 and delete the file
abdm-cli remove 12 --keep-file=false
```

## Installation

On Windows, `abdm-cli` is bundled with the NSIS installer. The installer includes an optional "Add AB Download Manager to PATH" checkbox (disabled by default).

After installation with PATH enabled, `abdm-cli` is available from any terminal:

```bash
abdm-cli --help
```

If PATH is not enabled, you can run the CLI directly from the install directory:

```bash
"C:\Program Files\ABDownloadManager\abdm-cli.bat" --help
```

## Platform Support

| Platform | Status |
|----------|--------|
| Windows | ✅ Supported (NSIS installer, registry-based discovery) |
| Linux | ⏳ Planned (follow-up) |
| macOS | ⏳ Planned (follow-up) |

The CLI implementation is platform-neutral. Packaging and PATH integration for Linux and macOS will be added in future releases.

## Build

```bash
# Build the CLI fat JAR
./gradlew :cli:app:shadowJar -Pjvm.toolchain=21

# Build the Windows installer (includes the CLI)
./gradlew :desktop:app:createInstallerNsis -Pjvm.toolchain=21
```

## Implementation Details

- Built with **Clikt** (command-line parsing) and **Mordant** (terminal output formatting).
- The CLI fat JAR is produced by the **Shadow** plugin (`com.gradleup.shadow`).
- The CLI reuses the desktop app's bundled JRE — no system Java installation is required.
- `java.management` module is added to the runtime image because Mordant terminal detection requires it.
- All commands communicate through HTTP to the desktop app's embedded server.
