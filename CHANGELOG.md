# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 1.8.4

### Added

- In-app browser for Android
- The Android service now tells the user why it is running
- The Add-Multi-Download page can now filter downloads using search and wildcards

### Fixed

- Random app crashes on some Android devices caused by service-related issues
- Issues with the in-app update feature on some Android devices

### Improved

- Updated translations
- The Android Foreground Service is now only used when necessary (active downloads, active queues, scheduled queues) and
  automatically stops after inactivity
- Add-Multi-Download page UI/UX improvements

## 1.8.3

### Added

- Ability to sort and remove queue items on Android (#996)
- A new shortcut to open the download list from the download progress dialog (#1001)

### Fixed

- Download table state not saved properly on desktop (#999)
- Update related notifications appearing repeatedly on android (#998)

### Improved

- Updated translations
- Settings Page UI improvements on android (#990)

## 1.8.2

### Fixed

- Resolved issues with the In-App update feature on some android devices
- Disabled notification badges on the launcher icon on android
- The application crashes on some devices (desktops) because of an issue in system theme detection logic

### Improved

- Updated translations
- Added tooltips for action buttons
- Display selected count in the "Add Multi Download" page on desktop (#970)
- Reduced battery consumption
- Various UI/UX enhancements

## 1.8.1

### Fixed

- Android 10 storage access issue that caused download errors (#977)

### Improved

- Updated translations
- Better support for adaptive icons on Android (#978)
- Improved directory picker on Android (#979)
- Slightly reduced application size

## 1.8.0

### Added

- Android Support
- macOS users can now use homebrew to install/update the application

### Fixed

- Some HLS streams are not recognized properly

### Improved

- Updated translations
- UI improvements

## 1.7.1

### Added

- Support for custom data directory (#895)

### Fixed

- System shortcuts not working on the main page (#885)
- Segment info display issues
- Suggested file names from browsers are now automatically corrected before use (#896)

### Improved

- Translations updated
- Download list UI improvements (#897)
- Extra icon sizes added to the Windows `.ico` file

## 1.7.0

### Added

- Support for downloading media files: audio, video, non encrypted HLS streams from the browser (browser extension needs
  to be updated)
- Option to customize each item individually in the “Add Multiple Downloads” page (by right-clicking on each item) (
  #866)
- “Download Page” and “Custom User-Agent” options are now available in the “Add Download” > "Configs" dialog
- Ability to remove recently used save locations (#873)

### Changed

- Browser integration API updated; updating the browser extension is required to support new features

### Improved

- Updated translations
- The download creation time is now set to when the “Add Download” dialog is opened (#846)

## 1.6.14

### Fixed

- An issue causing slow download speeds on some websites

### Improved

- Updated translations
- Download Engine improvements
- Minor UI improvements

## 1.6.13

### Fixed

- **Access Denied** error could sometimes happen when adding a list of downloads (#826)

### Improved

- Updated translations
- Download engine improvements (#828)
- **Customize Table Columns** popup now supports drag to reorder (#830)

## 1.6.12

### Added

- **Per Host Settings** — save username, password, thread count, user-agent, and more for specific hosts (#820)
- Support for using **Move** action by holding **Shift** during drag & drop (#821)

### Changed

- UI scale is now relative to the system scale instead of using a fixed value (#814)

### Fixed

- Encoding issue with the default download folder on Linux (#810)

### Improved

- Updated translations
- Enhanced multi-display support (#814)
- Main window now remembers its maximized state (#815)

## 1.6.11

### Added

- Option to change the download size unit (#804)

### Fixed

- "Permission denied" error when starting a new download (#795)

### Improved

- Updated translations
- Improved settings page (#805)
- Automatically fix illegal characters in server-provided filenames (#781)
- Better handling of filenames received from the server (#780)
- Use the OS default download location on first launch (#789)

## 1.6.10

### Added

- New Black theme (#767)

### Fixed

- Restored missing executable permissions for files inside archives (macOS & Linux) (#765)
- Eliminated flickering on the "New Update" page (#770)

### Improved

- Updated translations
- Hovering between menus now works without closing the open one (#766)
- Better item selection and new keyboard shortcuts on the queue items page (#769)
- Small UI improvements

## 1.6.9

### Fixed

- "Keep System Awake" was not properly cancelled on Windows (#755)
- "Create Desktop Entry" had issue if the path contains spaces on Linux (#733)
- Application crash on systems that have invalid font names (#737)
- Some settings statuses were not updating correctly (#732)
- "Download Dialog" position shifted when multiple dialogs were open simultaneously (#758)
- "Add Download Dialog" position shifted when multiple dialogs were open simultaneously (#761)

### Improved

- Translations updated
- Better handling of filenames received from the server (#759)

## 1.6.8

### Fixed

- Can't change Auto Shutdown option if it was enabled

## 1.6.7

### Added

- Lithuanian language support
- Kurdish (Sorani) language support
- Option to automatically shut down the system when downloads or queues complete (#726)
- Option to delete partial files on download cancellation (#724)

### Changed

- App icon is now hidden from the Dock at runtime on macOS when the system tray is used (#710)
- Default max download retry count changed to 3

### Fixed

- Removable storages are no longer monitored on Windows (#705)

### Improved

- Translations updated
- System stays awake while downloads are in progress (#725)
- Confirm dialogs and buttons now have better focus behavior
- Dropdowns are now searchable (#706)
- IO Operations improvements

## 1.6.6

### Added

- Option to create desktop entry on Linux (#698)

### Changed

- Renamed Linux desktop and autostart files for better compatibility (#699)

### Fixed

- Removed duplicate UI Scale option in the Settings (#697)

### Improved

- Updated translations
- Pressing "Stop All" now closes all active download windows (#700)

## 1.6.5

### Added

- New themes (Deep Ocean, new Dark, new Light)
- Category accepted file types are now optional (#690)
- Option to change the app font (#692)
- Option to switch between relative and absolute date/time formats (#694)
- Option to clear all items in the queue at once

### Changed

- Renamed the previous Dark theme to **Obsidian**
- Renamed the previous Light theme to **Light Gray**

### Fixed

- Issue where the app wouldn’t start for some Windows users (#695)

### Improved

- Updated translations
- General UI Improvements
- Automatically scroll to new downloads on the main page (#672)
- Improved path validation for new downloads (#693)

## 1.6.4

### Added

- Queues are now visible on the home page, next to the categories (#661)
- In-app update is now supported on macOS (#627)
- New option to enable the native menu bar on macOS (#646)

### Fixed

- macOS: Window now activates properly when "Show Downloads" is clicked from the system tray (#632)
- Linux: Startup desktop entry now includes an icon (#634)
- An issue where the "Edit Download" page could unintentionally change the download status (#641)
- Queue status not updated properly sometimes (#663)

### Improved

- Translations updated
- Minor UI improvements

## 1.6.3

### Added

- Korean Language
- An option to append ".part" extension to incomplete downloads (disabled by default)

### Fixed

- Prevent freeze when opening a file or folder
- Some websites close the connection if we ask for resume support
- Some non-standard links not captured correctly
- Crash when opening browser integration links on macOS
- Multiselect with Meta key not working as expected on macOS
- Multiselect not stopped properly after window focus lost

### Improved

- Translations updated
- Minor UI/UX improvements

## 1.6.2

### Added

- Thai Language

### Fixed

- System Tray crashes sometimes in Linux
- Icons not rendered properly sometimes in Linux
- System Tray icon color in macOS
- Quit handler in macOS

### Improved

- Translations updated
- Respect user defined position of system buttons in Linux

## 1.6.1

### Fixed

- Application shortcut in Windows have no icon

### Improved

- Translations updated

## 1.6.0

### Added

- macOS support
- Polish Language
- Hungarian Language
- Luri Bakhtiari Language
- Silent Download option in the Browser Integration
- Donate button in the app to support the project

### Fixed

- Overriding an existing download sometimes didn't work as expected.
- "Start Queue" checkbox sometimes did not work as expected.

### Improved

- Translations updated
- Custom Window decorations
- Window dragging on Linux is now handled by the OS
- Each platform now uses its own system button style
- JVM updated to version 21

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
