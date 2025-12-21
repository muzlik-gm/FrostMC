@echo off
echo ========================================
echo FrostSMP Plugin Deployment Script
echo ========================================
echo.
echo ⚠️  WARNING: Make sure your server is STOPPED!
echo    Copying while server is running may fail.
echo.
pause

REM Check if JAR exists
if not exist "target\FrostSMP.jar" (
    echo ERROR: target\FrostSMP.jar not found!
    echo Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo Found: target\FrostSMP.jar
echo.

SERVER_PATH=C:\Users\Alli computer\OneDrive\Desktop\Server


if not exist "%SERVER_PATH%" (
    echo ERROR: Server path does not exist!
    pause
    exit /b 1
)

if not exist "%SERVER_PATH%\plugins" (
    echo ERROR: plugins folder not found in server path!
    pause
    exit /b 1
)

echo.
echo Server path: %SERVER_PATH%
echo Target: %SERVER_PATH%\plugins\FrostSMP.jar
echo.

REM Backup old JAR if it exists
if exist "%SERVER_PATH%\plugins\FrostSMP.jar" (
    echo Backing up old JAR...
    copy "%SERVER_PATH%\plugins\FrostSMP.jar" "%SERVER_PATH%\plugins\FrostSMP.jar.backup" >nul
    echo Backup created: FrostSMP.jar.backup
)

REM Copy new JAR
echo.
echo Copying new JAR...
copy "target\FrostSMP.jar" "%SERVER_PATH%\plugins\FrostSMP.jar" >nul

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Plugin deployed!
    echo ========================================
    echo.
    echo Next steps:
    echo 1. Restart your Minecraft server
    echo 2. Check for errors in console
    echo 3. Test Storm Fragment abilities
    echo 4. Test rituals for magic circles
    echo.
    echo The particle errors should be GONE!
    echo ========================================
) else (
    echo.
    echo ERROR: Failed to copy JAR file!
    echo Make sure the server is stopped.
)

echo.
pause
