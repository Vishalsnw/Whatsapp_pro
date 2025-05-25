@echo off
rem -----------------------------------------------------------------------------
rem Build script for Windows
rem -----------------------------------------------------------------------------

set DIR=%~dp0
set APP_HOME=%DIR%
set JAVA_EXE=java

if not defined JAVA_HOME goto findJavaFromPath

set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto init

:findJavaFromPath
where java >NUL 2>NUL
if %ERRORLEVEL% == 0 goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
exit /b 1

:init
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
