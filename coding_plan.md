# Quick Download — AI Coding Plan

> **Workflow:** Gemini CLI & OpenCode do the coding → Claude Opus reviews each batch.
> 
> Tasks are split so each AI tool works on **independent** parts of the codebase (no merge conflicts). After each batch completes, Claude Opus reviews ALL changed files for correctness.

---

## How to Use This Plan

1. **Copy-paste the task block** (including context & instructions) into Gemini CLI or OpenCode
2. Let it complete the work
3. After both tools finish a batch, **paste the review prompt** into Claude Opus
4. Fix any issues Claude flags, then move to the next batch

---

## Batch 1 — Backend Core (No UI)

> **Goal:** Build the backend pieces that Quick Download needs. No UI changes yet.

---

### 🟢 Gemini CLI — Task 1A: DownloadSystem Quick Download Methods

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Gradle, Decompose, Koin, Coroutines)
File to modify: shared/app/src/commonMain/kotlin/com/abdownloadmanager/shared/util/DownloadSystem.kt

This is a facade class for the download engine. It already has methods like:
- addDownload(newItemsToAdd, queueId, categorySelectionMode)
- addDownload(newDownload, queueId, categoryId)  
- removeDownload(id, alsoRemoveFile, context)
- manualResume(id), manualPause(id), reset(id)
- editDownload(id, applyUpdate, downloadJobExtraConfig)

Related types are in:
- integration/server/src/main/kotlin/com/abdownloadmanager/integration/DownloadCredentialsFromIntegration.kt
  (IDownloadCredentialsFromIntegration with link, downloadPage, suggestedName)
- integration/server/src/main/kotlin/com/abdownloadmanager/integration/NewDownloadTask.kt
  (NewDownloadTask with downloadSource, folder, name, queueId)
```

**Instructions:**
```
Add 3 new methods to DownloadSystem class:

1. `suspend fun quickDownload(link: String, suggestedName: String?, headers: Map<String, String>?, tempFolder: String): Long`
   - Create a new download item using the existing addDownload() pattern
   - Set the download folder to `tempFolder`
   - Set filename to suggestedName (or extract from URL if null)
   - Auto-start the download immediately (call manualResume)
   - Return the download ID

2. `suspend fun finalizeQuickDownload(downloadId: Long, finalName: String, finalFolder: String, queueId: Long?, categoryId: Long?)`
   - Get the download item by ID
   - If download is complete:
     - Move/copy the file from temp location to `finalFolder/finalName`
     - Update the download record's folder and name using editDownload()
   - If download is still in progress:
     - Update the download record's folder and name so that when it completes, 
       the file will be at the right location
   - Handle cross-filesystem moves (copy + delete fallback)

3. `suspend fun cancelQuickDownload(downloadId: Long)`
   - Pause the download via manualPause()
   - Remove the download and its temp file via removeDownload(id, alsoRemoveFile=true, ...)

Follow the existing code style exactly. Use the same imports, coroutine patterns, 
and error handling as the existing methods in this file.
Do NOT modify any other files.
```

---

### 🔵 OpenCode — Task 1B: Integration Server Endpoint + Handler

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Gradle, Decompose, Koin, Coroutines)

Files to modify:
1. integration/server/src/main/kotlin/com/abdownloadmanager/integration/IntegrationHandler.kt
2. integration/server/src/main/kotlin/com/abdownloadmanager/integration/Integration.kt

IntegrationHandler.kt is an interface with these methods:
- suspend fun addDownload(list, options)
- fun listQueues(): List<ApiQueueModel>
- suspend fun addDownloadTask(task: NewDownloadTask)

Integration.kt has a createServer() method that sets up HTTP endpoints:
- POST /add → calls integrationHandler.addDownload()
- GET /queues → calls integrationHandler.listQueues()
- POST /start-headless-download → calls integrationHandler.addDownloadTask()
- POST /ping → returns "pong"

Existing types:
- IDownloadCredentialsFromIntegration (link, headers, downloadPage, suggestedName)
- AddDownloadOptionsFromIntegration (silentAdd, silentStart)
- AddDownloadsFromIntegration (items list + options)
```

