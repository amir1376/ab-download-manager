# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 1.5.8

### Added

- An option to allow update existing download from "Add Download" page if a duplicate is detected

### Fixed

- Crash when opening "Items" section of "Queues" page

### Improved

- Translations updated
- Duplicate download detection
- Minor UI/UX improvements

## 1.5.7

### Added

- Drag and Drop files to other categories or external applications

### Fixed

- "Parts Info" section in the "Download Progress" window does not expand for the first time

### Improved

- Translations Updated
- Improved UI rendering on Windows, resulting in higher FPS.
- Minor UI/UX Improvements

## 1.5.6

### Added

- Finnish Language Support
- An option to make the start time of queues optional
- An ability to edit saved checksums on the "File Checksum Checker" page'

### Changed

- The "Close" button in the "Download Progress" window has been renamed to "Cancel" (this stops the download and closes
  the window). To close the window without stopping the download, use the "X" button.

### Fixed

- An issue where filenames in email attachments were not captured correctly
- The updater wouldn't resume after the download was stopped
- "Open Folder" doesn’t work properly on Linux when the file name contains special characters.
- Changing settings in the 'Download Progress' window also affects other download items!

### Improved

- Translations updated
- "Download Progress" and "Queues" windows UI improvements
- Pressing "Download Browser Integration" the download page will be opened in the corresponding browser

## 1.5.5

### Added

- Japanese Language
- An option to automatically "Retry Failed Downloads" (Disabled by default for now)
- An option to Import/Export download credentials as curl command

### Fixed

- The download progress sometimes shows incorrect speeds and ETAs
- Some unverified hostnames can't be used when the "Ignore SSL Certificate" is enabled
- Startup on Boot issue in macOS
- Drag And Drop of links issue in macOS
- Some shortcuts didn't work properly in macOS
- System Tray didn't work in macOS

### Improved

- Translations updated
- Minor UI improvements
- App Icon size in macOS
- Override "About" dialog in macOS

## 1.5.4

### Added

- The app now supports full portability by creating an empty `.abdm` directory in the installation folder.
- An option to delete user data (configuration files) when using Windows Uninstaller.

### Fixed

- Unchecked "Use Category" didn't work as expected.

### Improved

- Download engine improvements.
- Translation updated.

## 1.5.3

### Added

- Vietnamese language.
- An option to "Select Queue" dialog to start the queue immediately.
- An option to allow user set custom User-agent in the settings.
- An option to not "Use Category" by default.
- An option to disable SSL Certificate Verification.
- An option to show/hide icon labels in the main toolbar (you can hover over them to see their labels).
- An option to not use System Tray.

### Fixed

- Sometimes Thread count not applied correctly.
- The download completion dialog appeared even when its option is disabled.
- Some servers return 256 Bytes instead of full size

### Improved

- Translations updated
- Minor UI improvements
- Use system language as default language
- Proxy Settings page improved

## 1.5.2

### Added

- An ability to validate downloads with File Checksum
- System Proxy support
- Proxy Auto Configuration (pac) support

### Changed

- Maximum allowed thread count has been increased

### Fixed

- Fixed the incorrect System Tray name on Linux.

### Improved

- Translations Updated
- Settings window size will be remembered now

## 1.5.1

### Added

- Italian Language
- German Language
- Georgian Language
- Indonesian Language
- An option to change download speed unit
- An ability to start new download using Rest-Api

### Fixed

- System tray in Linux now has correct icon and native option menu
- App crashes when changing theme in Linux
- Open file/folder action fails sometimes in Windows

### Improved

- Translations updated
- Split category and location configuration options in multi download page
- Home page minor UI improvements

## 1.5.0

### Added

- In App Update feature.
- An option to track deleted files on disk and remove them from the download list (either manually or automatically).
- Delete option to the Main toolbar.

### Changed

- UI Scale maximum value increased to 3x.

### Fixed

