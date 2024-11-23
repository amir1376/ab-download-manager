# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

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
