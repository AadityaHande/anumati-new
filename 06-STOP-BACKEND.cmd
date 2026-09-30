@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

echo ============================================================
echo ANUMATI - STOP BACKEND ON PORT 8080
echo ============================================================
echo.
set "FOUND=0"
for /f "tokens=5" %%P in ('netstat -ano -p tcp ^| findstr /R /C:":8080 .*LISTENING"') do (
  set "FOUND=1"
  echo Found listener PID %%P on port 8080.
  tasklist /FI "PID eq %%P" | findstr /I /C:"%%P" 
  echo.
  choice /C YN /N /M "Stop PID %%P? [Y/N] "
  if errorlevel 2 (
    echo Skipped PID %%P.
  ) else (
    taskkill /PID %%P /T /F
    if errorlevel 1 (echo [ERROR] Could not stop PID %%P.) else (echo [OK] PID %%P stopped.)
  )
)
if "%FOUND%"=="0" echo No process is listening on port 8080.
echo.
pause