**Instructions:**
```
1. In IntegrationHandler.kt, add a new method to the interface:
   suspend fun quickDownload(
       list: List<IDownloadCredentialsFromIntegration>,
       options: AddDownloadOptionsFromIntegration,
   )

2. In Integration.kt, inside the createServer() method, add a new endpoint 
   BEFORE the /ping endpoint:

   post("/quick-download") {
       runBlocking {
           val itemsToAdd = kotlin.runCatching {
               val message = it.getBody().orEmpty()
               AddDownloadsFromIntegration.createFromRequest(
                   json = json,
                   jsonData = message
               )
           }
           itemsToAdd.onFailure { it.printStackTrace() }
           itemsToAdd.getOrThrow().let { newImportRequest ->
               integrationHandler.quickDownload(
                   newImportRequest.items,
                   newImportRequest.options,
               )
           }
       }
       MyResponse.Text("OK")
   }

Follow the EXACT same pattern as the existing /add endpoint.
Do NOT modify any other files.
```

---

### 🔴 Claude Opus — Review 1

**Paste this into Claude Opus after both tasks complete:**
```
Review the following files for correctness, consistency, and potential bugs.
This is an AB Download Manager project (Kotlin, Coroutines, Koin, Decompose).

Files changed:
1. shared/app/src/commonMain/kotlin/com/abdownloadmanager/shared/util/DownloadSystem.kt  
   — Added quickDownload(), finalizeQuickDownload(), cancelQuickDownload() methods
2. integration/server/src/main/kotlin/com/abdownloadmanager/integration/IntegrationHandler.kt
   — Added quickDownload() method to interface
3. integration/server/src/main/kotlin/com/abdownloadmanager/integration/Integration.kt
   — Added POST /quick-download endpoint

Check for:
- Correct use of existing DownloadSystem patterns (addDownload, editDownload, removeDownload)
- File move/rename correctness (cross-filesystem handling)
- Thread safety / coroutine context correctness
- Consistent error handling
- The new endpoint follows the exact same pattern as /add
- No missing imports
- Interface method is properly defined

Provide specific fixes if anything is wrong.
```

---

## Batch 2 — Quick Download Dialog UI

> **Goal:** Build the Quick Download dialog component and wire it into navigation.

---

### 🟢 Gemini CLI — Task 2A: QuickDownloadComponent (Logic Layer)

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Compose Desktop, Decompose, Koin, Coroutines)

Reference files to study BEFORE coding:
- desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/addDownload/single/DesktopAddSingleDownloadComponent.kt
  (Example of how a Decompose component is structured for a download dialog)
- shared/app/src/commonMain/kotlin/com/abdownloadmanager/shared/util/DownloadSystem.kt
  (Has the new quickDownload/finalizeQuickDownload/cancelQuickDownload methods from Batch 1)
- desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/AppComponent.kt
  (Shows how components are created and managed)

The app uses Decompose for navigation and Koin for DI.
```

**Instructions:**
```
Create a NEW file:
desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/quickdownload/QuickDownloadComponent.kt

This component manages the state and logic for the Quick Download dialog.

class QuickDownloadComponent(
    componentContext: ComponentContext,
    private val downloadSystem: DownloadSystem,
    private val initialDownloadId: Long,
    private val initialUrl: String,
    private val initialName: String,
    private val initialFolder: String,
    private val onFinish: () -> Unit,
) : ComponentContext by componentContext {

    val fileName = MutableStateFlow(initialName)
    val saveFolder = MutableStateFlow(initialFolder)
    val selectedQueueId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = MutableStateFlow<Long?>(null)
    val downloadId: Long = initialDownloadId
    
    // Observe download progress from downloadSystem for this downloadId
    // (use downloadSystem's existing monitoring capabilities)
    
    fun updateFileName(name: String) { fileName.value = name }
    fun updateSaveFolder(folder: String) { saveFolder.value = folder }
    fun updateQueueId(id: Long?) { selectedQueueId.value = id }
    fun updateCategoryId(id: Long?) { selectedCategoryId.value = id }
    
    fun confirm() {
        // Call downloadSystem.finalizeQuickDownload() with current values
        // Then call onFinish()
    }
    
    fun cancel() {
        // Call downloadSystem.cancelQuickDownload()
        // Then call onFinish()
    }
}

