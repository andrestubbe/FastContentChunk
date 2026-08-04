@echo off
chcp 65001 >nul
cls

echo ⚡ Building FastContentChunk...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo �  Preparing runtime classpath...
call mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt -DincludeScope=runtime
if %ERRORLEVEL% NEQ 0 ( echo ❌ Classpath build failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running FastContentChunk demo...
set LIBPATH=%~dp0build
set /p CP=<cp.txt
java -Djava.library.path="%LIBPATH%" -cp "target\classes;%CP%" fastcontentchunk.DemoFastContentChunk

pause
