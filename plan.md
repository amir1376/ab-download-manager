# Quick Download Feature Implementation Plan

## Overview

This document outlines the implementation plan for adding a "Quick Download" feature similar to IDM (Internet Download Manager) in AB Download Manager. The feature aims to reduce the download initiation time from 30-40 seconds to under 5 seconds.

## Current Problem

- User clicks a download link in browser
- AB Download Manager dialog box opens with URL and name already in place
- User needs to select category, folder where download will be saved
- Clicks download button, then download starts
- **Total time: 30-40 seconds**

## Target Behavior

- User clicks a download link in browser
- AB Download Manager immediately shows a compact quick-download dialog with URL and name already in place
- **Download auto-starts in the background immediately** into a temp location as soon as the dialog appears
- While the download progresses in the background, the user can change the filename, folder, or category at their leisure
- Once the download completes, it is automatically renamed to the user-chosen name and moved to the selected folder
- **Total time: 1-2 seconds to start downloading; user can adjust details while download happens**

---

## Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  Browser        │     │  Integration     │     │  Quick Download     │
│  Extension      │────▶│  Server (HTTP)   │────▶│  Dialog             │
│  (or Clipboard) │     │                  │     │  (auto-start in bg) │
└─────────────────┘     └──────────────────┘     └─────────────────────┘
                                                         │
                                                   ┌─────┴─────┐
                                                   ▼           ▼
                                              ┌─────────┐ ┌──────────┐
                                              │ Temp DL  │ │ User     │
                                              │ (bg)     │ │ Edits    │
                                              └────┬─────┘ └────┬─────┘
                                                   │            │
                                                   ▼            ▼
                                              ┌──────────────────────┐
                                              │ Rename + Move to     │
                                              │ final folder         │
                                              └──────────────────────┘
