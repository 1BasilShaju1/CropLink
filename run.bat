@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\basil\AppData\Local\Temp\apache-maven-3.9.6\bin;%PATH%"

echo ============================================================
echo   Starting CropLink - Farm-to-Door Delivery Platform
echo ============================================================
echo   URL: http://localhost:8080
echo   Database: croplink_db (MySQL port 3306)
echo ============================================================

if exist "target\croplink-1.0.0.jar" (
    "%JAVA_HOME%\bin\java.exe" -jar target\croplink-1.0.0.jar
) else if exist "target\farm2door-1.0.0.jar" (
    "%JAVA_HOME%\bin\java.exe" -jar target\farm2door-1.0.0.jar
) else (
    call "C:\Users\basil\AppData\Local\Temp\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
)

pause
