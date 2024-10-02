Unicode True
RequestExecutionLevel user
SetCompressor /SOLID lzma
!include "LogicLib.nsh"
!include "MUI2.nsh"


!define APP_PUBLISHER "{{ app_publisher }}"
!define APP_NAME "{{ app_name }}"
!define APP_DISPLAY_NAME "{{ app_display_name }}"
!define APP_VERSION "{{ app_version }}"
!define APP_VERSION_WITH_BUILD "{{ app_version_with_build }}"
!define APP_DISPLAY_VERSION "{{ app_display_version }}"
!define SOURCE_CODE_URL "{{ source_code_url }}"
!define PROJECT_WEBSITE "{{ project_website }}"
!define COPYRIGHT "{{ copyright }}"

!define INPUT_DIR "{{ input_dir }}"
!define LICENSE_FILE "{{ license_file }}"
!define MAIN_BINARY_NAME "${APP_NAME}"

!define SIDEBAR_IMAGE "{{ sidebar_image_file }}"
!define HEADER_IMAGE "{{ header_image_file }}"
!define ICON_FILE "{{ icon_file }}"

!define REG_UNINSTALL_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
!define REG_RUN_KEY "Software\Microsoft\Windows\CurrentVersion\Run\${APP_NAME}"
!define REG_APP_KEY "Software\${APP_NAME}"

; icon for this installer!

Icon "${ICON_FILE}"
!define MUI_ICON "${ICON_FILE}"
!define MUI_UNICON "${ICON_FILE}"

!if "${SIDEBAR_IMAGE}" != ""
  !define MUI_WELCOMEFINISHPAGE_BITMAP "${SIDEBAR_IMAGE}"

  !define MUI_UNWELCOMEFINISHPAGE_BITMAP "${SIDEBAR_IMAGE}"
!endif

!if "${HEADER_IMAGE}" != ""
  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_BITMAP  "${HEADER_IMAGE}"

  !define MUI_UNHEADERIMAGE
  !define MUI_UNHEADERIMAGE_BITMAP "${HEADER_IMAGE}"
!endif

VIProductVersion "${APP_VERSION_WITH_BUILD}"
VIAddVersionKey "ProductName" "${APP_DISPLAY_NAME}"
VIAddVersionKey "FileDescription" "${APP_DISPLAY_NAME}"
VIAddVersionKey "LegalCopyright" "${COPYRIGHT}"
VIAddVersionKey "FileVersion" "${APP_VERSION_WITH_BUILD}"
VIAddVersionKey "ProductVersion" "${APP_VERSION_WITH_BUILD}"

Name "${APP_DISPLAY_NAME}"
OutFile "{{ output_file }}"

InstallDir "$LOCALAPPDATA\${APP_NAME}"



!define INSTALL_DIR `$INSTDIR`
Function .onInit

    ; Call RestorePreviousInstallLocation

FunctionEnd

; configure instfiles page
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_INSTFILESPAGE_NOAUTOCLOSE

; configure finish page
!define MUI_FINISHPAGE_LINK "Open project in GitHub"
!define MUI_FINISHPAGE_LINK_LOCATION "${SOURCE_CODE_URL}"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION RunMainBinary

;Installation Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "${LICENSE_FILE}"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

;Uninstallation Pages
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; set language
!insertmacro MUI_LANGUAGE "English"

; a macro clear files to cleanup installation folder
!macro clearFiles
    RmDir /r "${INSTALL_DIR}\app"
    RmDir /r "${INSTALL_DIR}\runtime"
    Delete "${INSTALL_DIR}\${MAIN_BINARY_NAME}.exe"
    Delete "${INSTALL_DIR}\${MAIN_BINARY_NAME}.ico"
    Delete "${INSTALL_DIR}\uninstall.exe"
    RmDir "${INSTALL_DIR}"
!macroend

Function RunMainBinary
   Exec "${INSTALL_DIR}\${MAIN_BINARY_NAME}.exe"
FunctionEnd

!macro GetBestExecutableName result
    StrCpy ${result} "${MAIN_BINARY_NAME}.exe"
!macroend

