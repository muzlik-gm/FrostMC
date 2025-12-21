@echo off
echo ========================================
echo FrostSMP JAR Verification Script
echo ========================================
echo.

set SOURCE_JAR=target\FrostSMP.jar
set SERVER_PATH=C:\Users\Alli computer\OneDrive\Desktop\Server
set SERVER_JAR=%SERVER_PATH%\plugins\FrostSMP.jar

echo Checking SOURCE JAR...
if exist "%SOURCE_JAR%" (
    echo ✅ Found: %SOURCE_JAR%
    for %%A in ("%SOURCE_JAR%") do (
        echo    Size: %%~zA bytes
        echo    Date: %%~tA
    )
) else (
    echo ❌ NOT FOUND: %SOURCE_JAR%
)

echo.
echo Checking SERVER JAR...
if exist "%SERVER_JAR%" (
    echo ✅ Found: %SERVER_JAR%
    for %%A in ("%SERVER_JAR%") do (
        echo    Size: %%~zA bytes
        echo    Date: %%~tA
    )
) else (
    echo ❌ NOT FOUND: %SERVER_JAR%
)

echo.
echo ========================================
echo COMPARISON:
echo ========================================
if exist "%SOURCE_JAR%" if exist "%SERVER_JAR%" (
    fc /b "%SOURCE_JAR%" "%SERVER_JAR%" >nul
    if errorlevel 1 (
        echo ❌ FILES ARE DIFFERENT!
        echo    The server is running an OLD version!
        echo    You MUST copy the new JAR to the server.
    ) else (
        echo ✅ FILES ARE IDENTICAL!
        echo    Server has the latest version.
    )
) else (
    echo ⚠️  Cannot compare - one or both files missing
)

echo.
echo ========================================
echo INSTRUCTIONS:
echo ========================================
echo 1. STOP your Minecraft server
echo 2. Run: deploy_fix.bat
echo 3. START your server
echo 4. Test abilities again
echo ========================================
echo.
pause
