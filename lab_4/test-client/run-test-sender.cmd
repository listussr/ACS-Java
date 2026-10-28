@echo off
rem Usage: run-test-sender.cmd bad|valid   (see TestSender.java)
setlocal
if not defined GF_HOME set "GF_HOME=%~dp0..\..\lab_1\server\glassfish7"
set "CP=%GF_HOME%\mq\lib\imq.jar;%GF_HOME%\glassfish\modules\jakarta.jms-api.jar"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21"
rem Java 11+ can run a single .java file without a separate compile step
"%JAVA_HOME%\bin\java" -cp "%CP%" "%~dp0TestSender.java" %*
endlocal
