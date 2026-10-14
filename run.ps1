# CropLink - Farm-to-Door Delivery Platform PowerShell Launcher
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\basil\AppData\Local\Temp\apache-maven-3.9.6\bin;$env:PATH"

Write-Host "============================================================" -ForegroundColor Green
Write-Host "  Starting CropLink - Farm-to-Door Delivery Platform" -ForegroundColor Green
Write-Host "  URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  Database: croplink_db (MySQL on localhost:3306)" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green

if (Test-Path "target\croplink-1.0.0.jar") {
    & "$env:JAVA_HOME\bin\java.exe" -jar "target\croplink-1.0.0.jar"
} elseif (Test-Path "target\farm2door-1.0.0.jar") {
    & "$env:JAVA_HOME\bin\java.exe" -jar "target\farm2door-1.0.0.jar"
} else {
    & "C:\Users\basil\AppData\Local\Temp\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
}
