param(
    [string]$BotUsername = "",
    [string]$BotToken = "",
    [string]$Origin = ""
)

$ErrorActionPreference = "Stop"
$TeleportRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$AndroidDir = Join-Path $TeleportRoot "android"
$LocalProps = Join-Path $AndroidDir "local.properties"
$TokenFile = Join-Path $AndroidDir "telegram.bot.token"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Teleport — настройка Telegram Login" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. @BotFather → /newbot → скопируйте токен"
Write-Host "2. @BotFather → /setdomain → ваш HTTPS-домен"
Write-Host ""

if (-not $BotUsername) {
    $BotUsername = Read-Host "Username бота (без @)"
}
if (-not $BotToken) {
    $BotToken = Read-Host "Токен бота"
}
if (-not $Origin) {
    $Origin = Read-Host "Домен origin [https://teleport.app]"
    if (-not $Origin) { $Origin = "https://teleport.app" }
}

$BotUsername = $BotUsername.Trim().TrimStart("@")
$BotToken = $BotToken.Trim()
$Origin = $Origin.Trim()

if (-not $BotUsername -or -not $BotToken) {
    Write-Host "Username и токен обязательны." -ForegroundColor Red
    exit 1
}

Set-Content -Path $TokenFile -Value $BotToken -Encoding UTF8 -NoNewline

$lines = @()
if (Test-Path $LocalProps) {
    $lines = Get-Content $LocalProps | Where-Object {
        $_ -notmatch '^\s*telegram\.(bot\.(username|token)|auth\.origin)\s*='
    }
}
$lines += "telegram.bot.username=$BotUsername"
$lines += "telegram.bot.token=$BotToken"
$lines += "telegram.auth.origin=$Origin"
Set-Content -Path $LocalProps -Value $lines -Encoding UTF8

Write-Host ""
Write-Host "Сохранено в local.properties и telegram.bot.token" -ForegroundColor Green
Write-Host "Домен в BotFather: $Origin" -ForegroundColor Yellow
Write-Host ""

$build = Read-Host "Пересобрать APK? [Y/n]"
if ($build -eq "" -or $build -eq "Y" -or $build -eq "y") {
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    Push-Location $AndroidDir
    & .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Copy-Item "app\build\outputs\apk\debug\app-debug.apk" (Join-Path $TeleportRoot "Teleport.apk") -Force
        Write-Host "APK: $TeleportRoot\Teleport.apk" -ForegroundColor Green
    }
    Pop-Location
}

Write-Host "Готово. В приложении: «Войти через Telegram»" -ForegroundColor Green
