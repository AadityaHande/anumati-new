@echo off
setlocal EnableExtensions EnableDelayedExpansion

if /i "%~1"=="__BACKEND_RUNNER" goto :backend_runner

cd /d "%~dp0"
set "ROOT=%~dp0"

call :banner "ANUMATI - BACKEND BUILD AND START"

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Java not found in PATH.
  goto :fail
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Maven not found in PATH.
  goto :fail
)

for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%~V"
set "JAVA_VER=%JAVA_VER:"=%"
for /f "tokens=1 delims=." %%M in ("%JAVA_VER%") do set "JAVA_MAJOR=%%M"
if "%JAVA_MAJOR%"=="" set "JAVA_MAJOR=0"
if %JAVA_MAJOR% LSS 21 (
  echo [ERROR] Java 21+ is required. Detected Java %JAVA_VER%.
  goto :fail
)

echo Java %JAVA_VER% detected. Maven compiler target remains Java 21.
echo.

call :check_postgres
if errorlevel 1 goto :fail

rem ------------------------------------------------------------
rem If a healthy Anumati backend is already running, reuse it.
rem This avoids false failures when a previous demo session is still up.
rem ------------------------------------------------------------
call :backend_health
if not errorlevel 1 (
  echo.
  echo [OK] Anumati backend is already running on port 8080.
  echo Health: http://localhost:8080/actuator/health
  echo.
  echo No second backend will be started.
  echo Keep the existing backend process running.
  echo.
  pause
  exit /b 0
)

call :check_backend_port
if errorlevel 1 goto :fail

call :banner "BUILDING BACKEND"
cd /d "%ROOT%backend"
call mvn -U clean package
if errorlevel 1 (
  echo [ERROR] Backend build/tests failed. Backend will NOT be started.
  goto :fail
)

if not exist "%ROOT%backend\.data\documents" mkdir "%ROOT%backend\.data\documents" >nul 2>&1
if not exist "%ROOT%logs" mkdir "%ROOT%logs" >nul 2>&1

set "DATABASE_URL=jdbc:postgresql://localhost:5432/anumati"
set "DATABASE_USERNAME=anumati"
set "DATABASE_PASSWORD=anumati-local"
set "ANUMATI_ADMIN_USERNAME=admin"
set "ANUMATI_ADMIN_PASSWORD=change-me"
set "ANUMATI_APPLICANT_USERNAME=applicant"
set "ANUMATI_APPLICANT_PASSWORD=change-me"
set "ANUMATI_OFFICER_USERNAME=officer"
set "ANUMATI_OFFICER_PASSWORD=change-me"
set "ANUMATI_ALLOWED_ORIGINS=http://localhost:3000"
set "ANUMATI_STORAGE_MODE=local"
set "SPRING_PROFILES_ACTIVE=local"
set "ANUMATI_STORAGE_LOCAL_ROOT=%ROOT%backend\.data\documents"
set "ANUMATI_STORAGE_LOCAL_BASE_URL=http://localhost:8080"
set "ANUMATI_SESSION_SECURE_COOKIE=false"
set "REQUIRE_VERIFIED_SOURCES=true"
set "PORT=8080"

call :banner "STARTING BACKEND"
echo Backend gets its own persistent Command Prompt window.
echo It will remain open so the actual Spring Boot error is always visible.
echo.

rem IMPORTANT: this quoting keeps the child batch invocation intact on Windows.
start "ANUMATI Backend" "%ComSpec%" /k call "%~f0" __BACKEND_RUNNER
if errorlevel 1 (
  echo [ERROR] Windows could not start the backend Command Prompt.
  goto :fail
)

for /l %%I in (1,1,30) do (
  call :backend_health
  if not errorlevel 1 (
    echo.
    echo ============================================================
    echo BACKEND IS RUNNING
    echo ============================================================
    echo Health:  http://localhost:8080/actuator/health
    echo Swagger: http://localhost:8080/swagger-ui.html
    echo.
    echo Keep the ANUMATI Backend window open.
    echo.
    pause
    exit /b 0
  )
  timeout /t 2 /nobreak >nul
)

