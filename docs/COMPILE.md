# Compiling FastContentChunk Native Library

## Prerequisites
- Windows 10/11 x64
- Visual Studio 2022 (MSVC C++ Build Tools)
- Java 17+ JDK

## Build Native Library

Run `compile.bat` from the root directory:

```cmd
compile.bat
```

This invokes MSVC `cl.exe` to build `build/fastchunk.dll`.

## Build Java Package

```cmd
mvn clean package -DskipTests
```
