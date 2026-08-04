@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ========================================
echo FastContentChunk Native Library Builder
echo ========================================
echo.

:: 1. Setup Java Environment
if not defined JAVA_HOME (
    echo JAVA_HOME not defined. Searching for JDK...
    for /d %%i in ("C:\Program Files\Java\jdk-*") do (
        set "JAVA_HOME=%%i"
    )
)

if not defined JAVA_HOME (
    echo ERROR: Could not find a JDK in C:\Program Files\Java.
    echo Please set JAVA_HOME manually.
    pause
    exit /b 1
)

echo Using JDK: %JAVA_HOME%

:: 2. Setup VS Environment
set "VS_ROOT=C:\Program Files\Microsoft Visual Studio"
set "VCVARS="

if not exist "%VS_ROOT%" (
    echo ERROR: Visual Studio root not found: %VS_ROOT%
    pause
    exit /b 1
)

for /f "delims=" %%i in ('dir /b /ad "%VS_ROOT%" 2^>nul') do (
    set "VS_VER=%%i"
    for /f "delims=" %%j in ('dir /b /ad "%VS_ROOT%\!VS_VER!" 2^>nul') do (
        set "VS_EDITION=%%j"
        if exist "%VS_ROOT%\!VS_VER!\!VS_EDITION!\VC\Auxiliary\Build\vcvarsall.bat" (
            set "VCVARS=%VS_ROOT%\!VS_VER!\!VS_EDITION!\VC\Auxiliary\Build\vcvarsall.bat"
            goto :foundVS
        )
    )
)

:foundVS
if not defined VCVARS (
    echo ERROR: Visual Studio not found under %VS_ROOT% or missing vcvarsall.bat
    echo Please install Visual Studio with "Desktop development with C++"
    pause
    exit /b 1
)

echo Found Visual Studio environment: %VCVARS%
call "%VCVARS%" x64
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

:: 3. Build Native Library
echo.
echo Building native library...
set "LIB_NAME=fastchunk"

if not exist build mkdir build

:: Check if cl.exe is available after VS setup
where cl.exe >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo Using cl.exe compiler...
    cl.exe /O2 /W3 /MD /EHsc /LD ^
        /I "%JAVA_HOME%\include" ^
        /I "%JAVA_HOME%\include\win32" ^
        /Fe:build\%LIB_NAME%.dll ^
        native\fastchunk\*.cpp ^
        /link /DLL /MACHINE:X64

    if %ERRORLEVEL% == 0 (
        echo.
        echo [SUCCESS] DLL built at: build\%LIB_NAME%.dll
        goto :build_success
    )
)

:: Fallback: Try to find cl.exe in known locations
echo cl.exe not in PATH, searching in MSVC directories...
set "MSVC_ROOT=C:\Program Files\Microsoft Visual Studio\18\Community\VC\Tools\MSVC"
for /f "delims=" %%i in ('dir /b /ad "%MSVC_ROOT%" 2^>nul') do (
    set "MSVC_VER=%%i"
    for /f "delims=" %%j in ('dir /b /ad "%MSVC_ROOT%\!MSVC_VER!\bin" 2^>nul') do (
        set "HOST_ARCH=%%j"
        for /f "delims=" %%k in ('dir /b /ad "%MSVC_ROOT%\!MSVC_VER!\bin\!HOST_ARCH!" 2^>nul') do (
            set "TARGET_ARCH=%%k"
            if exist "%MSVC_ROOT%\!MSVC_VER!\bin\!HOST_ARCH!\!TARGET_ARCH!\cl.exe" (
                set "CL_PATH=%MSVC_ROOT%\!MSVC_VER!\bin\!HOST_ARCH!\!TARGET_ARCH!"
                echo Found cl.exe at: !CL_PATH!
                set "PATH=!CL_PATH!;%PATH%"
                cl.exe /O2 /W3 /MD /EHsc /LD ^
                    /I "%JAVA_HOME%\include" ^
                    /I "%JAVA_HOME%\include\win32" ^
                    /Fe:build\%LIB_NAME%.dll ^
                    native\fastchunk\*.cpp ^
                    /link /DLL /MACHINE:X64

                if %ERRORLEVEL% == 0 (
                    echo.
                    echo [SUCCESS] DLL built at: build\%LIB_NAME%.dll
                    goto :build_success
                )
            )
        )
    )
)

echo ERROR: Unable to find or use cl.exe compiler
echo Visual Studio environment was set up but compiler not available
pause
exit /b 1

:build_success
if exist build\%LIB_NAME%.dll (
    echo DLL verification: build\%LIB_NAME%.dll exists
) else (
    echo WARNING: DLL not found in expected location
    dir /s /b build\*.dll
)

echo.
echo ===========================================
echo BUILD SUCCESSFUL!
echo ===========================================
pause
