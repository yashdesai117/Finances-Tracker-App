# PaisaTrack 1-Click Automated Build Script
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "         PaisaTrack: Android APK Auto-Builder             " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Check if Java is installed
$javaInstalled = $false
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0 -or $javaVersion -like "*version*") {
        $javaInstalled = $true
        Write-Host "[✓] Java is installed: $javaVersion" -ForegroundColor Green
    }
} catch {
    $javaInstalled = $false
}

if (-not $javaInstalled) {
    Write-Host "[!] OpenJDK 17 is required to build the Android APK." -ForegroundColor Yellow
    Write-Host "[*] Installing Microsoft OpenJDK 17 automatically via winget..." -ForegroundColor Cyan
    winget install Microsoft.OpenJDK.17 --accept-package-agreements --accept-source-agreements
    
    # Reload Path
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

# 2. Run Gradle Build
Write-Host "`n[*] Building PaisaTrack Android APK..." -ForegroundColor Cyan
if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleDebug
} else {
    Write-Host "[!] gradlew.bat not found in current directory." -ForegroundColor Red
    exit 1
}

# 3. Check for generated APK
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    New-Item -ItemType Directory -Force -Path "output" | Out-Null
    Copy-Item $apkPath -Destination "output\PaisaTrack.apk" -Force
    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host "  SUCCESS! APK generated successfully!                  " -ForegroundColor Green
    Write-Host "  Location: output\PaisaTrack.apk                       " -ForegroundColor White
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "`nHow to install on your Android phone:" -ForegroundColor Cyan
    Write-Host "1. Send 'output\PaisaTrack.apk' to your phone via USB, WhatsApp, Telegram, or Google Drive." -ForegroundColor White
    Write-Host "2. Tap on the APK on your phone and tap 'Install' (enable 'Allow unknown sources' if prompted)." -ForegroundColor White
    Write-Host "3. Open PaisaTrack, tap 'SMS Sync' to grant permission, then add the Widget to your Home Screen!" -ForegroundColor White
} else {
    Write-Host "[!] Build did not produce $apkPath. Please check Gradle output above." -ForegroundColor Yellow
}
