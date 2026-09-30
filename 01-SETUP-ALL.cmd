@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

rem ============================================================
rem ANUMATI - FIRST TIME / REFRESH SETUP
rem ============================================================
rem When double-clicked, relaunch in a persistent CMD window so
rem the window stays open even if a child command exits.

if /I not "%~1"=="__ANUMATI_INNER" (
    start "ANUMATI Setup" "%ComSpec%" /k call "%~f0" __ANUMATI_INNER
    exit /b 0
)

set "ROOT=%~dp0"
set "LOGDIR=%ROOT%logs"
if not exist "%LOGDIR%" mkdir "%LOGDIR%" >nul 2>&1
set "LOGFILE=%LOGDIR%\setup-last.log"

>"%LOGFILE%" echo ============================================================
>>"%LOGFILE%" echo ANUMATI SETUP STARTED %date% %time%
>>"%LOGFILE%" echo ============================================================

:START
cls
echo ============================================================
echo ANUMATI - FIRST TIME / REFRESH SETUP
echo ============================================================
echo.
echo This window is persistent and will NOT close automatically.
echo Full setup log: %LOGFILE%
echo.

rem -------------------- Tool checks ---------------------------
echo [1/6] Checking Java...
where java >nul 2>&1
if errorlevel 1 goto FAIL_JAVA
java -version 2>&1
java -version >>"%LOGFILE%" 2>&1
if errorlevel 1 goto FAIL_GENERIC

echo.
echo [2/6] Checking Maven...
where mvn >nul 2>&1
if errorlevel 1 goto FAIL_MAVEN
call mvn -version
call mvn -version >>"%LOGFILE%" 2>&1
if errorlevel 1 goto FAIL_GENERIC

echo.
echo [3/6] Checking Node.js / npm...
where node >nul 2>&1
if errorlevel 1 goto FAIL_NODE
where npm >nul 2>&1
if errorlevel 1 goto FAIL_NPM
node --version
if errorlevel 1 goto FAIL_GENERIC
call npm --version
if errorlevel 1 goto FAIL_GENERIC
node --version >>"%LOGFILE%" 2>&1
call npm --version >>"%LOGFILE%" 2>&1

rem ---------------- PostgreSQL check --------------------------
echo.
echo [4/6] Checking PostgreSQL on localhost:5432...
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet) { exit 0 } else { exit 1 }"
if errorlevel 1 goto FAIL_POSTGRES
echo PostgreSQL is reachable on localhost:5432.
>>"%LOGFILE%" echo PostgreSQL reachable on localhost:5432.

rem ---------------- Frontend setup ----------------------------
echo.
echo [5/6] Frontend dependencies + typecheck + production build...
cd /d "%ROOT%frontend"
if exist package-lock.json (
    echo package-lock.json found - running npm ci for a clean, reproducible dependency set.
    call npm ci
) else if exist node_modules (
    echo package-lock.json not found - node_modules exists, running npm install.
    call npm install
) else (
    echo package-lock.json not found - running npm install.
    call npm install
)
if errorlevel 1 goto FAIL_FRONTEND_INSTALL

echo.
echo Frontend typecheck...
call npm run typecheck
if errorlevel 1 goto FAIL_FRONTEND_TYPECHECK

echo.
echo Frontend production build...
call npm run build
if errorlevel 1 goto FAIL_FRONTEND_BUILD

rem ---------------- Backend setup -----------------------------
echo.
echo [6/6] Backend clean package + full test suite...
cd /d "%ROOT%backend"
call mvn -U clean package
if errorlevel 1 goto FAIL_BACKEND

>>"%LOGFILE%" echo SETUP COMPLETE %date% %time%
echo.
echo ============================================================
echo SETUP COMPLETE
 echo ============================================================
echo Backend:  build + tests passed
echo Frontend: dependencies + typecheck + production build passed
echo PostgreSQL: localhost:5432 reachable
echo.
echo Next: run 02-BACKEND-BUILD-AND-START.cmd
echo.
echo Press any key to close this setup window.
pause >nul
exit /b 0

:FAIL_JAVA
echo.
echo [ERROR] Java not found. Install JDK 21+ and reopen CMD.
goto FAIL
:FAIL_MAVEN
echo.
echo [ERROR] Maven not found. Install Maven 3.9+ and reopen CMD.
goto FAIL
:FAIL_NODE
echo.
echo [ERROR] Node.js not found. Install Node.js 20+ and reopen CMD.
goto FAIL
:FAIL_NPM
echo.
echo [ERROR] npm not found. Reinstall Node.js or fix PATH.
goto FAIL
:FAIL_POSTGRES
echo.
echo [ERROR] PostgreSQL is not reachable on localhost:5432.
echo Start PostgreSQL, then rerun the setup.
goto FAIL
:FAIL_FRONTEND_INSTALL
echo.
echo [ERROR] Frontend dependency installation failed.
goto FAIL
:FAIL_FRONTEND_TYPECHECK
echo.
echo [ERROR] Frontend typecheck failed.
goto FAIL
:FAIL_FRONTEND_BUILD
echo.
echo [ERROR] Frontend production build failed.
goto FAIL
:FAIL_BACKEND
echo.
echo [ERROR] Backend build/test failed.
goto FAIL
:FAIL_GENERIC
echo.
echo [ERROR] A setup command failed. See the output immediately above.
goto FAIL

:FAIL
>>"%LOGFILE%" echo SETUP FAILED %date% %time%
echo.
echo ============================================================
echo SETUP STOPPED - WINDOW WILL STAY OPEN
 echo ============================================================
echo Log: %LOGFILE%
echo.
echo Fix the reported problem, then rerun 01-SETUP-ALL.cmd
pause
exit /b 1
