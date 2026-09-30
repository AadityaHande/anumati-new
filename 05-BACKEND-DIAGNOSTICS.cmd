@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ============================================================
echo ANUMATI - BACKEND DIAGNOSTICS
echo ============================================================
echo.

echo [1] PostgreSQL :5432
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok=Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue; if($ok){'REACHABLE'}else{'NOT REACHABLE'}"
echo.

echo [2] Backend port 8080
netstat -ano -p tcp | findstr /R /C:":8080 .*LISTENING"
if errorlevel 1 echo NOT LISTENING

echo.
echo [3] Backend health
curl.exe -sS -i --max-time 5 http://localhost:8080/actuator/health
if errorlevel 1 echo [INFO] Health endpoint is not reachable.
echo.

echo [4] Helpful process details for port 8080
for /f "tokens=5" %%P in ('netstat -ano -p tcp ^| findstr /R /C:":8080 .*LISTENING"') do (
  echo PID %%P
  tasklist /FI "PID eq %%P"
)
echo.
pause
