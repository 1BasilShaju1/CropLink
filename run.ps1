# CropLink - Farm-to-Door Delivery Platform PowerShell Launcher
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  Starting CropLink - Farm-to-Door Delivery Platform" -ForegroundColor Green
Write-Host "  URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  Database: croplink_db (MySQL on localhost:3306)" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green

if (Test-Path "target\croplink-1.0.0.jar") {
    java -jar "target\croplink-1.0.0.jar"
} elseif (Test-Path "target\farm2door-1.0.0.jar") {
    java -jar "target\farm2door-1.0.0.jar"
} else {
    & ".\mvnw.cmd" spring-boot:run
}
