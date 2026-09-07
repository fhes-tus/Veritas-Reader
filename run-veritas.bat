@echo off
setlocal
cd /d "%~dp0"

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
) else (
    set "JAVA_EXE=java"
)

echo Starting Veritas Reader Desktop...
"%JAVA_EXE%" -jar "veritas-pc\build\compose\jars\VeritasReader-windows-x64-2.0.0.jar"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Launching via Gradle fallback...
    call gradlew.bat :veritas-pc:run
)
endlocal
