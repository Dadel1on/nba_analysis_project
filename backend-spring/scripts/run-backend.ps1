param(
  [ValidateSet('mysql')]
  [string]$Profile = 'mysql'
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

& "$PSScriptRoot\use-java17.ps1"

$mvn = 'D:\software\maven\apache-maven-3.9.14\bin\mvn.cmd'
if (-not (Test-Path $mvn)) {
  throw "Maven not found at $mvn"
}

Write-Host "Starting backend with Maven..."
Write-Host "Active Spring profile: $Profile"
& $mvn "-Dspring-boot.run.profiles=$Profile" spring-boot:run
