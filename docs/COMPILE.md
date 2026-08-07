# Building FastContentChunk (native tokenizer)

## Prerequisites

- Visual Studio 2019/2022 or Build Tools (VC++ compilers)
- CMake (or use Visual Studio's packaged CMake)
- JDK 17+ (for JNI headers and testing)

## Build Steps (Windows)

```powershell
cd FastContentChunk\native\fastchunk
mkdir build; cd build
cmake ..
cmake --build . --config Release
```

Expected artifact: `build\Release\fastchunk.dll` or similar for your configuration.

## Java Packaging

After building the native library, build the Java artifacts so demos can find the native binary:

```powershell
cd <repo-root>\FastContentChunk
mvn clean package -DskipTests
```

## Running the demo

Ensure `fastchunk.dll` is on `java.library.path` and run demo as in `../README.md`.

## Troubleshooting

- If `cmake` is missing, use Visual Studio's CMake at:
  `C:\Program Files\Microsoft Visual Studio\<version>\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe`
- For `UnsatisfiedLinkError`: verify JNI symbol names and `.def` exports if present.