; Function RestorePreviousInstallLocation
;     ReadRegStr $4 SHCTX "${REG_APP_KEY}" "InstallPath"
;     ${if} $4 != ""
;         StrCpy $INSTDIR $4
;     ${endif}
; FunctionEnd

; I should improve this.
!macro closeApp
    !insertmacro GetBestExecutableName $1
    DetailPrint "Stopping Executable $1"
    ; I don't wanna kill myself!
    ${If} "$EXEFILE" != "$1"
        ExecWait 'taskkill /F /IM "$1"' $0
    ${Else}
        DetailPrint "It seems that installer file name is same as app executable name"
        DetailPrint "Please close app manually"
        ; don't sleep the script for nothing.
        StrCpy $0 "1"
    ${EndIf}
    ${If} $0 == "0"
        Sleep 500
        BringToFront ; when we sleep it seems that window goes down
        DetailPrint "Current app stopped successfully"
    ${Endif}
!macroend

!macro CreateStartMenu
	createDirectory "$SMPROGRAMS\${APP_DISPLAY_NAME}"
	createShortCut "$SMPROGRAMS\${APP_DISPLAY_NAME}\${APP_DISPLAY_NAME}.lnk" "${INSTALL_DIR}\${MAIN_BINARY_NAME}.exe" "" "${INSTALL_DIR}\${MAIN_BINARY_NAME}.ico"
!macroend

!macro RemoveStartMenu
	RmDir /r "$SMPROGRAMS\${APP_DISPLAY_NAME}"
!macroend

!macro CreateDesktopShortcut
    CreateShortcut "$DESKTOP\${APP_DISPLAY_NAME}.lnk" "${INSTALL_DIR}\${MAIN_BINARY_NAME}.exe" "" "${INSTALL_DIR}\${MAIN_BINARY_NAME}.ico"
!macroend

!macro RemoveDesktopShortCut
	Delete "$DESKTOP\${APP_DISPLAY_NAME}.lnk"
!macroend



Section "${APP_DISPLAY_NAME}"
    SectionInstType RO

    DetailPrint "Closing app (if any)"
    !insertmacro closeApp
    DetailPrint "clearing old app (if any)"
    !insertmacro clearFiles
    DetailPrint "writing new data"
    SetOutPath "${INSTALL_DIR}"
    CreateDirectory "${INSTALL_DIR}"

    WriteUninstaller "${INSTALL_DIR}\uninstall.exe"

    File /nonfatal /r "${INPUT_DIR}\"


    ; Registry information for add/remove programs
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "DisplayName" "${APP_DISPLAY_NAME}"
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "DisplayIcon" "$\"${INSTALL_DIR}\${MAIN_BINARY_NAME}.exe$\""
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "DisplayVersion" "${APP_VERSION}"
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "Publisher" "${APP_PUBLISHER}"
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "InstallLocation" "$\"${INSTALL_DIR}$\""
    WriteRegStr SHCTX "${REG_UNINSTALL_KEY}" "UninstallString" "$\"${INSTALL_DIR}\uninstall.exe$\""
    WriteRegDWORD SHCTX "${REG_UNINSTALL_KEY}" "NoModify" "1"
    WriteRegDWORD SHCTX "${REG_UNINSTALL_KEY}" "NoRepair" "1"

    ; Registry keys for app installation path and version
    WriteRegStr SHCTX "${REG_APP_KEY}" "InstallPath" "${INSTALL_DIR}"
    WriteRegStr SHCTX "${REG_APP_KEY}" "Version" "${APP_VERSION}"
SectionEnd

Section "Start Menu"
    !insertmacro CreateStartMenu
SectionEnd

Section "Desktop Shortcut"
    !insertmacro CreateDesktopShortcut
SectionEnd

Section "Uninstall"
    !insertmacro closeApp
    !insertmacro clearFiles

    !insertmacro RemoveStartMenu
    !insertmacro RemoveDesktopShortCut

    DeleteRegKey SHCTX "${REG_UNINSTALL_KEY}"
    DeleteRegKey SHCTX "${REG_APP_KEY}"

    ; remove auto start on boot registry
    DeleteRegValue SHCTX "${REG_RUN_KEY}" "${APP_NAME}"
SectionEnd
