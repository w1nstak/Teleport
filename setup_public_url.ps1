# Указывает один публичный URL для iOS, Android и сервера.
# Использование:
#   .\setup_public_url.ps1 https://api.mysite.ru
#   .\setup_public_url.ps1   (читает PUBLIC_URL из public_url.env)

param(
    [string]$Url = ""
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Normalize-Url([string]$u) {
    $u = $u.Trim()
    if ($u -match "^PUBLIC_URL=") { $u = ($u -split "=", 2)[1].Trim() }
    $u = $u.Trim().TrimEnd("/")
    if (-not $u.StartsWith("http://") -and -not $u.StartsWith("https://")) {
        throw "URL должен начинаться с http:// или https://"
    }
    return "$u/"
}

if (-not $Url) {
    $envFile = Join-Path $root "public_url.env"
    if (-not (Test-Path $envFile)) { throw "Укажите URL или создайте public_url.env" }
    $line = Get-Content $envFile | Where-Object { $_ -match "^PUBLIC_URL=" } | Select-Object -First 1
    if (-not $line) { throw "В public_url.env нет PUBLIC_URL=" }
    $Url = $line
}

$base = Normalize-Url $Url
$hostOnly = $base.TrimEnd("/")

Write-Host "Публичный URL: $base"

# public_url.env
@"
# Публичный URL сервера Teleport
PUBLIC_URL=$($hostOnly)
"@ | Set-Content -Encoding UTF8 (Join-Path $root "public_url.env")

# iOS project.yml
$iosYml = Join-Path $root "ios\project.yml"
if (Test-Path $iosYml) {
    $lines = Get-Content $iosYml
    $replaced = $false
    $newLines = foreach ($line in $lines) {
        if (-not $replaced -and $line -match '^\s+API_BASE_URL:\s*') {
            $replaced = $true
            "    API_BASE_URL: $base"
        } else { $line }
    }
    Set-Content -Encoding UTF8 $iosYml $newLines
    Write-Host "OK ios/project.yml"
}

# Android local.properties
$androidProps = Join-Path $root "android\local.properties"
$example = Join-Path $root "android\local.properties.example"
if (-not (Test-Path $androidProps) -and (Test-Path $example)) {
    Copy-Item $example $androidProps
}
if (Test-Path $androidProps) {
    $props = Get-Content $androidProps -ErrorAction SilentlyContinue
    $filtered = @($props | Where-Object { $_ -notmatch "^api\.base\.url=" })
    $filtered += "api.base.url=$base"
    Set-Content -Encoding UTF8 $androidProps $filtered
    Write-Host "OK android/local.properties"
}

# server .env
$serverEnv = Join-Path $root "server\.env"
$serverLines = @()
if (Test-Path $serverEnv) {
    $serverLines = @(Get-Content $serverEnv | Where-Object { $_ -notmatch "^PUBLIC_URL=" })
}
$serverLines += "PUBLIC_URL=$hostOnly"
Set-Content -Encoding UTF8 $serverEnv $serverLines
Write-Host "OK server/.env"

Write-Host ""
Write-Host "Готово. Пересоберите IPA/APK с новым URL."
Write-Host "Проверка: curl $hostOnly/health"
