@echo off
rem ====================================================================
rem  Build the WAR and (re)deploy it to GlassFish via asadmin.
rem  The server and the JDBC resource jdbc/ragDS are shared with lab 1
rem  (see ..\lab_1\deploy\setup-resources.cmd) and JMS/mail resources (setup-jms.cmd). Override GF_HOME if needed.
rem  After deployment:
rem    http://localhost:8080/rag-jms/api/documents   (REST, XML+XSLT)
rem    http://localhost:8080/rag-jms/                (JSF pages + change log: changes.xhtml)
rem ====================================================================
setlocal
if not defined GF_HOME set "GF_HOME=%~dp0..\..\lab_1\server\glassfish7"
if not defined MVN set MVN=C:\apache-maven-3.9.6\bin\mvn.cmd
set ASADMIN="%GF_HOME%\bin\asadmin.bat"

pushd "%~dp0..\rag-jms"
call "%MVN%" -q -B package
if errorlevel 1 (popd & exit /b 1)
popd

call %ASADMIN% deploy --force=true --contextroot rag-jms --name rag-jms "%~dp0..\rag-jms\target\rag-jms.war"
call %ASADMIN% list-applications
endlocal
