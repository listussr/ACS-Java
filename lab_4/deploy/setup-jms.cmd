@echo off
rem ====================================================================
rem  Lab 4, task 2: JMS administered objects + JavaMail session on GlassFish.
rem  Run once, with the server running. (JDBC resource jdbc/ragDS comes
rem  from lab 1: ..\lab_1\deploy\setup-resources.cmd)
rem ====================================================================
setlocal
if not defined GF_HOME set "GF_HOME=%~dp0..\..\lab_1\server\glassfish7"
set ASADMIN="%GF_HOME%\bin\asadmin.bat"

rem 1. Physical destination in the built-in OpenMQ broker: a TOPIC
rem    (publish/subscribe - every subscriber gets its own copy of each message)
call %ASADMIN% create-jmsdest --desttype topic RagChangesTopic

rem 2. Connection factory - how the application connects to the broker
call %ASADMIN% create-jms-resource --restype jakarta.jms.ConnectionFactory ^
  --description "RAG change events" jms/ragConnectionFactory

rem 3. Destination resource - JNDI name the code uses, pointing to the physical topic
call %ASADMIN% create-jms-resource --restype jakarta.jms.Topic ^
  --property Name=RagChangesTopic jms/ragChanges

rem 4. JavaMail session for notifications (local Mailpit SMTP on port 1025)
call %ASADMIN% create-mail-resource --mailhost 127.0.0.1 --mailuser rag-kb ^
  --fromaddress rag-kb@localhost --property mail.smtp.port=1025 mail/ragMail

rem 5. Notification settings as server JNDI resources (read by NotificationMdb on every message,
rem    so a change applies immediately - see README)
call %ASADMIN% create-custom-resource --restype java.lang.Integer ^
  --factoryclass org.glassfish.resources.custom.factory.PrimitivesAndStringFactory ^
  --property value=150 rag/notify/maxDocumentTokens
call %ASADMIN% create-custom-resource --restype java.lang.String ^
  --factoryclass org.glassfish.resources.custom.factory.PrimitivesAndStringFactory ^
  --property "value=kb-admin@rag.local,lecturer@rag.local" rag/notify/recipients

call %ASADMIN% list-jmsdest
call %ASADMIN% list-jms-resources
call %ASADMIN% list-mail-resources
call %ASADMIN% list-custom-resources
endlocal
