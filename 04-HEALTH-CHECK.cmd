@echo off
setlocal

echo ==============================================
echo ANUMATI LOCAL HEALTH CHECK
echo ==============================================
echo.

echo Checking backend health...
curl -sS http://localhost:8080/actuator/health
if errorlevel 1 echo [FAIL] Backend health endpoint is not reachable.

echo.
echo Checking frontend...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $r=Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:3000' -TimeoutSec 5; Write-Host ('HTTP ' + $r.StatusCode) } catch { Write-Host '[FAIL] Frontend is not reachable.' }"
echo.
pause
