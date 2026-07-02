; RemoveFromPath.nsh
; Removes a directory from the user PATH (HKCU\Environment)
;
; Self-contained: uses whole-entry matching to find and remove
; the exact path entry (with semicolon boundaries, no substring match).
; After writing, broadcasts WM_SETTINGCHANGE.
;
; Usage (install section):
;   Push "C:\path\to\remove"
;   Call RemoveFromPath
;
; Usage (uninstall section):
;   Push "C:\path\to\remove"
;   Call un.RemoveFromPath

!ifndef RemoveFromPath_INCLUDED
!define RemoveFromPath_INCLUDED

!macro _RemovePath _func_name
Function ${_func_name}
    Exch $0
    Push $1
    Push $2
    Push $3
    Push $4
    Push $5
    Push $6
    Push $7

    ReadRegStr $1 HKCU "Environment" "Path"
    StrCmp $1 "" _rpf_done

    ; Wrapped PATH and target for boundary-safe search
    StrCpy $2 ";$1;"
    StrCpy $3 ";$0;"

    ; Search for wrapped target in wrapped PATH
    StrLen $4 $3
    StrCpy $6 $2
_rpf_search:
    StrCpy $7 $6 $4
    StrCmp $7 $3 _rpf_found
    StrCpy $6 $6 "" 1
    StrCmp $6 "" _rpf_done
    Goto _rpf_search

_rpf_found:
    ; $6 = remainder from match position, $4 = len of matched target
    ; Find prefix length
    StrLen $5 $2
    StrLen $7 $6
    IntOp $5 $5 - $7  ; offset of match

    ; Extract prefix (before match) and suffix (after match)
    StrCpy $7 $2 $5         ; prefix
    StrCpy $6 $6 "" $4      ; suffix (after removing target+wrapped semicolons)

    ; Reconstruct
    StrCpy $2 "$7$6"

    ; Clean up leading/trailing semicolons
    StrCpy $5 $2 1
    StrCmp $5 ";" "" _rpf_no_lead
    StrCpy $2 $2 "" 1
_rpf_no_lead:
    StrCpy $5 $2 1 -1
    StrCmp $5 ";" "" _rpf_no_trail
    StrLen $6 $2
    IntOp $6 $6 - 1
    StrCpy $2 $2 $6
_rpf_no_trail:
    WriteRegExpandStr HKCU "Environment" "Path" "$2"
    System::Call "user32::SendMessageTimeout(i 0xffff, i 0x001A, i 0, t 'Environment', i 0x0002, i 5000, *i r0)"

_rpf_done:
    Pop $7
    Pop $6
    Pop $5
    Pop $4
    Pop $3
    Pop $2
    Pop $1
    Pop $0
FunctionEnd
!macroend

!insertmacro _RemovePath "RemoveFromPath"
!insertmacro _RemovePath "un.RemoveFromPath"

!endif