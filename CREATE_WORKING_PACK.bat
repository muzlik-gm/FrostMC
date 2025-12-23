@echo off
echo ========================================
echo Creating Working Resource Pack
echo ========================================
echo.

cd resourcepack

echo Deleting old ZIP if exists...
if exist "..\FrostMC_Working.zip" del "..\FrostMC_Working.zip"

echo.
echo Creating new ZIP from resourcepack folder...
powershell -Command "Compress-Archive -Path 'assets','pack.mcmeta','pack.png' -DestinationPath '..\FrostMC_Working.zip' -Force"

echo.
echo ========================================
echo Done! FrostMC_Working.zip created
echo ========================================
echo.
echo Copy FrostMC_Working.zip to your .minecraft/resourcepacks/ folder
echo Then enable it in Minecraft and press F3+T to reload
echo.
pause
