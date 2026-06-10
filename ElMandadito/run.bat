@echo off
setlocal

set JAVA_HOME=C:\Users\TI\AppData\Local\Programs\Android Studio\jbr
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
set EMULATOR=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe
set GRADLE=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.9-bin\90cnw93cvbtalezasaz0blq0a\gradle-8.9\bin\gradle.bat
set AVD=Medium_Phone_API_36.0
set APK=app\build\outputs\apk\debug\app-debug.apk
set PACKAGE=com.elmandadito.app
set ACTIVITY=.ui.SplashActivity

echo [1/4] Verificando emulador...
for /f "tokens=2" %%i in ('"%ADB%" devices ^| findstr /i "emulator"') do set DEVICE_STATE=%%i

if "%DEVICE_STATE%"=="device" (
    echo     Emulador ya esta corriendo.
) else (
    echo     Iniciando emulador...
    start "" "%EMULATOR%" -avd %AVD% -no-snapshot-load
    echo     Esperando que bootee (puede tardar ~60s)...
    :WAIT
    for /f "tokens=2" %%i in ('"%ADB%" devices ^| findstr /i "emulator"') do set DEVICE_STATE=%%i
    if not "%DEVICE_STATE%"=="device" (
        timeout /t 5 /nobreak >nul
        goto WAIT
    )
    echo     Emulador listo.
)

echo [2/4] Compilando app...
call "%GRADLE%" assembleDebug --project-dir "%~dp0"
if %errorlevel% neq 0 (
    echo ERROR: Fallo la compilacion.
    pause
    exit /b 1
)

echo [3/4] Instalando APK...
"%ADB%" install -r "%~dp0%APK%"

echo [4/4] Lanzando app...
"%ADB%" shell am start -n "%PACKAGE%/%ACTIVITY%"

echo.
echo Listo. La app esta corriendo en el emulador.