Study the existing DesktopAddSingleDownloadComponent.kt to match the patterns 
(imports, coroutine scope usage, ComponentContext delegation).
Do NOT modify any existing files.
```

---

### 🔵 OpenCode — Task 2B: QuickDownloadPage UI + Window

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Compose Desktop, Decompose)

Reference files to study BEFORE coding:
- desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/addDownload/single/AddDownloadPage.kt
  (Example Compose UI for add download — study layout, theming, widget usage)
- desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/addDownload/ShowAddDownloadDialogs.kt
  (Example of how a dialog window is managed — CustomWindow, WindowTitle, etc.)

The QuickDownloadComponent (being built separately) has these state flows:
- fileName: MutableStateFlow<String>
- saveFolder: MutableStateFlow<String>
- selectedQueueId: MutableStateFlow<Long?>
- selectedCategoryId: MutableStateFlow<Long?>
- downloadId: Long
And these methods: confirm(), cancel(), updateFileName(), updateSaveFolder()
```

**Instructions:**
```
Create 2 NEW files:

FILE 1: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/quickdownload/QuickDownloadPage.kt

A compact Compose UI with:
- File name text field (editable, bound to component.fileName)
- Save folder row with path display + browse button
- A progress indicator showing download is active
- Two buttons: "Confirm" (calls component.confirm()) and "Cancel" (calls component.cancel())
- Keep it compact — this should be a small, fast dialog (~400x220dp)
- Follow the SAME theming, widget imports, and styling as AddDownloadPage.kt

FILE 2: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/quickdownload/ShowQuickDownloadDialog.kt

A window wrapper following the EXACT same pattern as ShowAddDownloadDialogs.kt:
- CustomWindow with always-on-top
- Compact size: 420x230 dp
- Center position
- WindowTitle set to "Quick Download"
- WindowIcon using MyIcons.appIcon
- Calls PlatformAppActivator.active() in LaunchedEffect

Do NOT modify any existing files.
```

---

### 🔴 Claude Opus — Review 2

**Paste this into Claude Opus:**
```
Review these NEW files for correctness and consistency with the existing codebase:

1. desktop/app/.../quickdownload/QuickDownloadComponent.kt (logic layer)
2. desktop/app/.../quickdownload/QuickDownloadPage.kt (Compose UI)
3. desktop/app/.../quickdownload/ShowQuickDownloadDialog.kt (window wrapper)

Reference existing patterns in:
- desktop/app/.../addDownload/single/DesktopAddSingleDownloadComponent.kt
- desktop/app/.../addDownload/single/AddDownloadPage.kt
- desktop/app/.../addDownload/ShowAddDownloadDialogs.kt

Check for:
- Correct Decompose ComponentContext usage
- Correct Compose state collection (collectAsState)
- Consistent imports and theming with the rest of the project
- Proper coroutine scope management in component
- Window sizing and positioning matches the pattern
- No missing imports or compilation errors
- UI is compact and functional

Provide specific fixes if anything is wrong.
```

---

## Batch 3 — Wiring & Settings

> **Goal:** Connect everything together in AppComponent and add settings.

---

### 🟢 Gemini CLI — Task 3A: Wire Quick Download into AppComponent

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Decompose, Koin)

File to modify: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/AppComponent.kt

This is a large file (~1230 lines). It manages all dialogs/pages using Decompose slots.
Key patterns to follow:
- It implements IntegrationHandler interface
- It uses childSlot() for dialogs (see how edit download, settings, etc. are managed)
- Each dialog has: open/close methods, a @Serializable config class, and a childSlot

