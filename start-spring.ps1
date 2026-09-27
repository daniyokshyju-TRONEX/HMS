$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$mysqlRoot = "C:\Program Files\MySQL\MySQL Server 8.4"
$mysqlData = "C:\mysql-data"
$mavenCandidates = @(
    "$env:USERPROFILE\Downloads\apache-maven-3.9.9\bin\mvn.cmd",
    "$env:USERPROFILE\Downloads\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd",
    "C:\Program Files\Apache\maven\bin\mvn.cmd"
)

$maven = $mavenCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $maven) {
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
}
if (-not $maven) {
    Write-Warning "Maven was not found. The existing packaged JAR will be used if available."
}

$env:PATH = "$javaHome\bin;$mysqlRoot\bin;$env:PATH"
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = "root" }
if ($null -eq $env:DB_PASSWORD) { $env:DB_PASSWORD = "" }
Set-Location $projectRoot

if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
    Start-Process -FilePath "$mysqlRoot\bin\mysqld.exe" `
        -ArgumentList "--basedir=`"$mysqlRoot`" --datadir=`"$mysqlData`"" `
        -WindowStyle Minimized
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        Start-Sleep -Seconds 1
        if (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue) {
            break
        }
    }
}

if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
    throw "MySQL did not start. Check the MySQL installation."
}

$webConnection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($webConnection) {
    $webProcess = Get-Process -Id $webConnection[0].OwningProcess -ErrorAction SilentlyContinue
    if ($webProcess -and $webProcess.ProcessName -eq "java") {
        Stop-Process -Id $webProcess.Id -Force
        Start-Sleep -Seconds 2
    } else {
        throw "Port 8080 is already being used by another application."
    }
}

Write-Host "Starting Spring Boot at http://localhost:8080"
Write-Host "Keep this window open during your presentation. Press Ctrl+C to stop."
if ($maven) {
    & $maven -q spring-boot:run
} else {
    $jar = Join-Path $projectRoot "target\hotel-management-1.0.0.jar"
    if (-not (Test-Path $jar)) {
        throw "Maven was not found and the packaged JAR is missing. Install Maven or build the project first."
    }
    Write-Warning "Maven was not found. Starting the existing packaged JAR."
    & java -jar $jar
}
