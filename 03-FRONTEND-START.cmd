@echo off
setlocal EnableExtensions
cd /d "%~dp0frontend"

if /I not "%~1"=="__FRONTEND_RUNNER" (
    start "ANUMATI Frontend" "%ComSpec%" /k call "%~f0" __FRONTEND_RUNNER
    exit /b 0
)

echo ============================================================
echo ANUMATI - FRONTEND START
 echo ============================================================

echo [PRECHECK] Node.js / npm...
where node >nul 2>&1
if errorlevel 1 goto :fail_node
where npm >nul 2>&1
if errorlevel 1 goto :fail_npm
node --version
call npm --version

if not exist node_modules (
  echo.
  echo Installing frontend dependencies...
  if exist package-lock.json (
    call npm ci
  ) else (
    call npm install
  )
  if errorlevel 1 goto :fail_install
)

set "NEXT_PUBLIC_API_URL=http://localhost:8080"
set "NEXT_PUBLIC_SITE_URL=http://localhost:3000"
set "NEXT_PUBLIC_PRODUCT_NAME=Anumati"

echo.
echo [OK] Frontend dependencies are ready.
echo Frontend: http://localhost:3000
echo Backend expected at: http://localhost:8080
echo.
echo The window remains open while Next.js is running.
echo.
call npm run dev
set "FRONTEND_EXIT=%ERRORLEVEL%"

echo.
echo ============================================================
echo FRONTEND STOPPED (exit code %FRONTEND_EXIT%)
echo ============================================================
pause
exit /b %FRONTEND_EXIT%

:fail_node
echo [ERROR] Node.js not found in PATH.
pause
exit /b 1
:fail_npm
echo [ERROR] npm not found in PATH.
pause
exit /b 1
:fail_install
echo [ERROR] Frontend dependency installation failed.
pause
exit /b 1
