# Автодеплой Teleport на Amvera Git
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$remote = "https://git.msk0.amvera.ru/w1nst/teleport"
$publicUrl = "https://teleport.w1nst.amvera.io"

function Find-Git {
    $candidates = @(
        "git",
        "${env:ProgramFiles}\Git\cmd\git.exe",
        "${env:ProgramFiles(x86)}\Git\cmd\git.exe"
    )
    foreach ($c in $candidates) {
        if (Get-Command $c -ErrorAction SilentlyContinue) { return (Get-Command $c).Source }
    }
    return $null
}

$git = Find-Git
if (-not $git) {
    Write-Host "Устанавливаю Git..."
    winget install --id Git.Git -e --accept-package-agreements --accept-source-agreements --silent
    $git = Find-Git
}
if (-not $git) { throw "Git не найден. Установите с https://git-scm.com/download/win" }

# Credentials
$credFile = Join-Path $root "amvera.credentials"
$user = "w1nst"
$token = $null
if (Test-Path $credFile) {
    Get-Content $credFile | ForEach-Object {
        if ($_ -match '^AMVERA_USER=(.+)$') { $user = $matches[1].Trim() }
        if ($_ -match '^AMVERA_TOKEN=(.+)$') { $token = $matches[1].Trim() }
    }
}
if (-not $token) {
    $sec = Read-Host "Токен Amvera (Репозиторий → пароль/токен)" -AsSecureString
    $token = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))
}

$authRemote = "https://${user}:$([uri]::EscapeDataString($token))@git.msk0.amvera.ru/w1nst/teleport"

Set-Location $root
if (-not (Test-Path ".git")) {
    & $git init
    & $git branch -M master
}
& $git remote remove amvera 2>$null
& $git remote add amvera $authRemote

& $git add -A
$status = & $git status --porcelain
if ($status) {
    & $git -c user.email="deploy@teleport.local" -c user.name="Teleport Deploy" commit -m "Deploy Teleport to Amvera"
}

Write-Host "Отправка на Amvera..."
& $git push -u amvera master --force

# Публичный URL в приложениях
& (Join-Path $root "setup_public_url.ps1") $publicUrl

Write-Host ""
Write-Host "Готово!"
Write-Host "  Сайт/API: $publicUrl"
Write-Host "  Health:   $publicUrl/health"
Write-Host ""
Write-Host "В Amvera: Настройки → Переменные:"
Write-Host "  DATA_DIR=/data"
Write-Host "  PUBLIC_URL=$publicUrl"