The IntegrationHandler interface now has a new method:
  suspend fun quickDownload(list, options)

The QuickDownloadComponent class (from Batch 2) takes:
  componentContext, downloadSystem, initialDownloadId, initialUrl, initialName, 
  initialFolder, onFinish

DownloadSystem (injected via Koin) now has:
  quickDownload(link, suggestedName, headers, tempFolder): Long
```

**Instructions:**
```
In AppComponent.kt, add Quick Download dialog support:

1. Add a @Serializable data class QuickDownloadConfig(val downloadId: Long, val url: String, 
   val name: String, val folder: String) inside AppComponent

2. Add a childSlot for quick download dialog (follow the exact same pattern 
   as the existing edit download dialog slot)

3. Add openQuickDownloadDialog(downloadId, url, name, folder) method

4. Add closeQuickDownloadDialog() method

5. Implement the IntegrationHandler.quickDownload() method:
   - Take the first item from the list
   - Determine temp folder (from settings or default to a temp-downloads dir in app data)
   - Call downloadSystem.quickDownload() to start the download immediately
   - Call openQuickDownloadDialog() with the returned download ID
   
Match the EXACT patterns used for other dialogs. Study how openEditDownloadDialog, 
openSettings, etc. work before making changes.
Only modify AppComponent.kt.
```

---

### 🔵 OpenCode — Task 3B: Quick Download Settings

**Context to provide:**
```
Project: AB Download Manager (Kotlin, Compose Desktop)

Study these files first:
- desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/pages/settings/
  (All files — understand how settings are structured)
- shared/config/ directory
  (Understand how config/preferences are stored)

The Quick Download settings need these fields:
- enabled: Boolean (default true)
- autoStartInBackground: Boolean (default true)
- tempDownloadFolder: String? (null = use default)
- clipboardMonitoringEnabled: Boolean (default false)
```

**Instructions:**
```
1. Create a new config class for Quick Download settings. Study how existing 
   settings/preferences are stored in the shared/config module and follow the 
   same pattern exactly.

   File: shared/config/src/commonMain/kotlin/.../QuickDownloadConfig.kt
   (put it next to existing config files)

   Fields:
   - quickDownloadEnabled: Boolean = true
   - autoStartInBackground: Boolean = true
   - tempDownloadFolder: String? = null
   - clipboardMonitoringEnabled: Boolean = false

2. Add a "Quick Download" section to the settings page.
   Follow the exact same pattern as existing settings sections.
   Include:
   - Toggle for "Enable Quick Download"
   - Toggle for "Auto-start downloads in background"
   - Folder picker for "Temp Download Folder" (show default path when null)
   - Toggle for "Clipboard Monitoring"

Study the existing settings code thoroughly before making changes.
Only create/modify settings-related files.
```

---

### 🔴 Claude Opus — Review 3

**Paste this into Claude Opus:**
```
Review these changes for correctness. This is the final wiring that connects 
Quick Download to the app.

Files changed:
1. desktop/app/.../AppComponent.kt — Added quick download dialog slot, 
   open/close methods, IntegrationHandler.quickDownload() implementation
2. shared/config/... — New QuickDownloadConfig class
3. Settings page files — New Quick Download settings section

Check for:
- Decompose childSlot is correctly configured with serializable config
- IntegrationHandler.quickDownload() correctly starts download and opens dialog
- Settings follow the same pattern as existing settings
- Config class follows existing config patterns  
- No circular dependencies or missing Koin bindings
- Everything compiles together with Batch 1 and Batch 2 changes
- The temp folder setting is properly wired to DownloadSystem.quickDownload()

Provide specific fixes if anything is wrong.
```

---

## Batch 4 — Clipboard & Tray (P1 Features)

> **Goal:** Add clipboard monitoring and tray enhancements. Only do this after Batches 1-3 are reviewed and working.

---

### 🟢 Gemini CLI — Task 4A: Clipboard Monitor Service

**Instructions:**
```
Create 2 NEW files:

