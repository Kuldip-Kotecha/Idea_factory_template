@echo off
setlocal enabledelayedexpansion

:: =============================================================================
:: Reusable Microservice Deployment Script
:: Usage: deploy-service.bat <service-name>
:: Example: deploy-service.bat user-service
:: =============================================================================

set "SERVICE=%~1"
if "%SERVICE%"=="" (
    echo Usage: %~nx0 ^<service-name^>
    echo Available services: api-gateway, user-service, core-service, notification-service, utility-service
    exit /b 1
)

echo [INFO] Preparing deployment for %SERVICE%...

if not exist "%~dp0..\Infrastructure\.env" (
    echo [WARNING] No .env found in Other\Infrastructure\. Using defaults or environment variables.
)

echo [INFO] Running Maven build & tests for %SERVICE%...
cd /d "%~dp0..\..\Backend"
call mvn clean test -pl %SERVICE% -Dtest=*UnitTest,*SmokeTest
if errorlevel 1 (
    echo [ERROR] Tests failed for %SERVICE%. Aborting deployment.
    exit /b 1
)

echo [SUCCESS] %SERVICE% verified and ready for deployment.
exit /b 0