```

**Key Difference from Previous Plan:** The download starts *immediately* in a temporary location. The dialog is for post-start adjustments. On completion, the file is renamed and moved.

---

## Feature Breakdown

### 1. Quick Download Dialog (Core Feature)

A lightweight, fast-appearing dialog that starts the download immediately and lets the user adjust details while download progresses.

**UI Components:**
- File name (auto-detected, editable)
- Save location (with quick-select buttons)
- Queue selection (dropdown with recent queues)
- Download progress indicator (shows download is already running)
- "Apply & Close" button (applies changes and closes dialog)
- "Advanced" button (opens full add-download dialog — pauses bg download and transfers it)
- Cancel button (cancels the background download)

**Behavior:**
- Appears within 500ms of download request
- **Immediately starts downloading** to a temp folder in background
- Pre-fills with smart defaults (last-used folder, auto-detected filename)
- Keyboard shortcuts: Enter to confirm, Esc to cancel
- When user closes the dialog (confirm), the download continues and on completion is renamed/moved to final path
- If user cancels, the temp download is aborted and temp file is cleaned up

**Location:** `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/quickdownload/`

### 2. Temp Download → Rename/Move Mechanism

The core workflow that makes Quick Download fast:

1. On dialog open, immediately call `DownloadSystem.addDownload()` with a **temp folder** and a **temp filename**
2. Download starts in background using existing download engine
3. User adjusts settings in dialog (name, folder, category, queue)
4. On download completion:
   - Rename file to user-chosen name
   - Move file from temp folder to user-chosen folder
5. Update the download record in the system with final path

**Implementation:**
- Add `quickDownload()` method to `DownloadSystem` that:
  - Creates a download in a temp directory (e.g., `<app-data>/temp-downloads/`)
  - Returns the download ID immediately
  - Starts the download automatically
- Add a `finalizeQuickDownload(id, finalName, finalFolder)` method to `DownloadSystem` that:
  - Waits for download completion (or is invoked by a listener)
  - Renames and moves the file
  - Updates the internal download record

**Location:** `shared/app/src/commonMain/kotlin/com/abdownloadmanager/shared/util/DownloadSystem.kt`

### 3. Smart Default Values

Eliminate user input by using intelligent defaults:

| Setting | Default Strategy |
|---------|------------------|
| Save Location | Last used folder for this file type, or user-configured default |
| Queue | Last used queue |
| Filename | Extract from URL / Content-Disposition header (already partially exists in `IDownloadCredentialsFromIntegration.suggestedName`) |
| Category | Detect from URL/file extension (existing category system) |

**Implementation:**
- Create `QuickDownloadDefaults` class in shared config
- Store last-used values in preferences
- Leverage existing file extension → category mapping from the category system

### 4. Browser Integration Enhancement

The existing HTTP server (`Integration.kt`) already has endpoints. Enhance it:

**Current Endpoints (in `Integration.createServer()`):**
- `POST /add` — Add download (opens UI dialog)
- `GET /queues` — Get queue list
- `POST /start-headless-download` — Start download without UI
- `POST /ping` — Health check

**New Endpoint:**
- `POST /quick-download` — Triggers quick download dialog with auto-start behavior

**Existing Infrastructure to Leverage:**
- `IDownloadCredentialsFromIntegration` already carries `link`, `headers`, `downloadPage`, `suggestedName`
- `AddDownloadOptionsFromIntegration` already has `silentAdd` and `silentStart` flags
- `IntegrationHandler` interface needs a new method: `quickDownload()`

**Files to modify:**
- `integration/server/src/main/kotlin/com/abdownloadmanager/integration/Integration.kt`
- `integration/server/src/main/kotlin/com/abdownloadmanager/integration/IntegrationHandler.kt`

### 5. Clipboard Monitoring (P1 — Phase 2)

Auto-detect when a URL is copied and offer to download:

#### Option A: Background Monitoring (Recommended)
- Monitor clipboard every 1-2 seconds
- Detect valid URLs (http/https)
- Show non-intrusive notification: "URL detected: [filename] — Click to download"
- Click notification → Opens quick download dialog (with auto-start)

#### Option B: Manual Trigger
- Global hotkey (e.g., Ctrl+Shift+D)
- Tray menu option

**Implementation:**
- Create `ClipboardMonitor` service
- Use `java.awt.Toolkit.getSystemClipboard()`
- Add setting to enable/disable

**Location:** `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/clipboard/`

### 6. System Tray Enhancement

Enhance the existing tray widget (`desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/ui/widget/Tray.kt`):

- Quick download from clipboard
- Recent downloads (last 5)
- Pause all / Resume all

---

## Implementation Steps

### Phase 1: Core Quick Download (Weeks 1-2)

#### 1.1 Add Quick Download Support to DownloadSystem

**File:** `shared/app/src/commonMain/kotlin/com/abdownloadmanager/shared/util/DownloadSystem.kt`

**Tasks:**
- [ ] Add `quickDownload(credentials: IDownloadCredentialsFromIntegration): Long` method
  - Creates download in temp directory
  - Auto-starts immediately
  - Returns download ID
- [ ] Add `finalizeQuickDownload(id: Long, finalName: String, finalFolder: String)` method
  - Moves completed file to final location
  - Updates download record
- [ ] Add `cancelQuickDownload(id: Long)` method
  - Stops download and cleans up temp file

#### 1.2 Add Quick Download Endpoint to Integration Server

**Files:**
- `integration/server/src/main/kotlin/com/abdownloadmanager/integration/IntegrationHandler.kt`
- `integration/server/src/main/kotlin/com/abdownloadmanager/integration/Integration.kt`

**Tasks:**
- [ ] Add `suspend fun quickDownload(items: List<IDownloadCredentialsFromIntegration>, options: AddDownloadOptionsFromIntegration)` to `IntegrationHandler` interface
- [ ] Add `POST /quick-download` handler in `Integration.createServer()`
- [ ] Implement the handler in the `AppComponent` (which implements `IntegrationHandler`)

#### 1.3 Create Quick Download Dialog UI

**New files under `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/quickdownload/`:**

```
quickdownload/
├── QuickDownloadComponent.kt      # Decompose component (state + logic)
├── QuickDownloadPage.kt           # Compose UI
└── ShowQuickDownloadDialog.kt     # Window management (follows ShowAddDownloadDialogs.kt pattern)
```

**Tasks:**
- [ ] Create `QuickDownloadComponent` extending Decompose component pattern
  - Holds download ID (from auto-started download)
  - Manages editable state: filename, folder, queue, category
  - Provides methods: `confirm()`, `cancel()`, `openAdvanced()`
- [ ] Create compact Compose UI in `QuickDownloadPage.kt`
  - Show file name (editable)
  - Show save folder (with browse button)
  - Show download progress bar
  - Confirm / Cancel / Advanced buttons
- [ ] Create window wrapper in `ShowQuickDownloadDialog.kt` following `ShowAddDownloadDialogs.kt` pattern
  - Compact window size (~400x200)
  - Always on top
  - Center on screen
- [ ] Register in `AppComponent` with Decompose slot/router

#### 1.4 Wire Up Navigation

**File:** `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/AppComponent.kt`

**Tasks:**
- [ ] Add `openQuickDownloadDialog(...)` method
- [ ] Add `closeQuickDownloadDialog()` method
- [ ] Add Decompose slot configuration for quick download dialogs
- [ ] Implement `IntegrationHandler.quickDownload()` to open the dialog + start bg download

### Phase 2: Smart Defaults & Clipboard (Weeks 3-4)

#### 2.1 Default Resolution Engine

**New files under `shared/config/`:**

```
shared/config/src/commonMain/kotlin/.../defaults/
├── QuickDownloadDefaults.kt       # Default value resolution
└── DefaultLocationResolver.kt     # Save location logic
```

**Tasks:**
- [ ] Create preferences keys for last-used folder, queue, etc.
- [ ] Implement file extension → category mapping using existing category system
- [ ] Add "Remember my choice" per domain option

#### 2.2 Clipboard Monitor Service

**New files under `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/clipboard/`:**

```
clipboard/
├── ClipboardMonitor.kt            # Main monitor class (coroutine-based)
└── ClipboardMonitorSettings.kt    # Enable/disable settings
```

**Tasks:**
- [ ] Implement clipboard monitoring with coroutines (poll every 1.5s)
- [ ] Add URL validation
- [ ] Add debouncing (avoid repeated detection of same URL)
- [ ] Create toast notification for detected URLs
- [ ] Add setting to enable/disable in Settings page

### Phase 3: Settings & Polish (Week 5)

#### 3.1 Quick Download Settings

**File:** `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/settings/`

```
Settings
├── Quick Download
│   ├── Enable Quick Download Dialog [toggle]
│   ├── Default Save Location [folder picker]
│   ├── Temp Download Folder [folder picker] (default: <app-data>/temp-downloads/)
│   ├── Default Queue [dropdown]
│   ├── Auto-start downloads [toggle] (on by default for quick download)
│   ├── Show notification for clipboard URLs [toggle]
│   └── Keyboard Shortcut [key picker]
```

> **Temp Download Folder:** Users can select a custom temporary folder where quick downloads are stored while in progress. This is useful if the default app-data location is on a small drive, and the user wants temp files on a larger/faster disk. Falls back to `<app-data>/temp-downloads/` if not set.

#### 3.2 System Tray Enhancement

**File:** `desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/ui/widget/Tray.kt`

- [ ] Add "Quick Download from Clipboard" menu item
- [ ] Add "Recent Downloads" submenu

---

## File Changes Summary

### New Files to Create

| File | Purpose |
|------|---------|
| `desktop/app/.../quickdownload/QuickDownloadComponent.kt` | Decompose component for quick download dialog |
| `desktop/app/.../quickdownload/QuickDownloadPage.kt` | Compose UI for compact dialog |
| `desktop/app/.../quickdownload/ShowQuickDownloadDialog.kt` | Window wrapper (follows existing pattern) |
| `desktop/app/.../clipboard/ClipboardMonitor.kt` | Clipboard URL monitoring service |
| `desktop/app/.../clipboard/ClipboardMonitorSettings.kt` | Clipboard settings |
| `shared/config/.../defaults/QuickDownloadDefaults.kt` | Default value management |

### Files to Modify

| File | Changes |
|------|---------|
| `integration/server/.../Integration.kt` | Add `POST /quick-download` endpoint in `createServer()` |
| `integration/server/.../IntegrationHandler.kt` | Add `quickDownload()` method to interface |
| `shared/app/.../DownloadSystem.kt` | Add `quickDownload()`, `finalizeQuickDownload()`, `cancelQuickDownload()` methods |
| `desktop/app/.../AppComponent.kt` | Register quick download dialog, implement `IntegrationHandler.quickDownload()` |
| `desktop/app/.../ui/widget/Tray.kt` | Add quick download tray menu items |
| Settings page files | Add Quick Download settings section |

---

## API Design

### DownloadSystem Additions

```kotlin
// In DownloadSystem.kt

