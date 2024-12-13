@echo off

set APP_NAME=ABDownloadManager
call :main "%1" "%2"
goto :eof

:stopApp
echo execute: taskkill /IM %APP_NAME%.exe /F
taskkill /IM %APP_NAME%.exe /F
call :wait_for_termination
echo %APP_NAME% is terminated
goto :eof

:wait_for_termination
echo checking for termination of %APP_NAME%
tasklist /FI "IMAGENAME eq %APP_NAME%.exe" | find /I "%APP_NAME%.exe" >nul 2>&1
if errorlevel 1 (
    goto :eof
) else (
    ping 127.0.0.1 -n 2 >nul 2>&1
    goto wait_for_termination
)


:removeCurrentInstallation
setlocal
    set installationFolder=%~1
    set filesToRemove=("app" "runtime" "ABDownloadManager.exe" "ABDownloadManager.ico")
    for %%f in %filesToRemove% do (
        if exist %installationFolder%\%%f (
            if exist %installationFolder%\%%f\* (
                echo executing rmdir /S /Q %installationFolder%\%%f
                rmdir /S /Q "%installationFolder%\%%f"
            ) else (
                echo executing del /F /Q %installationFolder%\%%f
                del /F /Q "%installationFolder%\%%f"
            )
        )
    )
    endlocal
goto :eof

:copyUpdateToInstallationFolder
    setlocal
    set updateFile=%1
    set installationFolder=%2
    echo executing: xcopy /E /I /Y %updateFile% %installationFolder%
    xcopy /E /I /Y %updateFile% %installationFolder%
    endlocal
goto :eof

:removeUpdateFolder
    setlocal
    set updateFolder=%1
    echo executing rmdir /S /Q "%updateFolder%"
    rmdir /S /Q "%updateFolder%"
    endlocal
goto :eof

:executeProgram
    setlocal
    set installationFolder=%~1
    set code=%2
    set message=%3
    echo executing %installationFolder%\%APP_NAME%.exe
    start "" %installationFolder%\%APP_NAME%.exe
    endlocal
goto :eof

:main
    setlocal
    set updateFile=%1
    set installationFolder=%2
    call :stopApp
    call :removeCurrentInstallation %installationFolder%
    call :copyUpdateToInstallationFolder %updateFile% %installationFolder%
    call :removeUpdateFolder %updateFile%
    call :executeProgram %installationFolder%
    endlocal
goto :eof


