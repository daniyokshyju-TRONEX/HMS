$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$mysqlRoot = "C:\Program Files\MySQL\MySQL Server 8.4"
$mysqlData = "C:\mysql-data"

$env:PATH = "$javaHome\bin;$mysqlRoot\bin;$env:PATH"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = ""

Set-Location $projectRoot

$mysqlListening = Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue
if (-not $mysqlListening) {
    if (-not (Test-Path $mysqlData)) {
        throw "MySQL data directory was not found at $mysqlData."
    }

    Start-Process -FilePath "$mysqlRoot\bin\mysqld.exe" `
        -ArgumentList "--basedir=`"$mysqlRoot`" --datadir=`"$mysqlData`"" `
        -WindowStyle Minimized

    for ($attempt = 1; $attempt -le 30; $attempt++) {
        Start-Sleep -Seconds 1
        if (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue) {
            break
        }
    }

    if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
        throw "MySQL did not start. Check the MySQL installation and data directory."
    }
}

$sourceFiles = Get-ChildItem -Recurse -Filter *.java -Path ".\src" |
    ForEach-Object { $_.FullName }
javac -cp ".\lib\mysql-connector-j.jar" -d ".\out" $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Write-Host "The web server is already running at http://localhost:8080"
    Start-Process "http://localhost:8080"
    exit 0
}

Write-Host "Starting Hotel Management System at http://localhost:8080"
Write-Host "Keep this window open while using the website. Press Ctrl+C to stop it."
java -cp ".\out;.\lib\mysql-connector-j.jar" WebServer
