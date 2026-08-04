@echo off
chcp 65001 >nul
:: FastJava Native DLL Compiler Script
:: Auto-detects Visual Studio and JAVA_HOME

echo ========================================
echo FastContentChunk Native Library Builder
echo ========================================

:: Configuration
set LIB_NAME=fastchunk

:: Try to find VS using vswhere.exe (most reliable)
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
if exist "%VSWHERE%" (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VS_PATH=%%i"
    )
)

:: Fallback: Check standard paths if vswhere didn't work
if not defined VS_PATH (
    if exist "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files\Microsoft Visual Studio\2022\Community"
    ) else if exist "C:\Program Files\Microsoft Visual Studio\2022\Enterprise\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files\Microsoft Visual Studio\2022\Enterprise"
    ) else if exist "C:\Program Files\Microsoft Visual Studio\2022\Professional\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files\Microsoft Visual Studio\2022\Professional"
    ) else if exist "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools"
    ) else if exist "C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvars64.bat" (
        set "VS_PATH=C:\Program Files (x86)\Microsoft Visual Studio\2019\Community"
    ) else if exist "C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvarsall.bat" (
        set "VS_PATH=C:\Program Files\Microsoft Visual Studio\18\Community"
    )
)

if not defined VS_PATH (
    echo ERROR: Visual Studio not found!
    echo Please install Visual Studio 2019, 2022, or 2026 with "Desktop development with C++"
    exit /b 1
)

echo Found Visual Studio at: %VS_PATH%

:: Try to detect JAVA_HOME if not set
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-25.0.3" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.3"
    ) else if exist "C:\Program Files\Java\jdk-25" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    ) else if exist "C:\Program Files\Java\jdk-21.0.11" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
    ) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17-hotspot"
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME not set!
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%

:: Setup environment
if exist "%VS_PATH%\VC\Auxiliary\Build\vcvars64.bat" (
    call "%VS_PATH%\VC\Auxiliary\Build\vcvars64.bat"
) else if exist "%VS_PATH%\VC\Auxiliary\Build\vcvarsall.bat" (
    call "%VS_PATH%\VC\Auxiliary\Build\vcvarsall.bat" x64
) else (
    echo ERROR: vcvars64.bat or vcvarsall.bat not found in VS installation
    exit /b 1
)

:: Verify cl.exe is available after environment setup
where cl.exe >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: cl.exe not found after VS environment setup
    echo This indicates the C++ build tools are not properly installed
    echo.
    echo Please run Visual Studio Installer and ensure:
    echo 1. "Desktop development with C++" workload is installed
    echo 2. "MSVC v143 - VS 2022 C++ x64/x86 build tools" is selected
    echo.
    echo Alternatively, install Visual Studio Build Tools from:
    echo https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022
    exit /b 1
)

:: Create build directory
if not exist build mkdir build

:: Compile C++ source
cl.exe /O2 /W3 /MD /EHsc /LD ^
   /I "%JAVA_HOME%\include" ^
   /I "%JAVA_HOME%\include\win32" ^
   /Fo:build\ ^
   /Fe:build\%LIB_NAME%.dll ^
   native\fastchunk\*.cpp ^
   /link /DLL /MACHINE:X64

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] DLL built at: build\%LIB_NAME%.dll
) else (
    echo.
    echo [FAILED] Compilation failed.
    exit /b 1
)

echo.
echo Done compile!