echo.
echo ============================================================
echo BACKEND DID NOT BECOME HEALTHY WITHIN 60 SECONDS
echo ============================================================
echo The ANUMATI Backend window contains the actual Spring Boot error.
echo Run 05-BACKEND-DIAGNOSTICS.cmd for port and health details.
echo.
pause
exit /b 1

:backend_runner
cd /d "%~dp0backend"
set "ROOT=%~dp0"
set "DATABASE_URL=jdbc:postgresql://localhost:5432/anumati"
set "DATABASE_USERNAME=anumati"
set "DATABASE_PASSWORD=anumati-local"
set "ANUMATI_ADMIN_USERNAME=admin"
set "ANUMATI_ADMIN_PASSWORD=change-me"
set "ANUMATI_APPLICANT_USERNAME=applicant"
set "ANUMATI_APPLICANT_PASSWORD=change-me"
set "ANUMATI_OFFICER_USERNAME=officer"
set "ANUMATI_OFFICER_PASSWORD=change-me"
set "ANUMATI_ALLOWED_ORIGINS=http://localhost:3000"
set "ANUMATI_STORAGE_MODE=local"
set "SPRING_PROFILES_ACTIVE=local"
set "ANUMATI_STORAGE_LOCAL_ROOT=%~dp0backend\.data\documents"
set "ANUMATI_STORAGE_LOCAL_BASE_URL=http://localhost:8080"
set "ANUMATI_SESSION_SECURE_COOKIE=false"
set "REQUIRE_VERIFIED_SOURCES=true"
set "PORT=8080"
call mvn spring-boot:run -Dspring-boot.run.profiles=local
set "BACKEND_EXIT=%ERRORLEVEL%"

echo.
if not "%BACKEND_EXIT%"=="0" (
  echo ============================================================
  echo SPRING BOOT STOPPED WITH AN ERROR
  echo ============================================================
  echo The real exception is shown above in this window.
  echo Exit code: %BACKEND_EXIT%
) else (
  echo ============================================================
  echo SPRING BOOT STOPPED NORMALLY
  echo ============================================================
)
echo.
pause
exit /b %BACKEND_EXIT%

:check_postgres
echo [PRECHECK] PostgreSQL :5432...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ok = Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue; if ($ok) { exit 0 } else { exit 1 }"
if errorlevel 1 (
  echo [ERROR] PostgreSQL is not reachable on localhost:5432.
  echo         Start PostgreSQL and verify the anumati database/user.
  exit /b 1
)
echo [OK] PostgreSQL :5432 is reachable.
exit /b 0

:backend_health
curl.exe -sS --max-time 3 -o "%TEMP%\anumati-health.txt" -w "%%{http_code}" http://localhost:8080/actuator/health > "%TEMP%\anumati-health-code.txt" 2>nul
set "HEALTH_CODE="
set /p HEALTH_CODE=<"%TEMP%\anumati-health-code.txt"
if "%HEALTH_CODE%"=="200" exit /b 0
exit /b 1

:check_backend_port
 echo.
 echo [PRECHECK] Backend port 8080...
 for /f "tokens=5" %%P in ('netstat -ano -p tcp ^| findstr /R /C:":8080 .*LISTENING"') do (
   echo [ERROR] Port 8080 is already in use by PID %%P.
   echo         The process is not responding as an Anumati health endpoint.
   echo         Run 05-BACKEND-DIAGNOSTICS.cmd to inspect it, then stop it before retrying.
   exit /b 1
 )
 echo [OK] Backend port 8080 is available.
 exit /b 0

:fail
 echo.
 echo ============================================================
 echo ANUMATI BACKEND COMMAND FAILED
 echo ============================================================
 echo This window will stay open. Fix the reported issue, then rerun.
 pause
 exit /b 1

:banner
echo ============================================================
echo %~1
echo ============================================================
exit /b 0
