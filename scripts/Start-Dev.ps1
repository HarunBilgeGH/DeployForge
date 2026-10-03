param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

if (-not (Test-Path -LiteralPath '.env')) {
    throw '.env.example dosyasını .env olarak kopyala ve boş parolaları doldur.'
}

$allowedKeys = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'ADMIN_USERNAME', 'ADMIN_PASSWORD')
foreach ($line in Get-Content -LiteralPath '.env' -Encoding utf8) {
    if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) { continue }
    if ($line -notmatch '^([A-Z_]+)=(.*)$') { throw '.env satırı geçersiz; ANAHTAR=değer biçimini kullan.' }
    $configKey = $Matches[1]
    $configValue = $Matches[2]
    if ($configKey -notin $allowedKeys) { throw "Desteklenmeyen yapılandırma anahtarı: $configKey" }
    if ($configValue.StartsWith('"') -or $configValue.StartsWith("'")) {
        throw '.env değerlerini bu geliştirme betiği için tırnaksız yaz.'
    }
    [Environment]::SetEnvironmentVariable($configKey, $configValue, 'Process')
}
foreach ($configKey in @('DB_PASSWORD', 'ADMIN_USERNAME', 'ADMIN_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($configKey, 'Process'))) {
        throw "$configKey boş bırakılamaz."
    }
}
if ($env:ADMIN_PASSWORD.Length -lt 16 -or $env:ADMIN_PASSWORD.Length -gt 72) {
    throw 'ADMIN_PASSWORD 16–72 karakter olmalı.'
}

Get-Command java -ErrorAction Stop | Out-Null
Get-Command docker -ErrorAction Stop | Out-Null
& docker compose up -d --wait postgres
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL başlatılamadı; Docker Desktop ve Compose durumunu kontrol et.' }
& .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
if ($LASTEXITCODE -ne 0) { throw 'Uygulama başlatılamadı; üstteki hata ayrıntısını kontrol et.' }
