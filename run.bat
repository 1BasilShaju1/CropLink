@echo off
setlocal
echo ============================================================
echo   Starting CropLink - Farm-to-Door Delivery Platform
echo ============================================================
echo   URL: http://localhost:8080
echo   Database: croplink_db (MySQL port 3306)
echo ============================================================

if exist "target\croplink-1.0.0.jar" (
    java -jar target\croplink-1.0.0.jar
) else if exist "target\farm2door-1.0.0.jar" (
    java -jar target\farm2door-1.0.0.jar
) else (
    call mvnw.cmd spring-boot:run
)

pause
