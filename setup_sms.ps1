#Requires -Version 5.1
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$serverDir = Join-Path $root "server"
$envFile = Join-Path $serverDir ".env"
$localProps = Join-Path $root "android\local.properties"

Write-Host "=== Teleport SMS setup ===" -ForegroundColor Cyan

if (-not (Test-Path $envFile)) {
    Copy-Item (Join-Path $serverDir ".env.example") $envFile
    Write-Host "Создан server\.env"
}

$ip = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*" -and $_.PrefixOrigin -ne "WellKnown" } |
    Select-Object -ExpandProperty IPAddress -First 1

if (-not $ip) { $ip = "192.168.1.100" }
$url = "http://${ip}:8765/"
Write-Host "IP для телефона: $ip" -ForegroundColor Green
Write-Host "URL API: $url"

$lines = @()
if (Test-Path $localProps) { $lines = Get-Content $localProps -Encoding UTF8 }
$filtered = $lines | Where-Object { $_ -notmatch "^api\.base\.url=" }
$filtered += "api.base.url=$url"
$filtered | Set-Content $localProps -Encoding UTF8
Write-Host "Обновлён android\local.properties (api.base.url)"

Write-Host ""
Write-Host "Дальше:" -ForegroundColor Yellow
Write-Host "1. Откройте server\.env"
Write-Host "2. Вставьте SMSRU_API_ID с https://sms.ru/?panel=api"
Write-Host "3. SMS_PROVIDER=smsru"
Write-Host "4. Запустите START_SMS_SERVER.bat"
Write-Host "5. Пересоберите APK (BUILD_TELEPORT_APK.bat)"