/**
 * Start a quick download immediately in temp directory.
 * Returns the download ID for tracking.
 */
suspend fun quickDownload(
    credentials: IDownloadCredentialsFromIntegration,
    tempFolder: String = "<app-data>/temp-downloads/"
): Long {
    // 1. Create download item pointing to tempFolder
    // 2. Start download immediately
    // 3. Return download ID
}

/**
 * Finalize a quick download: rename file and move to final location.
 * Called when user confirms settings or when download completes.
 */
suspend fun finalizeQuickDownload(
    downloadId: Long,
    finalName: String,
    finalFolder: String,
    categoryId: Long?,
    queueId: Long?
) {
    // 1. Wait for download if still in progress (or set up listener)
    // 2. Rename temp file to finalName
    // 3. Move from temp folder to finalFolder
    // 4. Update the download record in database
}

/**
 * Cancel a quick download: stop download and clean up temp file.
 */
suspend fun cancelQuickDownload(downloadId: Long) {
    // 1. Stop the download
    // 2. Delete temp file
    // 3. Remove download record
}
```

### IntegrationHandler Addition

```kotlin
// In IntegrationHandler.kt

interface IntegrationHandler {
    // ... existing methods ...
    
    suspend fun quickDownload(
        list: List<IDownloadCredentialsFromIntegration>,
        options: AddDownloadOptionsFromIntegration,
    )
}
```

### Quick Download Component

```kotlin
// In QuickDownloadComponent.kt

