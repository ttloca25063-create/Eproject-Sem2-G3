@echo off
cd /d "%~dp0"
set "MYSQL_JAR=%~dp0dist\lib\mysql-connector-j-26.10.0.jar"
if not exist "%MYSQL_JAR%" set "MYSQL_JAR=%~dp0lib\mysql-connector-j-26.10.0.jar"
set "CLASSES_DIR=%~dp0build\classes"

set "JAVA_EXE=java"
if exist "C:\Program Files\Java\jdk-25.0.2\bin\java.exe" set "JAVA_EXE=C:\Program Files\Java\jdk-25.0.2\bin\java.exe"

set "JAVAFX_LIB=%~dp0dist\lib"
if not exist "%JAVAFX_LIB%\javafx.controls.jar" (
    if exist "C:\Users\Student\Downloads\openjfx-25.0.5_windows-x64_bin-sdk\javafx-sdk-25.0.5\lib" (
        set "JAVAFX_LIB=C:\Users\Student\Downloads\openjfx-25.0.5_windows-x64_bin-sdk\javafx-sdk-25.0.5\lib"
    ) else if exist "C:\Users\Admin\Downloads\openjfx-25.0.5_windows-x64_bin-sdk\javafx-sdk-25.0.5\lib" (
        set "JAVAFX_LIB=C:\Users\Admin\Downloads\openjfx-25.0.5_windows-x64_bin-sdk\javafx-sdk-25.0.5\lib"
    )
)

echo ========================================================
echo    Starting CFL - Certificate Generation System...
echo ========================================================
"%JAVA_EXE%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%CLASSES_DIR%;%MYSQL_JAR%" main.AppLauncher
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application stopped with an error code: %ERRORLEVEL%
    pause
)