FILE 1: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/clipboard/ClipboardMonitor.kt

A coroutine-based clipboard monitor that:
- Polls the system clipboard every 1.5 seconds using java.awt.Toolkit.getSystemClipboard()
- Detects new HTTP/HTTPS URLs
- Emits detected URLs via a SharedFlow<DetectedUrl>
- Has start()/stop()/isRunning() methods
- Debounces: doesn't re-emit the same URL within 30 seconds
- Runs on Dispatchers.IO for clipboard access

data class DetectedUrl(val url: String, val suggestedFilename: String?, val timestamp: Long)

FILE 2: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/clipboard/ClipboardMonitorSettings.kt

A simple settings holder that reads from the QuickDownloadConfig (from Batch 3):
- isEnabled: Boolean
- pollingInterval: Long (ms)

Do NOT modify any existing files.
```

---

### 🔵 OpenCode — Task 4B: Tray Menu Enhancement

**Instructions:**
```
File to modify: desktop/app/src/main/kotlin/com/abdownloadmanager/desktop/ui/widget/Tray.kt

Study the existing tray implementation, then add:
- "Quick Download from Clipboard" menu item 
  (when clicked, reads clipboard, if URL found → triggers quick download)
- Separator line before the new item

Follow the exact existing pattern for menu items.
Only modify Tray.kt.
```

---

### 🔴 Claude Opus — Review 4

```
Review clipboard monitoring and tray changes:
1. ClipboardMonitor.kt — Coroutine-based clipboard polling
2. ClipboardMonitorSettings.kt — Settings integration
3. Tray.kt — New menu items

Check for: thread safety, clipboard API correctness on Linux, 
proper coroutine cancellation, no memory leaks, proper debouncing logic.
```

---

## Final Review — Full Integration Check

### 🔴 Claude Opus — Final Review

**After ALL batches are complete and individually reviewed:**
```
Do a FULL review of the Quick Download feature across all files.

New files created:
- shared/app/.../DownloadSystem.kt (modified — 3 new methods)
- integration/server/.../IntegrationHandler.kt (modified — 1 new method)
- integration/server/.../Integration.kt (modified — 1 new endpoint)
- desktop/app/.../quickdownload/QuickDownloadComponent.kt (new)
- desktop/app/.../quickdownload/QuickDownloadPage.kt (new)
- desktop/app/.../quickdownload/ShowQuickDownloadDialog.kt (new)
- desktop/app/.../AppComponent.kt (modified — dialog wiring)
- shared/config/.../QuickDownloadConfig.kt (new)
- Settings page (modified)
- desktop/app/.../clipboard/ClipboardMonitor.kt (new)
- desktop/app/.../clipboard/ClipboardMonitorSettings.kt (new)
- desktop/app/.../ui/widget/Tray.kt (modified)

Check the FULL flow end-to-end:
1. Browser extension sends POST /quick-download → Integration server → IntegrationHandler → AppComponent
2. AppComponent calls DownloadSystem.quickDownload() → download starts in temp folder
3. AppComponent opens QuickDownloadComponent → dialog appears with progress
4. User edits name/folder → confirms → finalizeQuickDownload() moves file
5. OR user cancels → cancelQuickDownload() cleans up

Also check: Koin dependency graph, Decompose navigation correctness,
all imports resolve, no unused code, thread safety, error handling.
```

---

## Quick Reference — Tool Assignment Summary

| Batch | Gemini CLI | OpenCode | Claude Opus |
|-------|-----------|----------|-------------|
| 1 | DownloadSystem methods | Integration endpoint | Review backend |
| 2 | QuickDownloadComponent | UI + Window | Review dialog |
| 3 | AppComponent wiring | Settings | Review wiring |
| 4 | Clipboard monitor | Tray menu | Review P1 features |
| Final | — | — | Full integration review |

> **Rule:** Never move to the next batch until Claude Opus approves the current batch.