class QuickDownloadComponent(
    componentContext: ComponentContext,
    private val downloadSystem: DownloadSystem,
    private val defaults: QuickDownloadDefaults,
    private val downloadId: Long,               // ID of auto-started download
    private val onClose: () -> Unit,
    private val onOpenAdvanced: () -> Unit,
) : ComponentContext by componentContext {
    
    // Editable state
    val fileName = MutableStateFlow<String>(...)
    val saveFolder = MutableStateFlow<String>(...)
    val selectedQueue = MutableStateFlow<Long?>(null)
    val selectedCategory = MutableStateFlow<Long?>(null)
    
    // Download progress (observed from DownloadSystem)
    val downloadProgress: StateFlow<Float>
    val downloadStatus: StateFlow<DownloadStatus>
    
    fun confirm() {
        // Apply user's choices → finalizeQuickDownload()
        onClose()
    }
    
    fun cancel() {
        // cancelQuickDownload()
        onClose()
    }
    
    fun openAdvanced() {
        // Transfer to full add-download dialog
        onOpenAdvanced()
    }
}
```

### Clipboard Monitor

```kotlin
// In ClipboardMonitor.kt

class ClipboardMonitor(
    private val scope: CoroutineScope,
    private val settings: ClipboardMonitorSettings
) {
    val urlDetected: Flow<DetectedUrl>
    
    fun start()
    fun stop()
    fun isRunning(): Boolean
}

data class DetectedUrl(
    val url: String,
    val suggestedFilename: String?,
    val timestamp: Long
)
```

---

## Testing Plan

### Manual Testing (Primary)
1. **Quick Dialog Appearance:**
   - Send a POST request to `http://localhost:<port>/quick-download` with a test URL
   - Verify dialog appears within 500ms
   - Verify download starts automatically in background
