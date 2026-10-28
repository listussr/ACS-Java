@echo off
rem ====================================================================
rem  One-time GlassFish setup: JDBC connection pool to PostgreSQL and
rem  JDBC resource jdbc/ragDS (referenced from persistence.xml).
rem  Prerequisites: server is running, postgresql-*.jar is in
rem  glassfish7\glassfish\domains\domain1\lib
rem ====================================================================
setlocal
set ASADMIN="%~dp0..\server\glassfish7\bin\asadmin.bat"

call %ASADMIN% create-jdbc-connection-pool ^
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource ^
  --restype javax.sql.DataSource ^
  --property "serverName=localhost:portNumber=5432:databaseName=rag_lab:user=rag_user:password=rag_pass" ^
  ragPool

call %ASADMIN% create-jdbc-resource --connectionpoolid ragPool jdbc/ragDS

rem Check: the server tries to open a DB connection
call %ASADMIN% ping-connection-pool ragPool
endlocal
