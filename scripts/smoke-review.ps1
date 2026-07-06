param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
$OutputEncoding = New-Object System.Text.UTF8Encoding($false)
[Console]::OutputEncoding = $OutputEncoding

$Root = Split-Path -Parent $PSScriptRoot
$Backend = Join-Path $Root "backend"
$ServerOut = Join-Path $Backend "server.out.log"
$ServerErr = Join-Path $Backend "server.err.log"

function Stop-ReviewServer {
    Get-CimInstance Win32_Process |
        Where-Object {
            $_.Name -eq "java.exe" -and
            $_.CommandLine -and
            $_.CommandLine.Contains("ReviewCopilotApplication")
        } |
        ForEach-Object {
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        }
}

function Wait-Health {
    param([int]$Port)

    for ($i = 0; $i -lt 60; $i++) {
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:$Port/health" -Method Get -TimeoutSec 2
            if ($health.status -eq "UP") {
                return $health
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "Backend did not become healthy on port $Port"
}

function New-DemoRepository {
    $demo = Join-Path $env:TEMP ("agentscope-review-demo-" + [guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Path $demo | Out-Null
    git -C $demo init -b main | Out-Null
    git -C $demo config user.email "review@example.local" | Out-Null
    git -C $demo config user.name "Review Bot" | Out-Null
    git -C $demo config core.autocrlf false | Out-Null

    New-Item -ItemType Directory -Path (Join-Path $demo "src/main/java/demo") -Force | Out-Null
    $file = Join-Path $demo "src/main/java/demo/DemoService.java"
    Set-Content -Path $file -Encoding UTF8 -Value @'
package demo;

public class DemoService {
  public String hello() {
    return "ok";
  }
}
'@
    git -C $demo add . | Out-Null
    git -C $demo commit -m "initial" | Out-Null

    Set-Content -Path $file -Encoding UTF8 -Value @'
package demo;

public class DemoService {
  public String hello() {
    try {
      return risky();
    } catch (Exception e) {
      e.printStackTrace();
      return "fallback";
    }
  }

  private String risky() {
    return "ok";
  }
}
'@

    return $demo
}

function Read-Utf8Url {
    param([string]$Url)

    $client = New-Object System.Net.WebClient
    try {
        $bytes = $client.DownloadData($Url)
        return [System.Text.Encoding]::UTF8.GetString($bytes)
    } finally {
        $client.Dispose()
    }
}

$mvnProcess = $null
try {
    Stop-ReviewServer
    Remove-Item -LiteralPath $ServerOut, $ServerErr -Force -ErrorAction SilentlyContinue

    $mvnProcess = Start-Process `
        -FilePath "mvn.cmd" `
        -ArgumentList @("-q", "spring-boot:run") `
        -WorkingDirectory $Backend `
        -WindowStyle Hidden `
        -RedirectStandardOutput $ServerOut `
        -RedirectStandardError $ServerErr `
        -PassThru

    $health = Wait-Health -Port $Port
    $demo = New-DemoRepository

    $body = @{
        repoPath = $demo
        diffMode = "WORKING_TREE"
        sessionId = "demo-session"
        focusCategories = @("bug-risk", "test-gap", "maintainability")
    } | ConvertTo-Json -Depth 5

    $job = Invoke-RestMethod `
        -Uri "http://localhost:$Port/api/reviews" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 10

    $current = $null
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Milliseconds 500
        $current = Invoke-RestMethod `
            -Uri ("http://localhost:$Port/api/reviews/{0}" -f $job.id) `
            -Method Get `
            -TimeoutSec 5
        if ($current.status -eq "COMPLETED" -or $current.status -eq "FAILED") {
            break
        }
    }

    if ($current.status -ne "COMPLETED") {
        throw "Review did not complete: $($current.status) $($current.errorMessage)"
    }

    $reportContent = Read-Utf8Url -Url ("http://localhost:$Port/api/reviews/{0}/report.md" -f $job.id)

    if (-not ([string]$reportContent).Contains("AgentScope-Java RC4") -or
        -not ([string]$reportContent).Contains("provider")) {
        throw "Report does not contain the expected explicit model provider note"
    }

    if (@($current.findings).Count -lt 2) {
        throw "Expected at least two findings, got $(@($current.findings).Count)"
    }

    $demoStatus = git -C $demo status --short
    $result = [pscustomobject]@{
        health = $health.status
        jobId = $job.id
        status = $current.status
        findingCount = @($current.findings).Count
        reportContainsModelNote = $true
        demoRepoStatus = ($demoStatus -join "; ")
    }
    $result | ConvertTo-Json -Depth 5
} finally {
    if ($mvnProcess -and -not $mvnProcess.HasExited) {
        Stop-Process -Id $mvnProcess.Id -Force -ErrorAction SilentlyContinue
    }
    Stop-ReviewServer
}
