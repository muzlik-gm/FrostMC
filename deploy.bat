@echo off
echo ========================================
echo FrostSMP Plugin Deployment
echo ========================================
echo.

set SERVER_PATH=C:\Users\Alli computer\OneDrive\Desktop\Server
set PLUGIN_JAR=%SERVER_PATH%\plugins\FrostSMP.jar
set SOURCE_JAR=target\FrostSMP.jar

echo Step 1: Checking if server is running...
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo WARNING: Java is running! Please stop the server first.
    echo.
    echo Press any key to continue anyway, or Ctrl+C to cancel...
    pause
)

echo.
echo Step 2: Backing up old plugin...
if exist "%PLUGIN_JAR%" (
    copy "%PLUGIN_JAR%" "%PLUGIN_JAR%.backup" >NUL 2>&1
    echo   Backup created: FrostSMP.jar.backup
) else (
    echo   No existing plugin found (first install)
)

echo.
echo Step 3: Copying new plugin JAR...
copy /Y "%SOURCE_JAR%" "%PLUGIN_JAR%"
if %ERRORLEVEL% EQU 0 (
    echo   SUCCESS: Plugin JAR updated!
) else (
    echo   ERROR: Failed to copy JAR file
    echo   Make sure the server is stopped and the path is correct
    pause
    exit /b 1
)

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo If you see these messages, the fix is working!
echo.
pause
