param(
    [switch]$SkipFrontendInstall
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Backend = Join-Path $Root "backend"
$Frontend = Join-Path $Root "frontend"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Command
    )

    Write-Host "==> $Name" -ForegroundColor Cyan
    & $Command
}

Invoke-Step "Check Java package prefix" {
    $badPackages = rg -n "^package " "$Backend/src/main/java" "$Backend/src/test/java" |
        Where-Object { $_ -notmatch "package com\.ynzz" }
    if ($badPackages) {
        $badPackages | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        throw "Found Java package declarations that do not start with com.ynzz"
    }
}

Invoke-Step "Backend tests" {
    Push-Location $Backend
    try {
        mvn test
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend dependencies" {
    if (-not $SkipFrontendInstall) {
        Push-Location $Frontend
        try {
            npm install
        } finally {
            Pop-Location
        }
    }
}

Invoke-Step "Frontend type-check" {
    Push-Location $Frontend
    try {
        npm run type-check
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend unit tests" {
    Push-Location $Frontend
    try {
        npm run test:unit -- --run
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend production build" {
    Push-Location $Frontend
    try {
        npm run build
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend lint" {
    Push-Location $Frontend
    try {
        npm run lint
    } finally {
        Pop-Location
    }
}

Write-Host "All verification steps passed." -ForegroundColor Green
