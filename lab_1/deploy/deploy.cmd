@echo off
rem ====================================================================
rem  Build the WAR and (re)deploy it to GlassFish via asadmin.
rem  After deployment: http://localhost:8080/rag-app/
rem ====================================================================
setlocal
set ASADMIN="%~dp0..\server\glassfish7\bin\asadmin.bat"
if not defined MVN set MVN=C:\apache-maven-3.9.6\bin\mvn.cmd

pushd "%~dp0..\rag-app"
call "%MVN%" -q -B package
if errorlevel 1 (popd & exit /b 1)
popd

rem --force=true replaces the application if it is already deployed
call %ASADMIN% deploy --force=true --contextroot rag-app --name rag-app "%~dp0..\rag-app\target\rag-app.war"
call %ASADMIN% list-applications
endlocal
