# Настройка входа через Яндекс ID для Teleport
param(
    [string]$ClientId = ""
)

$ErrorActionPreference = "Stop"
$TeleportRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$AndroidDir = Join-Path $TeleportRoot "android"
$LocalProps = Join-Path $AndroidDir "local.properties"
$IdFile = Join-Path $AndroidDir "yandex.client.id"

$Keytool = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
if (-not (Test-Path $Keytool)) {
    $Keytool = "keytool"
}

$Keystore = Join-Path $env:USERPROFILE ".android\debug.keystore"
$Package = "com.teleport.messenger"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Teleport — настройка Яндекс ID" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Пакет Android: $Package"
Write-Host ""

$sha256 = ""
if (Test-Path $Keystore) {
    $out = & $Keytool -list -v -alias androiddebugkey -keystore $Keystore -storepass android -keypass android 2>&1
    $line = $out | Select-String -Pattern "SHA256:\s*(.+)" | Select-Object -First 1
    if ($line) {
        $sha256 = ($line.Matches[0].Groups[1].Value -replace ":", "").ToUpper()
        Write-Host "SHA256 (debug, для oauth.yandex.ru):" -ForegroundColor Green
        Write-Host $sha256
        Write-Host ""
    }
} else {
    Write-Host "Debug keystore не найден: $Keystore" -ForegroundColor Yellow
    Write-Host "Соберите APK хотя бы раз в Android Studio, затем запустите скрипт снова."
    Write-Host ""
}

if (-not $ClientId) {
    Write-Host "1. Откройте https://oauth.yandex.ru/client/new/id/" -ForegroundColor Yellow
    Write-Host "2. Тип: «Для авторизации пользователей»"
    Write-Host "3. Платформа: Android-приложение"
    Write-Host "   - Имя пакета: $Package"
    if ($sha256) { Write-Host "   - SHA256: $sha256" }
    Write-Host "4. Права: login:info, login:email"
    Write-Host "5. Скопируйте Client ID"
    Write-Host ""
    Start-Process "https://oauth.yandex.ru/client/new/id/"
    $ClientId = Read-Host "Вставьте Client ID"
}

$ClientId = $ClientId.Trim()
if (-not $ClientId) {
    Write-Host "Client ID не указан." -ForegroundColor Red
    exit 1
}

Set-Content -Path $IdFile -Value $ClientId -Encoding UTF8 -NoNewline

$lines = @()
if (Test-Path $LocalProps) {
    $lines = Get-Content $LocalProps | Where-Object { $_ -notmatch '^\s*yandex\.client\.id\s*=' }
}
$lines += "yandex.client.id=$ClientId"
Set-Content -Path $LocalProps -Value $lines -Encoding UTF8

Write-Host ""
Write-Host "Сохранено:" -ForegroundColor Green
Write-Host "  $IdFile"
Write-Host "  $LocalProps (yandex.client.id)"
Write-Host ""

$build = Read-Host "Пересобрать APK сейчас? [Y/n]"
if ($build -eq "" -or $build -eq "Y" -or $build -eq "y") {
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    Push-Location $AndroidDir
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Copy-Item "app\build\outputs\apk\debug\app-debug.apk" (Join-Path $TeleportRoot "Teleport.apk") -Force
        Write-Host ""
        Write-Host "APK готов: $TeleportRoot\Teleport.apk" -ForegroundColor Green
    } else {
        Write-Host "Ошибка сборки." -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
}

Write-Host ""
Write-Host "Готово. В приложении нажмите «Войти через Яндекс ID»." -ForegroundColor Green
