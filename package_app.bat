@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-25-Full"
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "APP_NAME=FxShield"
set "VERSION=1.0.0"

REM لازم يكون نفس mainClass اللي فعليًا بتشغّل منه التطبيق
set "MAIN_CLASS=fx.shield.cs.UX.DashBoardPage"

REM يقلل تحذيرات Java 25 مع Gradle native-platform
set "JAVA_TOOL_OPTIONS=--enable-native-access=ALL-UNNAMED"

echo ========================================
echo   FxShield - Packaging (Gradle + jpackage)
echo ========================================
echo Using JDK at: %JAVA_HOME%
java --version
echo:

echo Checking for JavaFX modules...
if not exist "%JAVA_HOME%\jmods\javafx.base.jmod" (
    echo ERROR: Missing JavaFX jmods in %JAVA_HOME%
    pause
    exit /b 1
)
echo JavaFX modules found!
echo:

echo [0/4] Detecting project name from settings.gradle...
set "PROJECT_NAME="
for /f "usebackq tokens=1,* delims==" %%A in (`findstr /i "rootProject.name" settings.gradle`) do (
    set "RAW=%%B"
)
if not defined RAW (
    echo ERROR: Could not read rootProject.name from settings.gradle
    echo Please ensure settings.gradle contains: rootProject.name = 'YourName'
    pause
    exit /b 1
)
set "PROJECT_NAME=!RAW!"
set "PROJECT_NAME=!PROJECT_NAME:'=!"
set "PROJECT_NAME=!PROJECT_NAME:"=!"
set "PROJECT_NAME=!PROJECT_NAME: =!"
echo Project name: !PROJECT_NAME!
echo:

echo [1/4] Building distributable with Gradle (installDist)...
call gradlew clean installDist
if errorlevel 1 (
    echo ERROR: Gradle build failed.
    pause
    exit /b 1
)

set "DIST_DIR=build\install\!PROJECT_NAME!"
set "APP_LIB_DIR=!DIST_DIR!\lib"

if not exist "!APP_LIB_DIR!" (
    echo ERROR: Expected lib dir not found: !APP_LIB_DIR!
    echo Check settings.gradle rootProject.name and Gradle installDist output.
    dir "build\install" 2>nul
    pause
    exit /b 1
)

echo [2/4] Preparing temp_input...
rd /s /q "temp_input" 2>nul
mkdir "temp_input"

rd /s /q "dist" 2>nul
if exist "dist" ren "dist" "dist_old_%RANDOM%" 2>nul
mkdir "dist"

echo [3/4] Copying jars from Gradle installDist...
copy "!APP_LIB_DIR!\*.jar" "temp_input\" >nul
if errorlevel 1 (
    echo ERROR: Could not copy jars from !APP_LIB_DIR!
    pause
    exit /b 1
)

REM حدد jar الرئيسي: أول jar يطابق اسم المشروع + لا يكون sources/javadoc
set "MAIN_JAR="
for %%F in ("temp_input\!PROJECT_NAME!-*.jar") do (
    echo %%~nF | findstr /i "sources javadoc" >nul
    if errorlevel 1 (
        set "MAIN_JAR=%%~nxF"
        goto :JAR_FOUND
    )
)

REM fallback: أي jar غير sources/javadoc (لو اسم jar مش مطابق لاسم المشروع)
for %%F in ("temp_input\*.jar") do (
    echo %%~nF | findstr /i "sources javadoc" >nul
    if errorlevel 1 (
        if not defined MAIN_JAR (
            set "MAIN_JAR=%%~nxF"
        )
    )
)

:JAR_FOUND
if not defined MAIN_JAR (
    echo ERROR: Could not find a usable main jar in temp_input
    dir temp_input
    pause
    exit /b 1
)

echo Main JAR detected: !MAIN_JAR!
echo:

echo [4/4] Checking for WiX Toolset...
set "HAS_WIX=1"
where candle >nul 2>nul
if errorlevel 1 set "HAS_WIX=0"

set "PKG_TYPE=exe"
if "!HAS_WIX!"=="0" set "PKG_TYPE=app-image"

set "UPGRADE_UUID=93c5fd00-1234-5678-9abc-def012345678"

echo Packaging with jpackage...
jpackage ^
  --name "%APP_NAME%" ^
  --app-version "%VERSION%" ^
  --input "temp_input" ^
  --main-jar "!MAIN_JAR!" ^
  --main-class "%MAIN_CLASS%" ^
  --type "!PKG_TYPE!" ^
  --dest dist ^
  --win-dir-chooser --win-menu --win-shortcut --win-per-user-install ^
  --win-upgrade-uuid %UPGRADE_UUID% ^
  --description "Fx Shield - System Monitor and Optimizer" ^
  --vendor "AQU" ^
  --add-modules javafx.controls,javafx.base,javafx.graphics,javafx.fxml,java.net.http,java.management,jdk.charsets ^
  --jlink-options "--bind-services" ^
  --java-options "--enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics"

set "JP_ERR=%ERRORLEVEL%"

rd /s /q "temp_input" 2>nul

if not "%JP_ERR%"=="0" (
    echo ERROR: jpackage failed.
    pause
    exit /b %JP_ERR%
)

echo:
echo ========================================
if "!HAS_WIX!"=="1" echo SUCCESS: Installer created in dist folder.
if "!HAS_WIX!"=="0" echo SUCCESS: App Image created in dist\%APP_NAME%.
echo ========================================
pause
endlocal
