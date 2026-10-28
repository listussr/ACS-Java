\encoding UTF8

DROP DATABASE IF EXISTS rag_lab;
DROP ROLE IF EXISTS rag_user;

CREATE ROLE rag_user LOGIN PASSWORD 'rag_pass';

CREATE DATABASE rag_lab
    OWNER rag_user
    ENCODING 'UTF8'
    TEMPLATE template0;

\connect rag_lab
SET ROLE rag_user;

\ir 01_schema.sql
\ir 02_data.sql
\ir 03_change_log.sql
