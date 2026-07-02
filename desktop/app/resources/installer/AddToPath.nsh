; AddToPath.nsh
; Adds a directory to the user PATH (HKCU\Environment)
;
; Self-contained: no external function dependencies.
; Uses whole-entry matching to avoid false positives when
; one path is a prefix of another.
; After writing, broadcasts WM_SETTINGCHANGE.
;
; Usage:
;   Push "C:\path\to\add"
;   Call AddToPath

!ifndef AddToPath_INCLUDED
!define AddToPath_INCLUDED

; Checks if ";needle;" exists in ";haystack;"
; Sets $0 to remaining after match (found) or "" (not found).
; Expects $2 = haystack (already semicolon-wrapped), $3 = needle (wrapped).
!macro _AtpSearch _atp_label
    ; $2 = ";PATH;", $3 = ";target;"
    StrLen $4 $3
_atp_search_${_atp_label}:
    StrCpy $5 $2 $4
    StrCmp $5 $3 _atp_found_${_atp_label}
    StrCpy $2 $2 "" 1
    StrCmp $2 "" _atp_notFound_${_atp_label}
    Goto _atp_search_${_atp_label}
_atp_found_${_atp_label}:
    StrCpy $0 $2
    Goto _atp_done_${_atp_label}
_atp_notFound_${_atp_label}:
    StrCpy $0 ""
_atp_done_${_atp_label}:
!macroend

Function AddToPath
    Exch $0
    Push $1
    Push $2
    Push $3
    Push $4
    Push $5
    Push $6                  ; save original input path

    StrCpy $6 $0             ; preserve input path before search clobbers $0

    ReadRegStr $1 HKCU "Environment" "Path"

    ; Wrap in semicolons for whole-entry matching
    ; ";old_PATH;" — search for ";target;" within this
    StrCpy $2 ";$1;"
    StrCpy $3 ";$6;"

    ; Check if path already exists
    !insertmacro _AtpSearch "addtopath"
    StrCmp $0 "" _atp_do_add
    Goto _atp_finish

_atp_do_add:
    StrCmp $1 "" _atp_new _atp_append

_atp_new:
    WriteRegExpandStr HKCU "Environment" "Path" "$6"
    Goto _atp_broadcast

_atp_append:
    StrCpy $2 $1 1 -1
    StrCmp $2 ";" _atp_nosep
    StrCpy $1 "$1;"
_atp_nosep:
    WriteRegExpandStr HKCU "Environment" "Path" "$1$6"

_atp_broadcast:
    System::Call "user32::SendMessageTimeout(i 0xffff, i 0x001A, i 0, t 'Environment', i 0x0002, i 5000, *i r0)"

_atp_finish:
    Pop $6
    Pop $5
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Pop $0
FunctionEnd

!endif