- When you change "Default download Folder", the "Download Location" of categories also updated (if they are inside "
  Default Download Location").
- Issue on the "Add Multiple Download" page where the "Save Mode" set to "All in Same Location" was not functioning as
  expected.
- App does not start on boot when installation path contains space.

### Improved

- Redesigned "About" Page.
- Translations updated.
- "Extra Config" section on the "Add Download" page was not displaying correctly in some languages.

## 1.4.4

### Added

- UI Scale option

### Changed

- The "Selection" cell in download list table can be hidden (optional)

### Fixed

- Improved "Open Folder" in Windows
- Support third party file managers in Windows
- "Download Progress" Window shows up even if the "Auto Show Progress Window" option is disabled
- Improved "Confirm Delete Download" UX
- Improved the readability of shortcut text in menus
- Improved sort of download list by status
- Resize handle moves in opposite direction for RTL languages
- Improved "Home" page
- Improved "About" page
- Updated translations

## 1.4.3

### Added

- "Download Completion" window
- "Exit Confirmation" dialog when there is active download
- An Option to automatically show "Download Completion" window (Optional)
- An Option to automatically show "Download Progress" window when user presses on "Resume" (Optional)

### Changed

- Default thread count is now 8
- Shape of filename of release binaries changed (added arch name after platform name)

### Fixed

- "Delete entire list" task does not remove all downloads
- Filename not detected correctly from some download servers
- Rename download changes state to paused if it was finished
- Improved installation script for linux
- Improved "Settings" page
- Translations updated

## 1.4.2

### Added

- Edit Download Page (Rename, Refresh links/credentials etc…)
- Translators Credit Page
- Traditional Chinese Language
- Spanish Language
- French Language
- Turkish Language

### Changed

- Updated translations

## 1.4.1

### Added

- Portuguese (Brazilian) Language

### Changed

- Updated some languages

### Fixed

- Language names not shown by their native names
- Selected language not saved properly
- Wrong text for "Close" button in "Batch Download" page

## 1.4.0

### Added

- Localization Support
- Persian Language
- Arabic Language
- Chinese (Simplified) Language
- Ukrainian Language
- Russian Language
- Albanian Language
- Bengali Language

### Changed

- Category Download Location is now optional

### Fixed

- A bug in Download Engine
- "Add Queue" page will be shown properly when opened from "Import List" page

## 1.3.0

### Added

- Proxy Support
- Categories now can have URL patterns

### Fixed

- Application freezes a while when we drag(and drop) a large file on it
- Improved category section in the home screen

## 1.2.0

- in this version we replaced Wix installer with Nsis for better customization and more control over the installation
  process in Windows.
- if you use Windows in order to install this version please first uninstall the previous msi version (your settings and
  downloads will be safe)

### Added

- You can now create and customize categories
- Pause/Resume in header actions

### Changed

- Change installer in Windows from Wix to Nsis
- Improved import link page

## 1.1.0

### Added

- Added Batch Download
- Added an option to merge TitleBar with MenuBar (disabled by default)
- Added two cli options --version, --exit

### Fixed

- Fixed Opening downloaded file creates a subprocess in Windows
- Fixed that some non-standard links not imported correctly
- Improved window custom decoration logic and title bar position
- Improved settings page

## 1.0.10

### Fixed

- Improve home page
- Improve download page
- Opening Directory Picker cause the app to crash in Linux
- Folder opened two times when clicking on open folder in Linux

## 1.0.9

### Added

- Sparse File Allocation

### Fixed

- Improve directory picker
- Improve show help UX in settings
- Improve Download Engine

## 1.0.8

### Added

- Use server Last-Modified time option in settings
- Show Open File button if new download already exists and completed

### Fixed

- Improved custom window decoration in linux
- When we click on open folder in linux it opens the file instead!
- Some URLEncoded filenames are not decoded properly

## 1.0.7

### Added

- support follow system Dark/Light mode
- auto paste link (if any) from clipboard when opening add url page

### Fixed

- App is now open in center of screen
- Some settings doesn't persist after app restart
- Download speed shows a high value incorrectly when we reopen the app window after a while
- Some files not downloaded correctly now fixed

## 1.0.6

### Added

- Add Community and Browser Integration links to the app menu

### Changed

- Change default download folder

### Fixed

- Exception will not throw anymore if System Tray is not supported by the OS

## 1.0.5

- Improve Download Engine

## 1.0.4

- Improved UI/UX in Download Page

## 1.0.3

- Download Info Page now show users that a download file supports resuming or not

### Fixed

- Download links that does not support resume now handled correctly
- Some Web pages does not download correctly

## 1.0.2

- Error messages improvements

### Fixed

- handle some webservers does not respect requested range at first place

## 1.0.1

- UI improvements

### Changed

- repository url updated

## 1.0.0

- This is the first release of the app

### Added

- Multi Connection File Download
- Speed limiter
- Download Queues
- Download Scheduler
- DownloadManager Browser Integration Support
- Dark/Light themes
