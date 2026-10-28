@echo off
rem Local test SMTP server for notifications: SMTP 127.0.0.1:1025, web UI http://localhost:8025
"%~dp0tools\mailpit\mailpit.exe" --smtp 127.0.0.1:1025 --listen 127.0.0.1:8025
