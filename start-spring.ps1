$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Project root: $projectRoot"

function Find-JavaExecutable {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $candidates = @(
        "C:\Program Files\Microsoft\jdk-*\bin\java.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe",
        "C:\Program Files\Java\jdk-*\bin\java.exe",
        "C:\Program Files\Java\jre-*\bin\java.exe",
        "$env:USERPROFILE\AppData\Local\Programs\Eclipse Adoptium\jdk-*\bin\java.exe"
    )

    foreach ($pattern in $candidates) {
        $matches = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Sort-Object FullName -Descending
        if ($matches) { return $matches[0].FullName }
    }

    return $null
}

function Find-MySqlExecutable {
    $cmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $candidates = @(
        "C:\Program Files\MySQL\MySQL Server *\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server *\bin\mysqld.exe"
    )

    foreach ($pattern in $candidates) {
        $matches = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Sort-Object FullName -Descending
        if ($matches) { return $matches[0].FullName }
    }

    return $null
}

function Get-MySqlBinDir {
    $bin = Get-ChildItem -Path "C:\Program Files\MySQL\MySQL Server *\bin" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($bin) { return $bin.FullName }
    return $null
}

$javaExecutable = Find-JavaExecutable
if (-not $javaExecutable) {
    throw "Java was not found. Install JDK 17+ and make sure java.exe is available on PATH."
}

$javaHome = Split-Path (Split-Path $javaExecutable)
$env:PATH = "$javaHome\bin;$env:PATH"

if (-not $env:DB_USERNAME) { $env:DB_USERNAME = "root" }
if ($null -eq $env:DB_PASSWORD) { $env:DB_PASSWORD = "" }
Set-Location $projectRoot

$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "Starting MySQL service: $($mysqlService.Name)"
        Start-Service -Name $mysqlService.Name
    }
} else {
    $mysqlBinDir = Get-MySqlBinDir
    if ($mysqlBinDir) {
        $mysqldPath = Join-Path $mysqlBinDir "mysqld.exe"
        if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
            Write-Host "Starting MySQL from: $mysqldPath"
            Start-Process -FilePath $mysqldPath -WindowStyle Minimized
            for ($attempt = 1; $attempt -le 30; $attempt++) {
                Start-Sleep -Seconds 1
                if (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue) {
                    break
                }
            }
        }
    }
}

if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
    Write-Warning "MySQL is not running or not installed. The app needs MySQL to load hotel data."
    Write-Host "Install MySQL Server 8.x and then run the SQL file in database\hotel_management.sql before starting the app."
}

$jar = Join-Path $projectRoot "target\hotel-management-1.0.0.jar"
$projectHasJar = Test-Path $jar

$mavenCandidates = @(
    "$env:USERPROFILE\Downloads\apache-maven-3.9.9\bin\mvn.cmd",
    "$env:USERPROFILE\Downloads\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd",
    "C:\Program Files\Apache\maven\bin\mvn.cmd",
    "C:\Program Files\Maven\bin\mvn.cmd"
)

$maven = $mavenCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $maven) { $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source }

$webConnection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($webConnection) {
    $webProcess = Get-Process -Id $webConnection[0].OwningProcess -ErrorAction SilentlyContinue
    if ($webProcess -and $webProcess.ProcessName -eq "java") {
        Write-Host "Stopping the current Java app on port 8080"
        Stop-Process -Id $webProcess.Id -Force
        Start-Sleep -Seconds 2
    } else {
        throw "Port 8080 is already being used by another application. Close it before starting the hotel app."
    }
}

$schemaPath = Join-Path $projectRoot "database\hotel_management.sql"
$mysqlClient = Find-MySqlExecutable
if ($mysqlClient -and (Test-Path $schemaPath)) {
    $mysqlDir = Split-Path $mysqlClient
    $mysqlExe = Join-Path $mysqlDir "mysql.exe"
    if ((Test-Path $mysqlExe) -and (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
        Write-Host "Initializing the hotel database if needed."
        try {
            $mysqlPassword = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "" }
            $mysqlArgs = @('-uroot', "--password=$mysqlPassword")

            & $mysqlExe @mysqlArgs -e "CREATE DATABASE IF NOT EXISTS hotel_management;" 2>$null | Out-Null

            $schemaArgs = @('-uroot', "--password=$mysqlPassword", 'hotel_management')

            Get-Content -Raw $schemaPath | & $mysqlExe @schemaArgs 2>$null | Out-Null
        } catch {
            Write-Warning "Database schema setup was skipped automatically. You can still run the SQL script manually if needed."
        }
    }
}

Write-Host "Starting Spring Boot at http://localhost:8080"
Write-Host "Keep this window open during your presentation. Press Ctrl+C to stop."
if ($maven) {
    & $maven -q spring-boot:run
} elseif ($projectHasJar) {
    Write-Warning "Maven was not found. Starting the existing packaged JAR."
    & java -jar $jar
} else {
    throw "Neither Maven nor the packaged JAR was found. Install Maven or rebuild the project first."
}
