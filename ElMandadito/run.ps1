$JAVA_HOME = "C:\Users\TI\AppData\Local\Programs\Android Studio\jbr"
$ADB       = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$EMULATOR  = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
$AVD       = "Medium_Phone_API_36.0"
$APK       = "app\build\outputs\apk\debug\app-debug.apk"
$PACKAGE   = "com.elmandadito.app"
$ACTIVITY  = ".ui.SplashActivity"

Set-Location $PSScriptRoot

Write-Host "[1/4] Verificando emulador..."
$devices = & $ADB devices | Select-String "emulator"
if ($devices -match "device") {
    Write-Host "    Emulador ya esta corriendo."
} else {
    Write-Host "    Iniciando emulador..."
    Start-Process $EMULATOR -ArgumentList "-avd", $AVD, "-no-snapshot-load"
    Write-Host "    Esperando que bootee (puede tardar ~60s)..."
    do {
        Start-Sleep 5
        $devices = & $ADB devices | Select-String "emulator"
    } while ($devices -notmatch "device")
    Write-Host "    Emulador listo."
}

Write-Host "[2/4] Compilando app..."
$env:JAVA_HOME = $JAVA_HOME
& .\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo la compilacion."
    exit 1
}

Write-Host "[3/4] Instalando APK..."
& $ADB install -r $APK

Write-Host "[4/4] Lanzando app..."
& $ADB shell am start -n "$PACKAGE/$ACTIVITY"

Write-Host ""
Write-Host "Listo. La app esta corriendo en el emulador."
