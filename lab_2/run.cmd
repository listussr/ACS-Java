@echo off
rem ====================================================================
rem  Build (with unit tests) and start the Spring Boot application.
rem  After start: http://localhost:8081/   (stop: Ctrl+C)
rem ====================================================================
setlocal
if not defined MVN set MVN=C:\apache-maven-3.9.6\bin\mvn.cmd
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Java\jdk-21"

pushd "%~dp0rag-spring"
call "%MVN%" -q -B package
if errorlevel 1 (popd & exit /b 1)
"%JAVA_HOME%\bin\java" -jar target\rag-spring.jar
popd
endlocal