2. **Rename/Move on Completion:**
   - Change filename and folder in quick dialog
   - Wait for download to complete
   - Verify file appears at the new location with the correct name
3. **Cancel Flow:**
   - Start a quick download
   - Click cancel
   - Verify temp file is cleaned up
4. **Clipboard Monitoring:**
   - Enable clipboard monitoring in settings
   - Copy a URL to clipboard
   - Verify toast notification appears
   - Click "Download" on toast → verify quick download dialog opens

### Unit Tests (If Test Infrastructure Exists)
- Default value resolution logic
- URL validation
- Filename extraction from URL
- Temp folder path generation

### Performance Tests
- Dialog appears within 500ms
- Memory usage with clipboard monitoring
- Startup time with/without the feature

---

## Configuration Options

```kotlin
data class QuickDownloadSettings(
    val enabled: Boolean = true,
    val autoStartInBackground: Boolean = true,  // Core feature toggle
    val showNotifications: Boolean = true,
    val defaultSaveLocation: String? = null,
    val defaultQueueId: Long? = null,
    val clipboardMonitoringEnabled: Boolean = false,  // Opt-in
    val clipboardMonitoringInterval: Long = 1500L,    // ms
    val rememberSettingsPerHost: Boolean = true,
    val tempDownloadFolder: String? = null,           // User-chosen temp folder; null = use default (<app-data>/temp-downloads/)
    val dialogTimeout: Int = 0  // 0 = no timeout (keep dialog open)
)
```

---

## Future Enhancements (Out of Scope)

1. **Batch Quick Download** — Handle multiple URLs at once
2. **Scheduled Downloads** — Queue for later
3. **Smart Category Detection** — AI-based category suggestions
4. **Download Templates** — Save/load download configurations
5. **Browser Extension v2** — Native messaging / WebSocket for deeper integration
6. **Per-Host Settings** — Remember download settings per domain

---

## Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Download start time | 30-40 seconds | 1-2 seconds |
| Dialog appearance time | N/A | < 500ms |
| Number of clicks to start | 5-6 | 0 (auto-start) |
| User satisfaction | Baseline | +50% |

---

## Dependencies

- **Decompose** — Navigation (already in use)
- **Koin** — DI (already in use)
- **Kotlin Coroutines** — Async (already in use)
- **Compose** — UI (already in use)
- **Java Clipboard API** — Clipboard access (already available)
- **System Tray** — Tray integration (already in `Tray.kt`)

No new external dependencies required.

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| Temp file accumulation if app crashes | Add cleanup on startup — delete orphaned temp files |
| File move fails (cross-filesystem) | Use copy+delete fallback instead of rename |
| Clipboard monitoring drains battery | Opt-in toggle, pause when on battery |
| Browser extension communication fails | Fallback to manual URL entry / existing `/add` endpoint |
| Default folder doesn't exist | Validate and create on first use |
| Concurrent downloads overload system | Respect global queue limits |
| Privacy concerns with clipboard | Explicit opt-in, clear indicator when active |
| Race condition: user changes name while download in progress | Only apply rename/move *after* download completes |

---

## Implementation Priority

1. **P0 — Must Have**
   - Quick Download Dialog with auto-start
   - Temp download → rename/move mechanism
   - Browser Integration (`/quick-download` endpoint)
   - Smart Defaults

2. **P1 — Should Have**
   - Clipboard Monitoring
   - System Tray Quick Options

3. **P2 — Nice to Have**
   - Per-Host Settings
   - Sound Notifications
   - Keyboard Shortcuts

---

## Notes

- This feature aligns with AB Download Manager's goal of being a free, open-source alternative to IDM
- The implementation maintains backward compatibility with existing download flows (the `/add` endpoint is unchanged)
- The **core innovation** is auto-starting the download in a temp location immediately, letting the user adjust settings while download progresses
- All new features respect user privacy and include clear opt-in mechanisms
- The existing `AddDownloadOptionsFromIntegration` `silentAdd`/`silentStart` flags can be leveraged as a foundation
- Documentation should be updated to explain the new quick download feature
