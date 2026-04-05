$ErrorActionPreference = 'Stop'

$candidates = @()
if ($env:JAVA_HOME) {
  $candidates += $env:JAVA_HOME
}

$candidates += @(
  'E:\Java\Java17',
  'C:\Program Files\Java\jdk-17',
  'C:\Program Files\Java\jdk-17.0.0',
  'C:\Program Files\Eclipse Adoptium\jdk-17',
  'C:\Program Files\Eclipse Adoptium\jdk-17.0.0'
)

$candidates += Get-ChildItem -Path 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like 'jdk-17*' } |
  ForEach-Object { $_.FullName }

$candidates += Get-ChildItem -Path 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like 'jdk-17*' } |
  ForEach-Object { $_.FullName }

$java17Home = $candidates |
  Where-Object { $_ -and (Test-Path (Join-Path $_ 'bin\java.exe')) } |
  Select-Object -First 1

if (-not $java17Home) {
  throw "JDK 17 not found. Install JDK 17 and set JAVA_HOME."
}

$env:JAVA_HOME = $java17Home
$env:Path = "$env:JAVA_HOME\bin;" + ($env:Path -replace [regex]::Escape("$env:JAVA_HOME\bin;"), "")

Write-Host "JAVA_HOME set to $env:JAVA_HOME"
java -version
